// Count number of nodes
match (n)
return count(n);


// Find all "raman" nodes
match (n: Topic)
where n.name contains 'raman'
return n.name, n.weight;


// Get all neighbors sorted by the normalized weight
match (n:Topic)-[e1]-(m)-[e2]-(n2: Topic)
where (n.name = 'quantification' or n.name = 'characterization') and n2.name contains 'microplastic'
return m.name, m.weight;

match (n:Topic)-[e]-(m)
where n.name contains 'raman' and not m.name contains 'raman'
return m.name, m.weight, sum(e.weight), sum(e.normalizedWeight), collect(e.references)
order by sum(e.weight) desc;


// Return all pair-wise shortest paths between a list of nodes.
match (n:Topic), (m:Topic),
p = allShortestPaths((n)-[*..]-(m))
where n.name in ['water', 'female', 'raman']
and m.name in ['water', 'female', 'raman']
and n <> m
unwind nodes(p) as tmp
with collect(distinct(tmp)) as nodes_on_path
match (n:Topic)-[e]-(m:Topic)
where n in nodes_on_path and m in nodes_on_path and n.name < m.name
return n, m, e;
//return n.name, m.name, e.weight, e.normalizedWeight, e.references;

match (n:Topic), (m:Topic),
p = allShortestPaths((n)-[*..]-(m))
where n.name in ['water', 'female', 'raman']
and m.name in ['water', 'female', 'raman']
and n <> m
return p



match (n: Topic) where not exists(n.weight) or n.weight is null return n.name, n.weight;


match (n:Topic)-[e]-(m: Topic)
where n.name = 'raman'
return m.name, e.weight, e.normalizedWeight, e.references
limit 20;


match (n:Topic)-[e]-(m:Topic)
where n.name = 'raman'
with collect(m) as ms, n
match (m1: Topic)-[e]-(m2: Topic)
where (m1 in ms) and (m2 in ms) and m1.name < m2.name
return m1.name, m2.name, e.weight;


match (n:Topic)-[e]-(m:Topic)
where n.name = 'raman'
with collect(m) as ms, n
match (m1:Topic)-[e]-(m2:Topic)
where (m1 in ms) and (m2 in ms) and m1.name < m2.name
return m1.name, m2.name, e.weight;


match (n:Topic)-[e]-(m:Topic)
where n.name = 'raman'
return count(m);


match (n:Topic), (m:Topic),
p = allShortestPaths((n)-[*..]-(m))
where n.name in ['drinking water', 'raman'] and m.name in ['drinking water', 'raman'] and n.name < m.name
unwind nodes(p) as tmp
with collect(distinct(tmp)) as nodes_on_path
match (n:Topic)-[e]-(m:Topic)
where n in nodes_on_path and m in nodes_on_path and n.name < m.name
return n.name, m.name, e.weight, e.normalizedWeight, e.references;

PROFILE
MATCH (n:Topic)
WHERE n.name = 'raman'
MATCH p=(n)-[*1..2]-(m:Topic)
WHERE (n)--(m)
WITH p
UNWIND relationships(p) as rel
WITH collect(distinct rel) as rels
UNWIND rels as r
RETURN startNode(r).name, endNode(r).name,r.weight;
