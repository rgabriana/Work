alter table em_instance alter COLUMN  name drop NOT NULL;
alter table em_instance alter COLUMN  customer_id drop NOT NULL;
alter table em_instance add column last_connectivity_at timestamp without time zone;
alter table em_instance add column active boolean default false;
alter table users add column salt character varying(255) NOT NULL default 'randomsalt';

update em_instance set active = true where active is null;
update users set (password, salt) = ('7c0345648dfb5b05cf5bedcf2c14b37f63f79421','randomsalt') where email = 'admin';
