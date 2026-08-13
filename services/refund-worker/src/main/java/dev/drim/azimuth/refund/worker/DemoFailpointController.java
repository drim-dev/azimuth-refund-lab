package dev.drim.azimuth.refund.worker;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("demo")
@RequestMapping("/demo/failpoints/after-provider-response")
class DemoFailpointController {
    private final DemoDeliveryFailpoint failpoint;

    DemoFailpointController(DemoDeliveryFailpoint failpoint) {
        this.failpoint = failpoint;
    }

    @GetMapping
    DemoDeliveryFailpoint.FailpointStatus status() {
        return failpoint.status();
    }

    @PostMapping("/arm")
    ResponseEntity<DemoDeliveryFailpoint.FailpointStatus> arm() {
        return ResponseEntity.accepted().body(failpoint.arm());
    }

    @PostMapping("/release")
    ResponseEntity<DemoDeliveryFailpoint.FailpointStatus> release() {
        return ResponseEntity.accepted().body(failpoint.release());
    }

    @ExceptionHandler(DemoDeliveryFailpoint.FailpointStateConflict.class)
    ResponseEntity<String> conflict(DemoDeliveryFailpoint.FailpointStateConflict exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }
}
