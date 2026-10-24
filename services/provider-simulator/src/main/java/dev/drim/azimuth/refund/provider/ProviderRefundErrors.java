package dev.drim.azimuth.refund.provider;

import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

final class ProviderRefundConflictException extends RuntimeException {
    ProviderRefundConflictException(String idempotencyKey) {
        super("Idempotency-Key %s was already used with different request fields"
                .formatted(idempotencyKey));
    }
}

final class InvalidProviderRefundException extends RuntimeException {
    InvalidProviderRefundException(String message) {
        super(message);
    }
}

final class ProviderRefundNotFoundException extends RuntimeException {
    ProviderRefundNotFoundException(String idempotencyKey) {
        super("Provider refund for idempotency key %s was not found".formatted(idempotencyKey));
    }
}

@RestControllerAdvice
final class ProviderRefundErrors {
    @ExceptionHandler(ProviderRefundConflictException.class)
    ProblemDetail conflict(ProviderRefundConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());
        problem.setTitle("Provider refund conflict");
        problem.setType(URI.create("urn:problem:provider:refund:apply:conflict"));
        return problem;
    }

    @ExceptionHandler(InvalidProviderRefundException.class)
    ProblemDetail invalid(InvalidProviderRefundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage());
        problem.setTitle("Invalid provider refund");
        problem.setType(URI.create("urn:problem:provider:refund:apply:invalid"));
        return problem;
    }

    @ExceptionHandler(ProviderRefundNotFoundException.class)
    ProblemDetail notFound(ProviderRefundNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage());
        problem.setTitle("Provider refund not found");
        problem.setType(URI.create("urn:problem:provider:refund:get:not-found"));
        return problem;
    }
}
