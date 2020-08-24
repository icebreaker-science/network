const host = '/api';
let nodes;
let nodeMap = new Map();
let selectedNodes = new Set();

async function apiGet(endpoint) {
    return await (await fetch(`${host}${endpoint}`)).json();
}


async function loadAllNodes() {
    return await apiGet(`/node`);
}


async function loadGraph() {
    let edgeData;
    const _selectedNodes = new Array(...selectedNodes)
    if (_selectedNodes.length === 0) {
        edgeData = [];
    } else if (_selectedNodes.length === 1) {
        edgeData = await loadEgoGraph(_selectedNodes[0]);
    } else {
        edgeData = await loadShortestPathsGraph(_selectedNodes);
    }

    // Check if the selections have changed:
    const oldSelectedNodes = new Set(_selectedNodes);
    if (oldSelectedNodes.size !== selectedNodes.size) {
        return;
    }
    for (const n of oldSelectedNodes) {
        if (!selectedNodes.has(n)) {
            return;
        }
    }

    const container = document.getElementById('mynetwork');

    const data = toVisData(edgeData);

    const options = {
        nodes: {
            shape: 'dot',
            scaling: {
                min: 10,
                max: 50,
            },
            font: {
                size: 12,
                face: 'Tahoma',
            },
        },
        edges: {
            color: {
                color: '#b5d2f9',
                highlight: '#0026af'
            },
            scaling: {
                min: 0.1,
                max: 3,
            },
            smooth: {
                type: 'continuous',
            },
        },
        physics: {
            stabilization: false,
            barnesHut: {
                gravitationalConstant: -80000,
                springConstant: 0.001,
                springLength: 200,
            },
        },
        interaction: {
            tooltipDelay: 200,
            hideEdgesOnDrag: false,
        },
    };

    // create a network
    const network = new vis.Network(container, data, options);

    network.on('selectNode', (e) => {
        console.log('selectNode', e);
        const pos = e.event.center;
    });

    network.on('selectEdge', (e) => {
        if (e.edges.length > 1) {
            return;
        }
        console.log('selectEdge', e);
    });
}


async function loadEgoGraph(node) {
    return await apiGet(`/ego_graph?node=${node}`);
}


async function loadShortestPathsGraph(nodes) {
    return await apiGet(`/shortest_paths_graph?nodes=${nodes.join(',')}`);
}


function toVisData(edgeData) {
    const nodeSet = new Set();
    const nodes = [];
    const edges = [];

    for (const e of edgeData) {
        nodeSet.add(e.node1);
        nodeSet.add(e.node2);
        edges.push({
            from: e.node1,
            to: e.node2,
            value: e.data.weight
        });
    }

    nodeSet.forEach((n) => {
        if (!selectedNodes.has(n)) {
            nodes.push({
                id: n,
                label: n,
                title: n,
                value: nodeMap.get(n).weight
            });
        } else {
            nodes.push({
                id: n,
                label: n,
                title: n,
                value: nodeMap.get(n).weight,
                shape: 'triangle',
                color: '#90ee90'
            });
        }
    });

    console.log('Raw data', nodes, edges);

    return {
        nodes: new vis.DataSet(nodes),
        edges: new vis.DataSet(edges)
    };
}


function typeahead() {
    // Code taken from the examples for typeahead.js
    const substringMatcher = function (strs) {
        return function findMatches(q, cb) {
            let matches, substringRegex;

            // an array that will be populated with substring matches
            matches = [];

            // regex used to determine if a string contains the substring `q`
            substrRegex = new RegExp(q, 'i');

            // iterate through the pool of strings and for any string that
            // contains the substring `q`, add it to the `matches` array
            $.each(strs, function (i, str) {
                if (substrRegex.test(str)) {
                    matches.push(str);
                }
            });
            cb(matches);
        };
    };
    $('#keyword-field .typeahead').typeahead(
        {
            hint: true,
            highlight: true,
            minLength: 1
        },
        {
            name: 'nodes',
            source: substringMatcher(nodes.map(n => n.title)),
            limit: 10
        }
    );

    const addKeywordBtn = document.getElementById('add-keyword-btn');
    addKeywordBtn.addEventListener('click', () => {
        const nodeToAdd = $('#keyword-field .tt-input').val();
        $('#keyword-field .tt-input').val('')
        selectedNodes.add(nodeToAdd);

        const child = document.createElement('div')
        child.setAttribute('class', 'selected-keyword');
        const child1 = document.createElement('span');
        const text = document.createTextNode(nodeToAdd);
        child1.appendChild(text);
        child.appendChild(child1);
        const child2 = document.createElement('span');
        child1.appendChild(child2);
        const textX = document.createTextNode('X');
        child2.appendChild(textX)
        document.getElementById('selected-keywords').appendChild(child);

        child2.addEventListener('click', () => {
            selectedNodes.delete(nodeToAdd);
            child.remove();
            loadGraph();
        });

        loadGraph();
    });
}


async function main() {
    // All nodes will be fetched at the beginning. ~50k is not much data.
    const _nodes = await loadAllNodes();
    let nodeMinWeight;
    let nodeMaxWeight;
    _nodes.forEach(n => {
        nodeMap.set(n.name, n);
    });
    nodes = _nodes.map((n) => ({
        id: n.name,
        label: n.name,
        title: n.name,
        value: n.weight
    }));

    typeahead();
}


main();
