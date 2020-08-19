create table papers (
  icebreaker_id serial primary key,
  doi text,
  core_id text,
  title text,
  abstract text,
  has_full_text boolean,
  year integer,
  topics json,
  subjects json,
  language_detected_most_likely text,
  language_detected_probabilities json
);
