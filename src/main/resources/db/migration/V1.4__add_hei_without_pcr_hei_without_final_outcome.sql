CREATE TABLE IF NOT EXISTS public.hei_without_pcr (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    event_id uuid NULL,
    hei_id varchar(250) NULL,
    CONSTRAINT hei_without_pcr_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.hei_without_pcr DROP CONSTRAINT IF EXISTS fk_hei_without_pcr_event_id;
ALTER TABLE public.hei_without_pcr ADD CONSTRAINT fk_hei_without_pcr_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);

CREATE TABLE IF NOT EXISTS public.hei_without_final_outcome (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    event_id uuid NULL,
    hei_id varchar(250) NULL,
    CONSTRAINT hei_without_final_outcome_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.hei_without_final_outcome DROP CONSTRAINT IF EXISTS fk_hei_without_final_outcome_event_id;
ALTER TABLE public.hei_without_final_outcome ADD CONSTRAINT fk_hei_without_final_outcome_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);