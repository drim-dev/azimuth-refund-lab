# Constructed judgment lifecycle: stale

Verdict before change: `sound`

The fixture records a sound judgment fingerprint, then supplies a linkage input with a different
source fingerprint. `azimuth judge` therefore reports the old verdict as stale. No test has failed;
the diagnostic says only that the earlier reasoning no longer applies to the changed account.
