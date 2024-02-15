DROP TABLE masterapikeys CASCADE;
DROP TABLE apikeyrolemapping CASCADE;

CREATE TABLE IF NOT EXISTS public."masterapikeys"
(
    apikeyid serial primary key,
    apikeyname text not null unique,
	isactive boolean not null default true,
    validityts bigint not null,
	createdby int default 0,
	updatedby int default 0,
	crtd_ts timestamptz not null default current_timestamp,
	updtd_ts timestamptz
);

CREATE TABLE IF NOT EXISTS public."apikeyrolemapping"
(
	apikeyid int not null,
	roleid int not null,
	isactive boolean not null default true,
	createdby int default 0,
	updatedby int default 0
);
alter table apikeyrolemapping add primary key (apikeyid, roleid);

insert into masterapikeys (apikeyname, isactive, validityts, createdby, updatedby) values ('bundee internal', true, 2556124190, 0, 0);

INSERT INTO public.apikeyrolemapping (apikeyid, roleid, isactive, createdby, updatedby) VALUES (1, 1, true, 0, 0);