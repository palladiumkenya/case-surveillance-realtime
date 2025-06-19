CREATE TABLE IF NOT EXISTS public.eligible_for_vl(
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    event_id uuid NULL,
    positive_hiv_test_date timestamp NULL,
    visit_date timestamp NULL,
    art_start_date timestamp NULL,
    last_vl_order_date timestamp NULL,
    last_vl_results_date timestamp NULL,
    pregnancy_status varchar(100),
    breast_feeding_status varchar(100),
    last_vl_results varchar(250),
    CONSTRAINT eligible_for_vl_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.eligible_for_vl DROP CONSTRAINT IF EXISTS fk_eligible_for_vl_event_id;
ALTER TABLE public.eligible_for_vl ADD CONSTRAINT fk_eligible_for_vl_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);

CREATE TABLE IF NOT EXISTS public.unsuppressed_viral_load (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    event_id uuid NULL,
    positive_hiv_test_date timestamp NULL,
    visit_date timestamp NULL,
    art_start_date timestamp NULL,
    last_vl_order_date timestamp NULL,
    last_vl_results_date timestamp NULL,
    last_eac_encounter_date timestamp NULL,
    pregnancy_status varchar(100),
    breast_feeding_status varchar(100),
    last_vl_results varchar(250),
    CONSTRAINT unsuppressed_viral_load_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.unsuppressed_viral_load DROP CONSTRAINT IF EXISTS fk_unsuppressed_viral_load_event_id;
ALTER TABLE public.unsuppressed_viral_load ADD CONSTRAINT fk_unsuppressed_viral_load_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);