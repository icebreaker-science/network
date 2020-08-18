This starts a neo4j instance. The username is `neo4j`, the password is `elsa`.

HTTP runs on port 7474 and bolt on port 7687. The Graph Data Science (GDS) plugin will be installed.


## Run

1. Create the following folders: `data`, `logs`, `plugins`, and `import`.
2. To install the GDS plugin, download `neo4j-graph-data-science-[version]-standalone.jar` into the `plugins` directory. It can be downloaded from the official Neo4j download center: https://neo4j.com/download-center/

Then, run `docker-compose up`. Open http://localhost:7474/ for the web interface.


## Import Data

Assuming, there are two .csv files containing the nodes and the edges with the following formats:

```
nodes.csv: id:string | name:string | weight:int
edges.csv: node1:string | node2:string | type | weight:int
```

Copy the files into the `import` folder. Then, open http://localhost:7474/ and execute the following two queries to import the data:

```
LOAD CSV WITH HEADERS FROM 'file:///nodes.csv' AS row
CREATE (p:Topic {id: row.id, name: row.name, weight: toInteger(row.weight)})

LOAD CSV WITH HEADERS FROM "file:///edges.csv" AS row
MATCH (node1:Topic {id: row.node1}), (node2:Topic {id: row.node2})
CREATE (node1)-[:RELATED_TO {weight: toInteger(row.weight)}]->(node2)
```

**Important: Do not forget to add some indices.**

Note: This is not the fastest solution. Adding a 9 MB edge list with 240k entries can take hours.


## Example Queries

Count the available nodes:

```
match (n)
return count(n)
```

Let's find the (unweighted) shortest path between "raman spectroscopy" and "polymerase chain reaction":

```
match (n {id:'raman_spectroscopy'}), (m {id: 'polymerase_chain_reaction'}),
p = shortestPath((n)-[*..]-(m)) 
return p
```
