create table papers (
  icebreaker_id serial primary key,
  doi text,
  core_id text,
  title text,
  abstract text,
  has_full_text boolean,
  year integer,
  topics jsonb,
  subjects jsonb,
  language_detected_most_likely text,
  language_detected_probabilities jsonb
);

create index on papers (doi);

create index on papers (title);


create table wikidata (
  wikidata_id text primary key,
  name text,
  data jsonb
);

create index on wikidata (name);
