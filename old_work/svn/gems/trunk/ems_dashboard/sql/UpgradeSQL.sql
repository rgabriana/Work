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

--
-- Adding Unique constraint to Column to gems_id and capture_at to avoid duplicated records entry at master gems
--
ALTER TABLE energy_consumption ADD CONSTRAINT unique_energy_consumption UNIQUE (capture_at, gems_id);

--
-- Timestamp Column added for storing data specific to Mini Gems
--

ALTER TABLE gems
	ADD timezone character varying;
ALTER TABLE gems OWNER TO postgres;

