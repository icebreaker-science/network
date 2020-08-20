copy papers
    from '/basics.csv'
    with (format csv, header);


select *
from papers
where icebreaker_id in (137003,316120,328911,393415,931701,989933,1052152,1071207,2224072,7268788);


-- Same DOI: 251970,3359789
-- Same DOI+title: 226911,3192873
select count(*), sum(x.c)
from (
    select doi, count(*) as c
    from papers
    where doi is not null and doi <> ''
    group by doi, title
    having count(*) > 1
) as x;

select x.doi, x.title, x.c
from (
    select doi, title, count(*) as c
    from papers
    where doi is not null and doi <> ''
    group by doi, title
    having count(*) > 1
) as x
order by x.c desc;


select count(*)
from papers;


select
    language_detected_most_likely,
    count(*),
    round(count(*)::numeric / (select count(*) from papers), 2)
from papers
group by language_detected_most_likely
order by count(*) desc;


alter table papers
alter column topics
set data type jsonb
using topics::jsonb;


select count(*)
from papers
where has_full_text;
