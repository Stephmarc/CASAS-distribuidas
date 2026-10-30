ALTER TABLE event_outbox ADD COLUMN dispatch_seq bigserial;
ALTER TABLE event_outbox ADD COLUMN next_attempt_at timestamptz;
WITH ordered AS (SELECT event_id,row_number() OVER(ORDER BY created_at,event_id) seq FROM event_outbox)
UPDATE event_outbox o SET dispatch_seq=ordered.seq FROM ordered WHERE o.event_id=ordered.event_id;
SELECT setval(pg_get_serial_sequence('event_outbox','dispatch_seq'),COALESCE((SELECT max(dispatch_seq) FROM event_outbox),0)+1,false);
CREATE UNIQUE INDEX ux_outbox_sequence ON event_outbox(dispatch_seq);
CREATE INDEX ix_outbox_aggregate_pending ON event_outbox(aggregate_id,dispatch_seq) WHERE published_at IS NULL;
