# Assurance lifecycle replay

This directory demonstrates the optional assurance-service boundary with the accepted model in
`artifacts/assurance/project-snapshot.json`. The repository snapshot is the authority for the
claim contract; the files here define execution-time lifecycle records for that exact contract.

Run the deterministic, zero-service rehearsal with:

```sh
./scripts/assurance-replay
```

The default mode validates and prints `expected-trace.json`. That trace is a **recorded protocol
expectation**, not evidence that a test, mutation run, or production observation executed. It is
safe for an offline conference rehearsal because it performs no network calls.

Run the same sequence through a local Azimuth assurance API with:

```sh
ASSURANCE_URL=http://127.0.0.1:8080 ./scripts/assurance-replay --live
```

Live mode registers the real exported model snapshot, four structured definition versions, three
qualifications, six observations, and the finding from the repository's compact PIT observation.
It evaluates exact merge and production subjects at fixed logical times and checks these
transitions:

1. a qualified, satisfied, current observation opens the merge gate;
2. the same observation closes the gate when its declared lifetime expires;
3. a newer violated observation closes it with focused diagnostic work;
4. a later satisfied observation opens it again for the same exact revision;
5. the recorded PIT finding closes it even though the direct observation is satisfied;
6. changing the definition semantics closes it because the prior qualification is stale;
7. a bounded canary observation opens only for its exact artifact, deployment and cohort, rejects
   an altered subject, then closes at the explicit end of its ten-minute validity window;
8. a provider-ledger reconciliation observation opens the operation gate when each succeeded
   refund has one matching row, then a later duplicate-or-wrong-amount finding closes it.

No sleeps are involved. The `at` value in each gate request advances a logical clock. Replaying
the immutable inputs is safe: the service returns the existing record when its id and content are
identical, while gate evaluations append decision history by design.

The PIT challenge is linked to `tooling/pit/provider-refund-observation.json`, whose current result
is `findings` because eleven selected mutants timed out before executing tests. The replay does not
turn that adverse result green. The later satisfied direct observation and the adverse challenge
coexist, demonstrating why observations and challenges remain separate inputs to a gate.

The canary subject names all deployment identity dimensions supported by the protocol: source
revision, artifact digest, deployment id, environment and cohort. Its required context names the
ten-minute evaluation window and the observation's `expiresAt` is the machine-enforced boundary.
The gate therefore cannot reuse that observation for another build, deployment or cohort, and it
closes without sleeping when evaluation reaches the expiry timestamp.

The reconciliation definition is an operational detector rather than a test case. It relates the
refund API's succeeded projection to the provider ledger and requires exactly one row with the
approved amount and currency. A result is `violated` whether it finds a duplicate row or a wrong
amount; the live replay records that adverse result and derives `diagnose-violation` work. The
checked-in observations remain protocol fixtures, not a claim that production was monitored.
