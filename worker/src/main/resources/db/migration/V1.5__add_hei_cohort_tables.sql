CREATE TABLE IF NOT EXISTS public.hei_aged_6_to_8_months (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    event_id uuid NULL,
    hei_id varchar(250) NULL,
    CONSTRAINT hei_aged_6_to_8_months_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.hei_aged_6_to_8_months DROP CONSTRAINT IF EXISTS fk_hei_aged_6_to_8_months_event_id;
ALTER TABLE public.hei_aged_6_to_8_months ADD CONSTRAINT fk_hei_aged_6_to_8_months_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);

CREATE TABLE IF NOT EXISTS public.hei_aged_24_months (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    event_id uuid NULL,
    hei_id varchar(250) NULL,
    CONSTRAINT hei_aged_24_months_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.hei_aged_24_months DROP CONSTRAINT IF EXISTS fk_hei_aged_24_months_event_id;
ALTER TABLE public.hei_aged_24_months ADD CONSTRAINT fk_hei_aged_24_months_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);