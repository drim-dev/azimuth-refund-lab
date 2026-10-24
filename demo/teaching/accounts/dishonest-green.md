# Constructed agent judgment: dishonest green tag

Verdict: `dishonest-tag`

Before judgment, the machine tier accepts the declared `component / universal / relational` tuple.
The test body only asserts a constant HTTP status and never repeats a delivery or observes the
payment provider's public operation history. Its declaration does not describe what it does.

The agent records a fingerprinted `dishonest-tag` judgment in the stage model. On the next check,
the deterministic tier consumes that artifact and reports `dishonest-tag-judged`. The CI failure is
therefore derived from the account; it is not inferred from the teaching stage's name.
