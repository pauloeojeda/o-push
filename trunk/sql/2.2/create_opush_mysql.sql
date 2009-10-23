DROP TABLE IF EXISTS opush_sync_mail;
DROP TABLE IF EXISTS opush_sync_perms;
DROP TABLE IF EXISTS opush_sec_policy;
DROP TABLE IF EXISTS opush_sync_state;
DROP TABLE IF EXISTS opush_id_mapping;
DROP TABLE IF EXISTS opush_folder_mapping;
DROP TABLE IF EXISTS opush_device;

CREATE TABLE opush_device (
       id 		SERIAL PRIMARY KEY,
       identifier 	VARCHAR(255) NOT NULL,
       owner		INTEGER REFERENCES userobm(userobm_id) ON DELETE CASCADE,
       type		VARCHAR(64) NOT NULL
);

CREATE TABLE opush_folder_mapping (
       id		SERIAL PRIMARY KEY,
       device_id	INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
       collection	VARCHAR(255) NOT NULL
);

-- store last sync dates
CREATE TABLE opush_sync_state (
	sync_key	VARCHAR(64) UNIQUE NOT NULL,
	collection_id   INTEGER NOT NULL REFERENCES opush_folder_mapping(id) ON DELETE CASCADE,
	device_id	INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
	last_sync	TIMESTAMP NOT NULL
);
ALTER TABLE opush_sync_state ADD CONSTRAINT 
unique_opush_col_dev UNIQUE (collection_id, device_id);

CREATE TABLE opush_sec_policy (
       id				SERIAL PRIMARY KEY,
       device_password_enabled		BOOLEAN DEFAULT FALSE
       -- add other fields fields...
);

-- A row exists if a user is allowed to Sync.
CREATE TABLE opush_sync_perms (
       owner		INTEGER REFERENCES userobm(userobm_id) ON DELETE CASCADE,
       device_id	INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
       -- add not null later
       policy		INTEGER REFERENCES opush_sec_policy(id) ON DELETE SET NULL
);


CREATE TABLE opush_sync_mail (
	collection_id   INTEGER NOT NULL REFERENCES opush_folder_mapping(id) ON DELETE CASCADE,
	device_id	INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
	mail_uid	INTEGER NOT NULL
);
