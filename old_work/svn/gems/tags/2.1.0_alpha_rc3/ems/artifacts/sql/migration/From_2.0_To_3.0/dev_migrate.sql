---16th Nov 2011
UPDATE roles set name='Admin' where name='ADMIN';
UPDATE roles set name='Auditor' where name='AUDITOR';
UPDATE roles set name='Employee' where name='EMPLOYEE';
UPDATE roles set name='FacilitiesAdmin' where name='FACILITIES_ADMIN';
UPDATE roles set name='TenantAdmin' where name='TENANT_ADMIN';

---21st Nov 2011
ALTER TABLE tenants ADD COLUMN phone_no character varying;
ALTER TABLE tenants ADD COLUMN  status character varying;
ALTER TABLE tenants ADD COLUMN valid_domain character varying;

---22nd Nov 2011
--Set all the user status to active
update users set status='ACTIVE'; 

---02 Jan 2012
--- Create user with select priveleges only
CREATE USER debugems WITH PASSWORD 'debugems';

GRANT CONNECT ON DATABASE ems TO debugems;
GRANT USAGE ON SCHEMA public TO debugems;

SELECT 'GRANT SELECT ON ' || relname || ' TO debugems;'
FROM pg_class JOIN pg_namespace ON pg_namespace.oid = pg_class.relnamespace
WHERE nspname = 'public' AND relkind IN ('r', 'v');


---09-01-2012
ALTER TABLE company ADD tenant_id bigint;
ALTER TABLE company ADD CONSTRAINT fk_company_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE campus ADD tenant_id bigint;
ALTER TABLE campus ADD CONSTRAINT fk_campus_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE building ADD tenant_id bigint;
ALTER TABLE building ADD CONSTRAINT fk_building_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE floor ADD tenant_id bigint;
ALTER TABLE floor ADD CONSTRAINT fk_floor_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE area ADD tenant_id bigint;
ALTER TABLE area ADD CONSTRAINT fk_area_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);