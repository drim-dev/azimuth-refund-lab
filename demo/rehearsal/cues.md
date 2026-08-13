# Live demo cues

## Enter the failure boundary

Run `scripts/demo-failure` only after `scripts/rehearsal readiness` reports a pristine fixture.
Keep the browser on the support console while the script runs. The intended visible state is:

- approved refund: pending;
- worker failpoint: paused;
- provider applications: 1;
- final trip cost after correction: EUR 90.

The financial authority is the provider ledger, not the pending rider projection. If any assertion
fails, stop and use the offline screenshots or recorded output; do not improvise repeated approval
or release requests.

## Show correction under redelivery

Run `scripts/demo-correct`. The intended visible state is:

- approved refund: succeeded;
- worker interruption count: 1;
- provider applications: still 1;
- rider receipt: captured EUR 120, correction EUR 30, final cost EUR 90.

The script name describes the correct journey. It does not patch source during the talk.
