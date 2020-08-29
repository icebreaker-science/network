copy papers
    from '/basics.csv'
    with (format csv, header);


select *
from papers
where icebreaker_id in (137003,316120,328911,393415,931701,989933,1052152,1071207,2224072,7268788);


-- Same DOI: 251970,3359789
-- Same DOI+title: 226911,3192873
-- Same doi+title+abstract+year+topics+subjects+has_full_text: 198606,2941182
--    After: 997,14182 -> Why? I guess because null=null -> null and not true.
select count(*), sum(x.c)
from (
    select doi, count(*) as c
    from papers
    group by doi, title, year, topics, subjects
    having count(*) > 1
) as x;

select doi, title, abstract, topics, subjects, x.c
from (
    select doi, title, abstract, topics, subjects, count(*) as c
    from papers
    group by doi, title, abstract, year, topics, subjects, has_full_text
    having count(*) > 1
) as x
order by x.c desc;


-- Before: 7836565
-- After: 5107174
-- After2: 3957723
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


select year, count(*)
from papers
group by year
order by year;