# Verification: Rename refund execution model package

Regenerate the union linkage from refund-api, refund-worker, payment-provider, rider BFF, rider web
and system-test sources. Run `azimuth check` with the retained PIT challenge.

For each area named by a realization obligation, remove one representative relation in a temporary
copy of the generated manifest and verify that `azimuth check` reports
`missing-required-realization` for that exact area. This checks the machine boundary without
editing production source or preserving a manufactured failure.

No surface negative test applies because neither split claim is a site-domain invariant and the
workspace declares no surface.
