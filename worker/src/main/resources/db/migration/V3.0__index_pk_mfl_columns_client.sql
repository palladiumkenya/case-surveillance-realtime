ALTER TABLE public.client DROP CONSTRAINT IF EXISTS uid_client_mfl_pk;
ALTER TABLE public.client ADD CONSTRAINT uid_client_mfl_pk UNIQUE (patient_pk, mfl_code);