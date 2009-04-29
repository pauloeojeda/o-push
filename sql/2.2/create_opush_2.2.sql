DROP TABLE IF EXISTS sync_state;
DROP TABLE IF EXISTS id_mapping;
DROP TABLE IF EXISTS device;

CREATE TABLE device (
       id 		SERIAL PRIMARY KEY,
       identifier 	VARCHAR(255) NOT NULL,
       owner		INTEGER REFERENCES userobm(userobm_id),
       type		VARCHAR(64) NOT NULL
);

CREATE TABLE id_mapping (
       device_id	INTEGER NOT NULL REFERENCES device(id),
       client_id	VARCHAR(64) NOT NULL,
       server_id	VARCHAR(64) NOT NULL
);

CREATE TABLE sync_state (
       sync_key		VARCHAR(64) UNIQUE NOT NULL,
       device_id	INTEGER UNIQUE NOT NULL REFERENCES device(id),
       last_sync	TIMESTAMP NOT NULL
);
