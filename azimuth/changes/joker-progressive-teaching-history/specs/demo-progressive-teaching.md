# Intent delta: demo/progressive-teaching

## Add requirement: progressive-states-are-honest
Criticality: routine

The demo SHALL reproduce each progressive verification state with explicit provenance and SHALL
keep constructed states distinguishable from authentic executions.

### Add scenario: refund-failure-and-repair-are-replayable
GIVEN the correct refund application and assurance account exist
WHEN a presenter selects a prepared teaching state
THEN the expected machine or agent-tier result is reproduced without editing source live
AND the unsafe cold open records two financial effects only in the explicit demo mode
AND the final correct run records one financial effect across redelivery
AND authentic and constructed observations are labelled separately
