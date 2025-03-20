CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE IF NOT EXISTS public.client (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    patient_pk varchar(256) NULL,
    sex varchar(10) NULL,
    ward varchar(256) NULL,
    county varchar(256) NULL,
    sub_county varchar(256) NULL,
    dob date NULL,
    CONSTRAINT client_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.event (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    client_id uuid NULL,
    event_type varchar(50) NULL,
    mfl_code varchar(50) NULL,
    load_date timestamp NULL,
    created_at timestamp NULL,
    updated_at timestamp NULL,
    CONSTRAINT event_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.event DROP CONSTRAINT IF EXISTS fk_event_client_id;
ALTER TABLE public.event ADD CONSTRAINT fk_event_client_id FOREIGN KEY (client_id) REFERENCES public.client(id);

CREATE TABLE IF NOT EXISTS public.new_case (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    event_id uuid NULL,
    positive_hiv_test_date timestamp NULL,
    CONSTRAINT new_case_pkey PRIMARY KEY (id)
    );
ALTER TABLE public.new_case DROP CONSTRAINT IF EXISTS fk_new_case_event_id;
ALTER TABLE public.new_case ADD CONSTRAINT fk_new_case_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);

