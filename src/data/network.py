"""Basic functions to create the network.

The workflow to generate a network:

1. count_edges()
2. Define a set of nodes that shall be in the network.
3. create_network()
4. add_references()

Optionally, it is possible to derive a network that has a reduced amount of edges: reduce_network()
"""

from time import sleep
from typing import Iterator
from tqdm.auto import tqdm
import networkx as nx
import csv
import os

from data import data_models
from data.data_models import BasicDataEntry


def count_edges(entries, number_entries=None):
    """
    Computes the weights for all edges. This contains ALL topics - without any filtering!
    """
    edge_dict = {}  # (node1:str, node2:str) -> weight where node1<node2
    for entry in tqdm(entries, total=number_entries):
        if entry.topics is None:
            continue
        for i in range(0, len(entry.topics) - 1):
            for j in range(i, len(entry.topics)):
                t1 = entry.topics[i]
                t2 = entry.topics[j]
                if t2 < t1:
                    tmp = t1
                    t1 = t2
                    t2 = tmp
                if (t1, t2) not in edge_dict:
                    edge_dict[(t1, t2)] = 0
                edge_dict[(t1, t2)] += 1
    return edge_dict


def create_network(node_dict, edge_dict, node_set):
    """
    :param node_dict: dict<node id, weight>; this dict may also contain nodes that should not be part of the network.
    :param edge_dict: dict<(node1 id, node2 id), weight>; this dict may also contain edges that should not be part of the network.
    :param node_set: a set of nodes that should be included in the network; other nodes will be ignored.
    """
    G = nx.Graph()
    for ((n1, n2), w) in tqdm(edge_dict.items(), total=len(edge_dict), desc='Filter edges'):
#         if w < 3:
#             continue
        if n1 == n2:
            continue
        if n1 not in node_set or n2 not in node_set:
            continue
        G.add_edge(n1, n2, weight=w)
    nx.set_node_attributes(G, node_dict, name='weight')

    # Compute a normalized edge weight
    normalized_weights = {}
    for e in tqdm(G.edges(data=True), total=len(G.edges()), desc='Computing normalized edge weights'):
        n1, n2, data = e
        w = data['weight']
        normalized_weight = (w / G.degree(n1, weight='weight')) * (w / G.degree(n2, weight='weight'))
        normalized_weights[(n1, n2)] = normalized_weight
    nx.set_edge_attributes(G, normalized_weights, name='normalized_weight')
    
    return G


def add_references_to_edges(entries: Iterator[BasicDataEntry], G, number_entries=None):
    """
    This function add the corresponding references to all edges
    """
    node_set = set(G.nodes())
    edge_dict = {}  # (node1:str, node2:str) -> ids of references as comma separated list where node1<node2
    for entry in tqdm(entries, total=number_entries, desc='Add edge references'):
        if entry.topics is None:
            continue
        nodes = list(node_set.intersection(set(entry.topics)))
        for i in range(0, len(nodes) - 1):
            for j in range(i + 1, len(nodes)):
                t1 = nodes[i]
                t2 = nodes[j]
                if t2 < t1:
                    tmp = t1
                    t1 = t2
                    t2 = tmp
                if (t1, t2) not in edge_dict:
                    edge_dict[(t1, t2)] = ''
                edge_dict[(t1, t2)] += '{},'.format(entry.icebreaker_id)
    nx.set_edge_attributes(G, edge_dict, name='references')


def reduce_network(G, k):
    """
    Let's only keep the top k edges per node
    """
    G2 = nx.Graph()
    for n1 in tqdm(G.nodes(), total=len(G.nodes()), desc='Reducing network'):
        G2.add_node(n1, weight=G.nodes[n1]['weight'])
        neighbors = list(G[n1].items())
        top_neighbors = sorted(neighbors, key=lambda neigh: neigh[1]['weight'], reverse=True)[:k]
        for neigh in top_neighbors:
            n2 = neigh[0]
            w = neigh[1]['weight']
            normalized_w = neigh[1]['normalized_weight']
            references = None
            if 'references' in neigh[1]:
                references = neigh[1]['references']
            if n2 < n1:
                continue
            G2.add_edge(n1, n2, weight=w, normalized_weight=normalized_w, references=references)
    return G2


def export_to_gephi(G, path):
    """
    Writes the files node_list.csv and edge_list.csv.
    """
    # Write node list
    with open(os.path.join(path, 'node_list.csv'), 'wt') as f:
        f.write('Name,Weight\n')
        for n, data in G.nodes(data=True):
            w = data['weight']
            line = '{},{}'.format(n, w)
            f.write(line + '\n')
        
    # Write edge list
    with open(os.path.join(path, 'edge_list.csv'), 'wt') as f:
        f.write('Source,Target,Type,Weight,NormalizedWeight\n')
        for n1, n2, data in G.edges(data=True):
            line = '{},{},undefined_relation,{},{:.16f}'.format(n1, n2, data['weight'], data['normalized_weight'])
            f.write(line + '\n')


def _node_name_to_neo4j_id(name):
    return name.replace(' ', '_')


def export_to_neo4j(G, path):
    """
    Writes csv files that can be imported into neo4j. This file can be imported with the import tool
    """
    # Write node list  
    with open(os.path.join(path, 'nodes.csv'), 'wt') as f:
        f.write(':ID,name,weight:int,:LABEL\n')
        for n, data in G.nodes(data=True):
            w = data['weight']
            line = '{},{},{},Topic'.format(_node_name_to_neo4j_id(n), n, w)
            f.write(line + '\n')

    # Write edge list
    with open(os.path.join(path, 'edges.csv'), 'wt') as f:
        f.write(':START_ID,:END_ID,:TYPE,weight:int,normalizedWeight:float,references\n')
        csv_writer = csv.writer(f)
        for n1, n2, data in G.edges(data=True):
            csv_writer.writerow([_node_name_to_neo4j_id(n1), _node_name_to_neo4j_id(n2), 'RELATED_TO', 
                            data['weight'], data['normalized_weight'], data['references']])
            