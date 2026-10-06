-- Performance indexes for CSI Events.
-- PostgreSQL does not index foreign-key columns automatically, and every
-- screen in the app filters by one of these columns.
--
-- Safe to run more than once. Run it in Supabase: SQL Editor -> New query -> Run.

CREATE INDEX IF NOT EXISTS idx_teams_event_id               ON teams (event_id);
CREATE INDEX IF NOT EXISTS idx_teams_leader_id              ON teams (leader_id);

CREATE INDEX IF NOT EXISTS idx_team_members_team_id         ON team_members (team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user_id         ON team_members (user_id);

CREATE INDEX IF NOT EXISTS idx_scores_team_id               ON scores (team_id);
CREATE INDEX IF NOT EXISTS idx_scores_judge_id              ON scores (judge_id);
CREATE INDEX IF NOT EXISTS idx_scores_criteria_id           ON scores (criteria_id);

CREATE INDEX IF NOT EXISTS idx_judge_assignments_judge_event ON judge_assignments (judge_id, event_id);
CREATE INDEX IF NOT EXISTS idx_judge_assignments_team_id     ON judge_assignments (team_id);
CREATE INDEX IF NOT EXISTS idx_judge_assignments_event_id    ON judge_assignments (event_id);

CREATE INDEX IF NOT EXISTS idx_event_judges_judge_id        ON event_judges (judge_id);
CREATE INDEX IF NOT EXISTS idx_event_judges_event_id        ON event_judges (event_id);

CREATE INDEX IF NOT EXISTS idx_judging_criteria_event_id    ON judging_criteria (event_id);

CREATE INDEX IF NOT EXISTS idx_users_role                   ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_email_upper            ON users (upper(email));  -- matches Spring Data IgnoreCase

CREATE INDEX IF NOT EXISTS idx_events_event_date            ON events (event_date DESC, id DESC);
