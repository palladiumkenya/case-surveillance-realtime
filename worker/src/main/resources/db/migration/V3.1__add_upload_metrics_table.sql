CREATE TABLE IF NOT EXISTS public.upload_metrics (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    site_code varchar(100) NOT NULL,
    event_type varchar(100) NOT NULL,
    record_count bigint NOT NULL,
    timestamp timestamp NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT upload_metrics_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS upload_metrics_site_code_timestamp_idx
    ON public.upload_metrics (site_code, timestamp);
