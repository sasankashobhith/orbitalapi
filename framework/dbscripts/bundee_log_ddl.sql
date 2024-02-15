drop table if exists  log_table;

create table log_table (
	log_ts timestamptz not null default current_timestamp,
    id bigserial,
	server text,
	thread_id bigint,
	log_level text,
	mod_nm text,
	api_nm text,
    log_msg text
);