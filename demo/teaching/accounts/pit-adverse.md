# Authentic application hardener observation: PIT timeouts

Verdict: `review-required`

The checked-in PIT observation was produced against the current provider subjects. Eleven mutation
minions timed out while restarting the Spring/Testcontainers component harness. The adapter retains
them as `TIMED_OUT` review items even though PIT's aggregate display counts timeouts as detected.
This is an adverse and inconclusive observation about harness suitability. It is neither a
surviving-mutant story nor positive evidence for the product claim.

The separate teaching fixture does produce an authentic survivor. It is deliberately smaller so
that PIT can run the same mutation once with a weak response oracle and once with a direct
financial-effect oracle.
