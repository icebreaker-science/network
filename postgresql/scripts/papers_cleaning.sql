-- To remove redundant papers
-- It is important to use "is not distinct from" instead of "=" since "null = null" returns null.
-- The efficiency can most probably be improved. The complexity of the task should be in O(n) but this query seems to
-- cost more as it causes a nested loop (why??).
delete from papers p
using (
  select doi, title, abstract, year, topics, subjects, has_full_text, min(icebreaker_id) as min_id
  from papers
  group by doi, title, abstract, year, topics, subjects, has_full_text
  having count(*) > 1
) x
where
  p.doi is not distinct from x.doi
  and p.title is not distinct from x.title
  and p.abstract is not distinct from x.abstract
  and p.year is not distinct from x.year
  and p.topics is not distinct from x.topics
  and p.subjects is not distinct from x.subjects
  and p.has_full_text is not distinct from x.has_full_text
  and p.icebreaker_id <> min_id;
-- Alternative/better solution?
delete from papers p
using (
  select row(doi, title, abstract, year, topics, subjects, has_full_text)::text all_rows, min(icebreaker_id) as min_id
  from papers
  group by row(doi, title, abstract, year, topics, subjects, has_full_text)::text
  having count(*) > 1
) x
where
  row(p.doi, p.title, p.abstract, p.year, p.topics, p.subjects, p.has_full_text)::text = x.all_rows
  and p.icebreaker_id <> min_id;


-- We only want publications written in English. Those with an unknown language shall also be removed because,
-- as a small analysis shows, they often do not contain an actual abstract text or are just weird.
delete from papers p
where p.language_detected_most_likely is distinct from 'en';
