# Network


## Overview

This project consists of four parts:


#### Pure Python

A plain python project resides in the `src/` folder. It contains the *good* python code. The project should be kept clean and well-documented.


#### Jupyter Notebooks

Jupyter notebooks are in `notebook/`. This is the place to generate visualizations and to play around. Every developer has a personal folder in `notebook/_private`.


#### Relational Database

We use a PostgreSQL database to store a part of the data and for some analysis. The schema is defined in `postgresql/init.sql` and interesting SQL scripts are collected in `postgresql/scripts/`.


#### Graph Database

A Neo4j instance contains the network and is great to query the graph. Scripts are stored in `neo4j/scripts`.


## Setup

A jupyter server and the two databases can be all started by calling:

```
docker-compose up
```

To install the Graph Data Science (GDS) plugin for Neo4j, put the `neo4j-graph-data-science-[version]-standalone.jar` into the `neo4j/plugins/` directory. It can be downloaded from the official Neo4j download center: https://neo4j.com/download-center/.

To install the Python requirements for the jupyter notebooks, open a terminal in the jupyter web interface and execute `pip install -r requirements.txt`.

The relevant ports and credentials are:

- Jupyter: http://localhost:12200, the login token will be printed during the startup in the console.
- PostgreSQL: runs on port 12210, db name is `icebreaker_network`, username and password are `postgres`.
- Neo4j: The web interface is at http://localhost:12220, the Bolt port is 12221. The username is `neo4j` and the password is `elsa`.

Take a look into `docker-compose.yml` for further details.


## Good To Know

The **Jetbrains editors** have a plugin for Neo4j called "Graph Database support".

There are two ways to **import data into Neo4j**. It is possible to use the `LOAD` command but this is very slow: Adding a 9 MB edge list with 240k entries can take hours. If the dataset is not tiny, it is recommended to use the import tool (`neo4j-admin import`). In combination with Docker, it is however not trivial to use because it only works if the database is shutdown but the Docker container also dies if Neo4j is stopped. A solution is to shutdown the container and than start another container which mounts the data volume and executes the import tool.

Copy a node list `nodes.csv` and an edge list `edges.csv` to `neo4j/import`, stop the Docker containers and execute:

```
docker run --rm --volumes-from icebreaker_network_neo4j neo4j neo4j-admin import --nodes /import/nodes.csv --relationships /import/edges.csv
``` 

Read https://neo4j.com/docs/operations-manual/current/tools/import/ and https://neo4j.com/docs/operations-manual/current/tutorial/import-tool/ for information about the import tool and the expected file format.
