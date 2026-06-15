ALTER TABLE public.event ADD COLUMN initial_load_date timestamp NULL;

-- Backfill existing rows with the current load_date as their initial load date
UPDATE public.event SET initial_load_date = load_date WHERE initial_load_date IS NULL;
