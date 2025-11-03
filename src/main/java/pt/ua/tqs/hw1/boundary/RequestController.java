package pt.ua.tqs.hw1.boundary;

import org.springframework.web.bind.annotation.RestController;
import pt.ua.tqs.hw1.data.ServiceRequest;
import pt.ua.tqs.hw1.service.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RequestController {

    private static final Logger log = LoggerFactory.getLogger(RequestController.class);
    private static final String ERROR_KEY = "error"; 
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    private ResponseEntity<Object> requestNotFoundResponse() {
        log.warn("Request not found.");
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Object> invalidTokenResponse() {
        log.warn("Invalid token format received.");
        return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "invalid token"));
    }

    private ResponseEntity<Object> handleRequestOperation(String token, Function<Long, Object> operation, String invalidStateMsg) {
        try {
            var result = operation.apply(Long.valueOf(token));
            return ResponseEntity.ok(result);
        } catch (RequestNotFoundException e) {
            log.warn("Request with token={} not found", token);
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        } catch (InvalidStateTransitionException e) {
            log.warn("Invalid state transition for request {}", token);
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, invalidStateMsg));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<Object> submitRequest(@RequestBody ServiceRequest request) {
        log.info("/submit POST request received");
        try {
            ServiceRequest saved = requestService.submitRequest(request);
            log.info("Request successfully created with token={} and state={}", saved.getToken(), saved.getState());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (InvalidRequestDateException e) {
            log.warn("Invalid request date: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "the request date is invalid"));
        } catch (RequestOverflowException e) {
            log.warn("Request overflow: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "too many requests for this day and place have already been booked"));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ServiceRequest>> getRequests() {
        log.info("/requests GET request");
        List<ServiceRequest> requests = requestService.getRequests();
        log.info("Returning {} total requests", requests.size());
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @GetMapping("/requests/municipalities/{municipality}")
    public ResponseEntity<List<ServiceRequest>> getRequestsInMunicipality(@PathVariable String municipality) {
        log.info("/requests/municipalities/municipality GET request");
        List<ServiceRequest> requests = requestService.getRequests(municipality);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<Object> getRequest(@PathVariable("id") String token) {
        log.info("/requests/id GET request");
        try {
            ServiceRequest req = requestService.getRequest(Long.valueOf(token));
            log.info("Request found with token={}, state={}", req.getToken(), req.getState());
            return ResponseEntity.ok(req);
        } catch (RequestNotFoundException e) {
            log.warn("Request with token={} not found", token);
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        }
    }

    @GetMapping("/requests/{id}/states")
    public ResponseEntity<Object> getRequestStates(@PathVariable("id") String token) {
        log.info("/requests/id/states GET request");
        try {
            var states = requestService.getStateChanges(Long.valueOf(token));
            return ResponseEntity.ok(states);
        } catch (RequestNotFoundException e) {
            log.warn("Request with token={} not found for state history", token);
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        }
    }

    @PutMapping("/requests/{id}/cancel")
    public ResponseEntity<Object> cancelRequest(@PathVariable("id") String token) {
        log.info("/requests/id/cancel PUT request");
        return handleRequestOperation(token, requestService::cancelRequest, "request isn't in a valid state to be cancelled");
    }

    @PutMapping("/requests/{id}/assign")
    public ResponseEntity<Object> assignRequest(@PathVariable("id") String token) {
        log.info("/requests/id/assign PUT request");
        return handleRequestOperation(token, requestService::assignRequest, "request isn't in a valid state to be assigned");
    }

    @PutMapping("/requests/{id}/start")
    public ResponseEntity<Object> startRequest(@PathVariable("id") String token) {
        log.info("/requests/id/start PUT request");
        return handleRequestOperation(token, requestService::startRequest, "request isn't in a valid state to start");
    }

    @PutMapping("/requests/{id}/end")
    public ResponseEntity<Object> completeRequest(@PathVariable("id") String token) {
        log.info("/requests/id/end PUT request");
        return handleRequestOperation(token, requestService::completeRequest, "request isn't in a valid state to end");
    }
}
