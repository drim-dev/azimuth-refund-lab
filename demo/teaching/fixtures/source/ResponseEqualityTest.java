package demo.teaching;

final class ResponseEqualityTest {
    void repeatedResponsesAreEqual() {
        String firstResponse = "accepted";
        String secondResponse = "accepted";
        assert firstResponse.equals(secondResponse);
        // No authoritative ledger count is observed, so a duplicate charge can still pass.
    }
}
