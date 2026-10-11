-- Adds the columns used by event start time / venue and by "change password"
-- (tokens issued before users.password_changed_at are rejected).
--
-- All columns are nullable, so existing rows keep working. Run it BEFORE
-- deploying the backend version that uses them.
--
-- Safe to run more than once. Run it in Supabase: SQL Editor -> New query -> Run.

ALTER TABLE events ADD COLUMN IF NOT EXISTS event_time TIME;
ALTER TABLE events ADD COLUMN IF NOT EXISTS venue      VARCHAR(200);

ALTER TABLE users  ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMP WITH TIME ZONE;
