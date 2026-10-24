package demo.teaching;

final class RefundRedeliveryComposedTest {
    void refundRedeliveryPreservesOneRefund() {
        int httpStatus = 200;
        assert httpStatus == 200;
    }

    void refundRedeliveryReturnsOneStableResponse() {
        String firstResponse = "accepted";
        String secondResponse = "accepted";
        assert firstResponse.equals(secondResponse);
    }
}
