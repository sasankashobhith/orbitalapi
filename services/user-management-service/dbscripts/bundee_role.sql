DROP TABLE IF EXISTS public."masterpermission" CASCADE;
DROP TABLE IF EXISTS public."rolepermission" CASCADE;


CREATE TABLE IF NOT EXISTS public."rolepermission"
(
    roleid int not null references masterroles(roleid),
    permission_name text not null
);

create unique index rolepermission_unq_idx on rolepermission (roleid, permission_name);

INSERT INTO public.masterroles (roleid, rolename, isactive) VALUES (1, 'Bundee Admin', true);

INSERT INTO public.rolepermission (roleid, permission_name) VALUES (1, 'CREATE_NEW_API_KEY');
INSERT INTO public.rolepermission (roleid, permission_name) VALUES (1, 'LIST_ALL_API_KEY');
INSERT INTO public.rolepermission (roleid, permission_name) VALUES (1, 'ENABLE_API_KEY');
INSERT INTO public.rolepermission (roleid, permission_name) VALUES (1, 'DISABLE_API_KEY');
INSERT INTO public.rolepermission (roleid, permission_name) VALUES (1, 'ASSIGN_ROLES_TO_API_KEY');
INSERT INTO public.rolepermission (roleid, permission_name) VALUES (1, 'GEN_API_KEY_TOKEN');