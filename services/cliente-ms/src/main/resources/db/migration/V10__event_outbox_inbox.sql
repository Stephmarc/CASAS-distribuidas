CREATE TABLE event_outbox(event_id varchar(36) PRIMARY KEY, aggregate_id varchar(36) NOT NULL, payload text NOT NULL,created_at timestamptz NOT NULL,published_at timestamptz,attempts integer NOT NULL DEFAULT 0,last_error text);
CREATE INDEX ix_outbox_pending ON event_outbox(created_at) WHERE published_at IS NULL;
CREATE TABLE event_inbox(event_id varchar(36) PRIMARY KEY,processed_at timestamptz NOT NULL);

CREATE TABLE event_failures(failure_id varchar(150) PRIMARY KEY,payload text NOT NULL,created_at timestamptz NOT NULL,replayed_at timestamptz);
