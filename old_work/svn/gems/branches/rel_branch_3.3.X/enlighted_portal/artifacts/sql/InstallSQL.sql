


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
    status character varying
);


ALTER TABLE public.users OWNER TO postgres;
--
-- Name:  customer ; type table
--
CREATE SEQUENCE customer_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.customer_seq OWNER TO postgres;
 
CREATE TABLE customer (
    id bigint NOT NULL,
    name character varying NOT NULL,
    address  character varying,
    email character varying,
    contact character varying
   
);
 

ALTER TABLE public.customer OWNER TO postgres;



CREATE SEQUENCE license_detail_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.license_detail_seq OWNER TO postgres;
 

CREATE TABLE license_detail (
    id bigint NOT NULL,
    customer_id bigint NOT NULL,
    level character varying NOT NULL,
    start_date character varying NOT NULL,
    end_date character varying NOT NULL,
    created_on date,
    mac_id character varying NOT NULL,
    api_key bytea,
    status character varying
   
);
ALTER TABLE public.license_detail OWNER TO postgres;


CREATE TABLE user_audit (
    id bigint NOT NULL,
    username character varying NOT null,
    action_type character varying NOT NULL,   
	log_time timestamp without time zone not null,
    description character varying,
    ip_address character varying NOT null DEFAULT ''
);

ALTER TABLE ONLY user_audit
    ADD CONSTRAINT user_audit_pk PRIMARY KEY (id);
ALTER TABLE public.user_audit OWNER TO postgres;

CREATE SEQUENCE user_audit_seq
    INCREMENT BY 1
    MAXVALUE 999999999999999999
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.user_audit_seq OWNER TO postgres;

--
-- Name: role_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT role_pk PRIMARY KEY (id);




ALTER TABLE ONLY users
    ADD CONSTRAINT users_pk PRIMARY KEY (id);

    
    ALTER TABLE ONLY customer
    ADD CONSTRAINT customer_pk PRIMARY KEY (id);
    
    ALTER TABLE ONLY license_detail
    ADD CONSTRAINT license_pk PRIMARY KEY (id);


-- Name: user_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT user_role_fk FOREIGN KEY (role_id) REFERENCES roles(id);



ALTER TABLE ONLY license_detail
    ADD CONSTRAINT customer_id_fk FOREIGN KEY (customer_id) REFERENCES customer(id);
    


--
-- Adding Default values
--

INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Admin');

INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'NormalUser');


INSERT INTO users (id, email, "password", first_name, last_name, created_on, role_id,status) VALUES (nextval('user_seq'), 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Administrator', '', '2012-01-19', 1,'A');
