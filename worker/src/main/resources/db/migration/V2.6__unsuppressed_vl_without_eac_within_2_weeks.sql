CREATE TABLE IF NOT EXISTS public.unsuppressed_vl_without_eac_within_2_weeks (
                                                              id UUID NOT NULL DEFAULT uuid_generate_v4(),
                                                              event_id UUID NULL,
                                                              missed_eac_flag BOOLEAN NULL,
                                                              date_14_days_post_hvl timestamp NULL,
                                                              CONSTRAINT unsuppressed_vl_without_eac_within_2_weeks_pkey PRIMARY KEY (id)
);
ALTER TABLE public.unsuppressed_vl_without_eac_within_2_weeks DROP CONSTRAINT IF EXISTS fk_unsuppressed_vl_without_eac_within_2_weeks_event_id;
ALTER TABLE public.unsuppressed_vl_without_eac_within_2_weeks ADD CONSTRAINT fk_unsuppressed_vl_without_eac_within_2_weeks_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);