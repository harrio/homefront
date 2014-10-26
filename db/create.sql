CREATE TABLE sensor (
  sensor_id serial PRIMARY KEY,
  mac varchar(20) NOT NULL,
  name varchar(100) NOT NULL
);

CREATE TABLE probe (
  probe_id serial PRIMARY KEY,
  sensor_id integer REFERENCES sensor(sensor_id) NOT NULL,
  group_id integer REFERENCES probegroup(group_id) NOT NULL,
  key varchar(5) NOT NULL,
  name varchar(100) NOT NULL,
  humidity boolean NOT NULL
);

CREATE TABLE temperature (
  temp_id serial PRIMARY KEY,
  probe_id integer REFERENCES probe(probe_id) NOT NULL,
  value numeric(5, 2) NOT NULL,
  time timestamp WITH TIME ZONE NOT NULL DEFAULT NOW(),
  aggregation smallint
);

CREATE TABLE humidity (
  hum_id serial PRIMARY KEY,
  probe_id integer REFERENCES probe(probe_id) NOT NULL,
  value numeric(5, 2) NOT NULL,
  time timestamp WITH TIME ZONE NOT NULL DEFAULT NOW(),
  aggregation smallint
);

CREATE TABLE probegroup (
  group_id serial PRIMARY KEY,
  name varchar(100) NOT NULL,
  index integer NOT NULL
);
