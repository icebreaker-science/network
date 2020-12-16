//fix ref weight, **requires list weights**
MATCH(n:Topic)-[rel:RELATED_TO]-(n2:Topic)
WITH rel, size(rel.references) as relW
SET rel.weight = relW

//check for rel inconsistency
MATCH (n:Topic)-[rel:RELATED_TO]-(n2:Topic) 
WHERE size(rel.references) <> rel.weight 
RETURN rel
LIMIT 10

// switch to string references
MATCH (n1:Topic)-[rel:RELATED_TO]-(n2:Topic)
WITH rel, reduce(acc="", var in rel.references | acc+var+",") as refs
SET rel.references = LEFT(refs, SIZE(refs)-1)


// switch to list references
MATCH (n1:Topic)-[rel:RELATED_TO]-(n2:Topic)
With rel, rel.references as refs
set rel.references=[x in split(refs, ',') where x <> '']


// check for inconsistent refs
MATCH (n1:Topic)-[rel:RELATED_TO]-(n2:Topic)
WHERE rel.weight <> size(rel.references)
RETURN rel
LIMIT 10

// remove bidirectional dependencies (sets them to sets)
MATCH (n1:Topic)-[rel1:RELATED_TO]->(n2:Topic),(n1)<-[rel2:RELATED_TO]-(n2)
WITH rel1, rel2, n1, n2, apoc.convert.toSet(rel1.references+rel2.references) as allRefs
SET rel1.references = allRefs
DELETE rel2
