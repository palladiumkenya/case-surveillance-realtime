CREATE TABLE IF NOT EXISTS public.prep_uptake (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    event_id UUID NULL,
    prep_number VARCHAR(100) NULL,
    prep_status VARCHAR(100) NULL,
    prep_type VARCHAR(100) NULL,
    prep_regimen VARCHAR(250) NULL,
    prep_start_date timestamp NULL,
    reason_for_starting_prep TEXT NULL,
    reason_for_switching_prep TEXT NULL,
    date_switched_prep TEXT NULL,
    pregnancy_status VARCHAR(100) NULL,
    breastfeeding_status VARCHAR(100) NULL,
    CONSTRAINT prep_uptake_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.prep_uptake DROP CONSTRAINT IF EXISTS fk_prep_uptake_event_id;
ALTER TABLE public.prep_uptake ADD CONSTRAINT fk_prep_uptake_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);