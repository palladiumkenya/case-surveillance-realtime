ALTER TABLE public.event DROP COLUMN emr_version;

ALTER TABLE public.reporting_manifest ADD COLUMN emr_version varchar(150) NULL;