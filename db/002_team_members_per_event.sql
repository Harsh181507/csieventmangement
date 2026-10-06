-- Students may be in one team PER EVENT, but older versions of the backend
-- created a UNIQUE constraint on team_members.user_id, which stops a student
-- from ever joining a team in a second event.
--
-- 1) Check whether that constraint exists:
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'team_members'::regclass AND contype = 'u';

-- 2) If step 1 shows a UNIQUE constraint on (user_id) only, drop it.
--    Replace the name below with the conname from step 1 and run:
-- ALTER TABLE team_members DROP CONSTRAINT <conname>;

-- 3) Keep a student from being added to the same team twice:
CREATE UNIQUE INDEX IF NOT EXISTS uq_team_members_team_user ON team_members (team_id, user_id);
