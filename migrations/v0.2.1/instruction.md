Migrating v0.2.0 to v.0.2.1
===========================

Migrating database
------------------

- Add column contestStartTime (bigint(20), NOT NULL) with default values of 0 to uriel_contest_team.
- Rename column contestEnterTime to contestStartTime in uriel_contest_contestant.
