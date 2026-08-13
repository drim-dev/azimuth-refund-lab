package dev.drim.azimuth.refund.api.http;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiProblem.class)
    ProblemDetail handleApiProblem(ApiProblem exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(exception.status(), exception.getMessage());
        problem.setTitle(exception.title());
        problem.setType(URI.create("urn:refund-api:" + exception.code()));
        return problem;
    }

    public static final class ApiProblem extends RuntimeException {
        private final HttpStatus status;
        private final String title;
        private final String code;

        public ApiProblem(HttpStatus status, String title, String code, String detail) {
            super(detail);
            this.status = status;
            this.title = title;
            this.code = code;
        }

        public HttpStatus status() {
            return status;
        }

        public String title() {
            return title;
        }

        public String code() {
            return code;
        }
    }
}
