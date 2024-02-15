create table perf_mon_data (
	id bigserial primary key,
    svc_host text not null,
    eventid text not null,
    sessionid text not null,
    event_type smallint not null,
    event_name text not null,
    event_ts bigint not null,
    proc_timems smallint not null,
    errcode int not null,
    reqsize int default (-1),
    respsize int default (-1),
	crtd_ts timestamp not null default current_timestamp
);
-- create index eventts_idx on perf_mon_data (eventts);
-- create index eventtype_idx on perf_mon_data (eventtype);

create table svc_mon_data (
	id serial primary key,
    svc_host text not null,
    svc_type smallint not null,
    eventid text not null,
    svc_name text not null,
    event_ts bigint not null,
    exthost	text not null,
    errcode int not null,
    errdetail text not null,
	crtd_ts timestamp not null default current_timestamp
);

-- create index eventts_idx on perf_mon_data (eventts);
-- create index svchost_idx on perf_mon_data (svchost);

