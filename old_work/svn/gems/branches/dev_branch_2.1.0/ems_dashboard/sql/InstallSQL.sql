

--
-- Name: energy_consumption; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE energy_consumption (
    id bigint NOT NULL,
    gems_id bigint NOT NULL,
    tenants_id bigint,
    power_used numeric(19,2),
    base_power_used numeric(19,2),
    cost double precision,
    base_cost double precision,
    occ_saving numeric(19,2) DEFAULT 0,
    tuneup_saving numeric(19,2) DEFAULT 0,
    ambient_saving numeric(19,2) DEFAULT 0,
    manual_saving numeric(19,2) DEFAULT 0,
    totalfixturecontributed bigint,
    price double precision,
    saved_power_used numeric(19,2),
    saved_cost double precision,
    capture_at timestamp without time zone
);


ALTER TABLE public.energy_consumption OWNER TO postgres;

--
-- Name: energy_consumption_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE energy_consumption_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.energy_consumption_seq OWNER TO postgres;

--
-- Name: gems; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE gems (
    id bigint NOT NULL,
    name character varying(20) NOT NULL,
    gems_unique_address character varying,
    mac_id character varying,
    gems_ip_address character varying NOT NULL,
    port integer DEFAULT 443 NOT NULL,
    version bigint,
    api_key varchar(25),
    status character varying DEFAULT 'I'
);


ALTER TABLE public.gems OWNER TO postgres;

--
-- Name: gems_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE gems_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.gems_seq OWNER TO postgres;

--
-- Name: role_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE role_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.role_seq OWNER TO postgres;

--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE roles (
    id bigint NOT NULL,
    name character varying
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: tenant_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE tenant_seq
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 999999999999999999
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.tenant_seq OWNER TO postgres;

--
-- Name: tenants; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE tenants (
    id bigint NOT NULL,
    valid_domain_name character varying
);


ALTER TABLE public.tenants OWNER TO postgres;

--
-- Name: user_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE user_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.user_seq OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE users (
    id bigint NOT NULL,
    email character varying NOT NULL,
    password character varying NOT NULL,
    first_name character varying,
    last_name character varying,
    created_on date,
    role_id bigint,
    tenant_id bigint,
    status character varying
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: energy_consumption_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY energy_consumption
    ADD CONSTRAINT energy_consumption_pkey PRIMARY KEY (id);


--
-- Name: gems_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY gems
    ADD CONSTRAINT gems_pk PRIMARY KEY (id);


--
-- Name: role_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT role_pk PRIMARY KEY (id);


--
-- Name: tenants_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY tenants
    ADD CONSTRAINT tenants_pk PRIMARY KEY (id);


--
-- Name: uniqueipaddress; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY gems
    ADD CONSTRAINT uniqueipaddress UNIQUE (gems_ip_address);


--
-- Name: users_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pk PRIMARY KEY (id);


--
-- Name: energy_consumption_tenants_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY energy_consumption
    ADD CONSTRAINT energy_consumption_tenants_id FOREIGN KEY (tenants_id) REFERENCES tenants(id);


--
-- Name: user_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT user_role_fk FOREIGN KEY (role_id) REFERENCES roles(id);


--
-- Name: user_tenant_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT user_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenants(id);


--
-- Adding Default values
--

INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Admin');

INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'NormalUser');

INSERT INTO tenants (id) VALUES (1);

INSERT INTO users (id, email, "password", first_name, last_name, created_on, role_id,tenant_id,status) VALUES (nextval('user_seq'), 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Administrator', '', '2012-01-19', 1,1,'A');


--
-- API Key Column added for API Key Authentication process
--

ALTER TABLE gems
	ADD api_key varchar(25);
ALTER TABLE gems OWNER TO postgres;

--
-- Adding Uniuq IP Constraint
--
ALTER TABLE gems ADD CONSTRAINT uniqueipaddress UNIQUE (gems_ip_address);


--
-- Adding Status Column to indicate Active/InActive Status of the GEM
--

ALTER TABLE gems
	ADD status character varying DEFAULT 'I';
ALTER TABLE gems OWNER TO postgres;


