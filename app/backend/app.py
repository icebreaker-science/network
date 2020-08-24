import pickle
import networkx as nx
from flask import Flask, jsonify, request
from flask_cors import CORS


# --- Init ---

with open('./data.pickle', 'rb') as f:
    G = pickle.load(f)['G']

app = Flask(__name__)
CORS(app)


# --- Basic Functions ---

def graph_to_edge_array(g):
    return [{
        'node1': e[0],
        'node2': e[1],
        'data': e[2]
    } for e in g.edges(data=True)]


def graph_to_json_response(g):
    return jsonify(graph_to_edge_array(g))


# --- Routes ---

@app.route('/')
def hello_world():
    return jsonify({'message': 'Here lives the Icebreaker Network.'})


@app.route('/node')
def get_all_nodes():
    return jsonify([{
        'name': n[0],
        'weight': n[1]['weight']
    } for n in G.nodes(data=True)])


@app.route('/ego_graph')
def get_ego_graph():
    node = request.args.get('node')
    g = nx.ego_graph(G, node)
    if len(g.nodes()) > 200:
        top_nodes = [n[0] for n in sorted(g.nodes(data=True), key=lambda n: n[1]['weight'], reverse=True)][:200]
        top_nodes.append(node)
        g = nx.subgraph(g, top_nodes)
    return graph_to_json_response(g)


@app.route('/shortest_paths_graph')
def get_shortest_paths_graph():
    """
    Returns a graph containing all shortest paths between all pairs of the selected nodes.

    It expects a comma-separated list argument.
    """
    nodes = request.args.get('nodes').split(',')
    result_nodes = set(nodes)

    for i in range(0, len(nodes) - 1):
        for j in range(i + 1, len(nodes)):
            for sp in nx.all_shortest_paths(G, source=nodes[i], target=nodes[j]):
                for n in sp:
                    result_nodes.add(n)
    g = nx.subgraph(G, result_nodes)
    return graph_to_json_response(g)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port='12251')
