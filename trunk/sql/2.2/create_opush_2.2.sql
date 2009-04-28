CREATE TABLE device (
       id 		SERIAL PRIMARY KEY,
       identifier 	VARCHAR(255) NOT NULL,
       login		VARCHAR(255) NOT NULL,
       type		VARCHAR(64) NOT NULL
);

CREATE TABLE id_mapping (
       device_id	INTEGER NOT NULL REFERENCES device(id),
       client_id	VARCHAR(64) NOT NULL,
       server_id	VARCHAR(64) NOT NULL
);

CREATE TABLE sync_state (
       device_id	INTEGER NOT NULL REFERENCES device(id),
       sync_key		VARCHAR(64) NOT NULL,
       last_sync	TIMESTAMP NOT NULL
);
