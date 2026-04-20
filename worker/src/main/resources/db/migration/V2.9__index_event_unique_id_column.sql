ALTER TABLE public.event DROP CONSTRAINT IF EXISTS uid_event_unique_id;
ALTER TABLE public.event ADD CONSTRAINT uid_event_unique_id UNIQUE (event_unique_id);