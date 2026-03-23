CREATE TABLE IF NOT EXISTS public.missed_vl_opportunities (
                                                id UUID NOT NULL DEFAULT uuid_generate_v4(),
                                                event_id UUID NULL,
                                                missed_vl_flag BOOLEAN NULL,
                                                visit_date timestamp NULL,
                                                CONSTRAINT missed_vl_opportunities_pkey PRIMARY KEY (id)
);
ALTER TABLE public.missed_vl_opportunities DROP CONSTRAINT IF EXISTS fk_missed_vl_opportunities_event_id;
ALTER TABLE public.missed_vl_opportunities ADD CONSTRAINT fk_missed_vl_opportunities_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);