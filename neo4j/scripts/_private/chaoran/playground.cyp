// Count number of nodes
match (n)
return count(n);


// Get all neighbors sorted by the normalized weight
match (n:Topic)-[e]-(m)
where n.name contains 'raman' and not m.name contains 'raman'
return m.name, m.weight, e.weight, e.normalizedWeight, e.references
order by e.normalizedWeight desc;


// Return all pair-wise shortest paths between a list of nodes.
match (n:Topic), (m:Topic),
p = shortestPath((n)-[*..]-(m))
where n.name in ['virus', 'water', 'raman spectroscopy', 'nmr']
and m.name in ['virus', 'water', 'raman spectroscopy', 'nmr'] and n <> m
return p;
