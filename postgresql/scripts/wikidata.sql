-- Step 0: Create tables

create table wikidata
(
	wikidata_id text not null
		constraint wikidata_pkey
			primary key,
	name text,
	data jsonb
);

create index wikidata_name_idx
	on wikidata (name);

create table wikidata_property
(
	entity_from text not null,
	entity_to text not null,
	property text not null
);

create index wikidata_property_entity_from_property_idx
	on wikidata_property (entity_from, property);

create index wikidata_property_entity_from_idx
	on wikidata_property (entity_from);

create table keyword__wikidata
(
	keyword text not null,
	wikidata text not null
		constraint keyword__wikidata_pk
			primary key,
	parent0 jsonb,
	parent1 jsonb,
	parent2 jsonb,
	parent3 jsonb,
	parent4 jsonb,
	parent5 jsonb,
	parent6 jsonb,
	parent7 jsonb,
	parent8 jsonb,
	parent9 jsonb,
	description text,
	aliases jsonb,
	parent0_to_9 jsonb,
	parent_keyword jsonb,
	parent0_to_4 jsonb,
	parent0_to_4_keyword jsonb
);

create index keyword__wikidata_keyword_index
	on keyword__wikidata (keyword);

create index keyword__wikidata_wikidata_index
	on keyword__wikidata (wikidata);


-- Prerequisite: The wikidata table is filled.


-- Step 1: Write the relationships of interest into a separate table

insert into wikidata_property (entity_from, entity_to, property)
select entity_from, entity_to, property
from (
  select
    wikidata_id as entity_from,
    jsonb_array_elements((jsonb_each(data->'claims')).value)->'mainsnak'->'datavalue'->'value'->>'id' as entity_to,
    jsonb_array_elements((jsonb_each(data->'claims')).value)->'qualifiers' as qualifiers,
    (jsonb_each(data->'claims')).key as property
  from wikidata
) as edge
where
  edge.entity_to is not null
  -- Qualifiers are weird: rat (Q43730466) as an "instance of an organisms known by a particular common name" is not a
  -- useful information.
  and edge.qualifiers is null
and property in (
  -- Note: Only those with an identifier as value will be written to the table.
  'P31', -- instance of
  'P279', -- subclass of
  'P274', -- chemical formula
  'P3780', -- active ingredient in
  'P366', -- use
  'P361', -- part of
  'P2868' -- subject has role
);

create index on wikidata_property (entity_from, property);

create index on wikidata_property (entity_from);


-- Step 2: Match our keywords with Wikidata. For a keyword, there might be multiple Wikidata entries since there are
-- multiple Wikidata entries with the same label.

insert into keyword__wikidata (keyword, wikidata)
select
  k.name,
  w.wikidata_id
from
  keyword k
  join wikidata w on k.name = lower(w.data->'labels'->'en'->>'value');

update keyword__wikidata kw
set
  description = w.data->'descriptions'->'en'->>'value',
  aliases = w.data->'aliases'->'en'
from wikidata w
where kw.wikidata = w.wikidata_id;


-- Step 3: The goal is now to find the generic terms for our keywords that can be used to build up a hierarchy.
--   We will use the properties "instance of" and "subclass of". In this step, the direct "parents" of the
--   selected wikidata entries should be identified and stored in the field "parent 0".

update keyword__wikidata k
set parent0 = k2.parent0
from (
  select
    k2.wikidata,
    jsonb_agg(distinct wp.entity_to) as parent0
  from
    keyword__wikidata k2
    join wikidata_property wp on k2.wikidata = wp.entity_from
  where
    wp.property in ('P31', 'P279')
  group by
    k2.wikidata
) as k2
where
  k.wikidata = k2.wikidata;


-- Step 4: Next, the parents of "parent0" should be identified. This will then be repeated up to parent9.

update keyword__wikidata k
set parent9 = k2.parent -- choose: parent1 to parent9
from (
  -- The query does not use the index on wikidata_property (entity_from, property) - why not??
  select
    k2.wikidata,
    jsonb_agg(distinct wp.entity_to) as parent
  from
    (
      select
        wikidata,
        jsonb_array_elements_text(parent8) as child -- choose: parent0 to parent8
      from keyword__wikidata
      where parent8 is not null -- choose: parent0 to parent8
    ) k2
    join wikidata_property wp on wp.entity_from = k2.child
  where
    wp.property in ('P31', 'P279')
  group by
    k2.wikidata
) as k2
where
  k.wikidata = k2.wikidata;

-- Here are some statistics:

select
  count(distinct keyword) number_keywords,
  count(*) as number_wikidata, -- This is the same as count(distinct wikidata) so we can define wikidata as primary key.
  sum(case when description is not null then 1 else 0 end) as description,
  sum(case when parent0 is not null then 1 else 0 end) as parent0,
  sum(case when parent1 is not null then 1 else 0 end) as parent1,
  sum(case when parent2 is not null then 1 else 0 end) as parent2,
  sum(case when parent3 is not null then 1 else 0 end) as parent3,
  sum(case when parent4 is not null then 1 else 0 end) as parent4,
  sum(case when parent5 is not null then 1 else 0 end) as parent5,
  sum(case when parent6 is not null then 1 else 0 end) as parent6,
  sum(case when parent7 is not null then 1 else 0 end) as parent7,
  sum(case when parent8 is not null then 1 else 0 end) as parent8,
  sum(case when parent9 is not null then 1 else 0 end) as parent9
from keyword__wikidata;


-- Step 5: Collect the parents 0 to 9 in a single column.

update keyword__wikidata kw
set parent0_to_9 = kw2.parent0_to_9
from
  (
    select kw3.wikidata, jsonb_agg(kw3.p) as parent0_to_9
    from
      (
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent0) p
        ) union
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent1) p
        ) union
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent2) p
        ) union
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent3) p
        ) union
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent4) p
        ) union
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent5) p
        ) union
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent6) p
        ) union
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent7) p
        ) union
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent8) p
        ) union
        (
          select kw4.wikidata, p ->> 0 as p
          from keyword__wikidata kw4, jsonb_array_elements(kw4.parent9) p
        )
      ) kw3
    group by kw3.wikidata
  ) kw2
where kw.wikidata = kw2.wikidata;

-- Repeat step 4 and 5 for parent0_to_4/parent0_to_4_keyword


-- Step 6: Filter for those parent entries that are also a keyword.

update keyword__wikidata kw0
set
  parent_keyword = kw.parent_keyword
from
(
  select kw.wikidata, jsonb_agg(distinct kw2.keyword) as parent_keyword
  from
    keyword__wikidata kw,
    jsonb_array_elements(kw.parent0_to_9) p,
    keyword__wikidata kw2
  where
    p->>0 = kw2.wikidata
  group by kw.wikidata
) kw
where kw0.wikidata = kw.wikidata;


-- Step 7: Export the results and pass it to the experts.

select
  keyword,
  weight as number_publications,
  wikidata as wikidata_id,
  description,
  aliases,
  parent0_to_4_keyword as categories
from
  (select * from keyword where length(name) > 2 order by weight desc limit 10000) k
  join keyword__wikidata kw on k.name = kw.keyword
where
  ((parent0_to_4_keyword is null) or
  (not parent0_to_4_keyword ? 'text'
  and not parent0_to_4_keyword ? 'image'
  and not parent0_to_4_keyword ? 'work'
  and not parent0_to_4_keyword ? 'information'
  and not parent0_to_4_keyword ? 'publication'
  and not parent0_to_4_keyword ? 'painting'
  and not parent0_to_4_keyword ? 'drawing'
  and not parent0_to_4_keyword ? 'location'
  and not parent0_to_4_keyword ? 'agent'
  and not parent0_to_4_keyword ? 'organization'
  and not parent0_to_4_keyword ? 'river'
  and not parent0_to_4_keyword ? 'sculpture')
  )
order by k.weight desc, k.name;
