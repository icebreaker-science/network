-- Print the disk usage of the tables
-- Source: https://wiki.postgresql.org/wiki/Disk_Usage
select
  *,
  pg_size_pretty(total_bytes) as total,
  pg_size_pretty(index_bytes) as INDEX,
  pg_size_pretty(toast_bytes) as toast,
  pg_size_pretty(table_bytes) as TABLE
from (
       select
         *,
         total_bytes - index_bytes - COALESCE(toast_bytes, 0) as table_bytes
       from (
              select
                c.oid,
                nspname                               as table_schema,
                relname                               as TABLE_NAME,
                c.reltuples                           as row_estimate,
                pg_total_relation_size(c.oid)         as total_bytes,
                pg_indexes_size(c.oid)                as index_bytes,
                pg_total_relation_size(reltoastrelid) as toast_bytes
              from pg_class c
                left join pg_namespace n on n.oid = c.relnamespace
              where relkind = 'r' and nspname = 'public'
            ) a
     ) a;


-- Display the current activity
select * from pg_stat_activity
order by usename, backend_start desc;


-- Kill a query
-- select pg_cancel_backend(PID); -- The soft way
-- select pg_terminate_backend(PID); -- The hard way
