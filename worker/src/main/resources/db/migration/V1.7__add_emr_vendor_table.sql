CREATE TABLE IF NOT EXISTS public.emr_vendor (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    vendor_name varchar(100) NOT NULL,
    CONSTRAINT emr_vendor_pkey PRIMARY KEY (id)
    );

INSERT INTO public.emr_vendor (vendor_name)
VALUES ('KenyaEMR'),
       ('AMRS'),
       ('ECare');

ALTER TABLE public.event ADD COLUMN emr_vendor_id UUID NULL;
ALTER TABLE public.event ADD COLUMN emr_version varchar(150) NULL;
ALTER TABLE public.event DROP CONSTRAINT IF EXISTS fk_event_emr_vendor_id;
ALTER TABLE public.event ADD CONSTRAINT fk_event_emr_vendor_id FOREIGN KEY (emr_vendor_id) REFERENCES public.emr_vendor(id);