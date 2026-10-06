-- Removes duplicate unique constraints and indexes.
--
-- The schema was created more than once, so several tables enforce the SAME
-- rule two to four times (e.g. team_members has four identical UNIQUE
-- (team_id, user_id) constraints). Every insert/update has to maintain each
-- copy. One copy of every rule is kept, so nothing about what is allowed changes.
--
-- Safe to re-run. Run it in Supabase: SQL Editor -> New query -> Run.

BEGIN;

-- team_members: UNIQUE (team_id, user_id) x4  -> keep team_members_team_id_user_id_key
ALTER TABLE team_members DROP CONSTRAINT IF EXISTS team_members_unique_team_user;
ALTER TABLE team_members DROP CONSTRAINT IF EXISTS unique_team_user;
DROP INDEX IF EXISTS uq_team_members_team_user;
DROP INDEX IF EXISTS idx_team_members_team_id;            -- covered by (team_id, user_id)

-- event_judges: UNIQUE (event_id, judge_id) x3 -> keep event_judges_event_id_judge_id_key
ALTER TABLE event_judges DROP CONSTRAINT IF EXISTS event_judges_unique_event_judge;
ALTER TABLE event_judges DROP CONSTRAINT IF EXISTS unique_event_judge;
DROP INDEX IF EXISTS idx_event_judges_event_id;           -- covered by (event_id, judge_id)

-- judge_assignments: UNIQUE (team_id, judge_id) x2, plus (event_id, team_id, judge_id)
-- which (team_id, judge_id) already implies -> keep judge_assignments_team_id_judge_id_key
ALTER TABLE judge_assignments DROP CONSTRAINT IF EXISTS unique_team_judge;
ALTER TABLE judge_assignments DROP CONSTRAINT IF EXISTS judge_assignments_unique_team_judge;
DROP INDEX IF EXISTS idx_judge_assignments_team_id;       -- covered by (team_id, judge_id)

-- scores: UNIQUE (team_id, judge_id, criteria_id) x2 -> keep scores_team_id_judge_id_criteria_id_key
ALTER TABLE scores DROP CONSTRAINT IF EXISTS scores_unique_judge_team_criteria;
DROP INDEX IF EXISTS idx_scores_team_id;                  -- covered by (team_id, judge_id, criteria_id)

-- event_registrations: plain index that duplicates the unique constraint
DROP INDEX IF EXISTS idx_event_registrations_event_user;

COMMIT;

-- Planner statistics have never been collected for these tables
ANALYZE users, events, teams, team_members, judging_criteria, scores,
        event_judges, judge_assignments, event_registrations;
