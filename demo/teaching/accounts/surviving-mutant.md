# Authentic teaching hardener observation: surviving mutant

Verdict: `toothless`

PIT removes the equality condition guarding the financial-effect increment. The response-only
oracle executes once and still passes, so the mutation is `SURVIVED`. The direct oracle executes
the same mutation and reads the effect count after two deliveries; that report is `KILLED`.

These are authentic executions over the bounded teaching fixture. They challenge the oracle shape;
they are not represented as mutation results from the Spring/PostgreSQL application.
