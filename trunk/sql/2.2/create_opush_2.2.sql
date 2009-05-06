DROP TABLE IF EXISTS sync_state;
DROP TABLE IF EXISTS id_mapping;
DROP TABLE IF EXISTS device;

CREATE TABLE device (
       id 		SERIAL PRIMARY KEY,
       identifier 	VARCHAR(255) NOT NULL,
       owner		INTEGER REFERENCES userobm(userobm_id) ON DELETE CASCADE,
       type		VARCHAR(64) NOT NULL
);

CREATE TABLE id_mapping (
       device_id	INTEGER NOT NULL REFERENCES device(id) ON DELETE CASCADE,
       client_id	VARCHAR(255) NOT NULL,
       server_id	VARCHAR(255) NOT NULL
);

CREATE TABLE sync_state (
       sync_key		VARCHAR(64) UNIQUE NOT NULL,
       collection	VARCHAR(255) NOT NULL,
       device_id	INTEGER NOT NULL REFERENCES device(id) ON DELETE CASCADE,
       last_sync	TIMESTAMP NOT NULL
);
ALTER TABLE ONLY sync_state ADD CONSTRAINT 
unique_col_dev UNIQUE (collection, device_id);
