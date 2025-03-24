CREATE TABLE IF NOT EXISTS public.at_risk_pbfw (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    event_id uuid NULL,
    CONSTRAINT at_risk_pbfw_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.at_risk_pbfw DROP CONSTRAINT IF EXISTS fk_at_risk_pbfw_event_id;
ALTER TABLE public.at_risk_pbfw ADD CONSTRAINT fk_at_risk_pbfw_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);

CREATE TABLE IF NOT EXISTS public.prep_linked_at_risk_pbfw (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    event_id uuid NULL,
    prep_start_date timestamp NULL,
    prep_number varchar(100) NULL,
    prep_regimen varchar(250) NULL,
    CONSTRAINT prep_linked_at_risk_pbfw_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.prep_linked_at_risk_pbfw DROP CONSTRAINT IF EXISTS fk_prep_linked_at_risk_pbfw_event_id;
ALTER TABLE public.prep_linked_at_risk_pbfw ADD CONSTRAINT fk_prep_linked_at_risk_pbfw_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);