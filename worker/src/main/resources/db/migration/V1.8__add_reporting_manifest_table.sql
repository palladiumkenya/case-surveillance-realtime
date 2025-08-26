CREATE TABLE IF NOT EXISTS public.reporting_manifest (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    mfl_code varchar(100) NOT NULL,
    report_date date NOT NULL,
    CONSTRAINT reporting_manifest_pkey PRIMARY KEY (id),
    CONSTRAINT reporting_manifest_mfl_code_report_date UNIQUE (mfl_code, report_date)
    );