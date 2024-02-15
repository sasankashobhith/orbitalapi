drop table if exists  pagination_data;

create table pagination_data (
	crtd_ts timestamptz not null default current_timestamp,
	
    id bigserial primary key,
	all_ids bigint[] not null,
    
    api text not null
);