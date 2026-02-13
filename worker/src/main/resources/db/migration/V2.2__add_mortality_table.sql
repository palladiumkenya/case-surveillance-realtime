CREATE TABLE IF NOT EXISTS public.mortality (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    event_id UUID NULL,
    cause_of_death VARCHAR(100) NULL,
    death_date timestamp NULL,
    CONSTRAINT mortality_pkey PRIMARY KEY (id)
);
ALTER TABLE public.mortality DROP CONSTRAINT IF EXISTS fk_mortality_event_id;
ALTER TABLE public.mortality ADD CONSTRAINT fk_mortality_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);