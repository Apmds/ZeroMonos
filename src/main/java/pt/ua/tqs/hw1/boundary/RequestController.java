package pt.ua.tqs.hw1.boundary;

import org.springframework.web.bind.annotation.RestController;

import pt.ua.tqs.hw1.data.ServiceRequest;
import pt.ua.tqs.hw1.service.InvalidRequestDateException;
import pt.ua.tqs.hw1.service.InvalidStateTransitionException;
import pt.ua.tqs.hw1.service.RequestNotFoundException;
import pt.ua.tqs.hw1.service.RequestOverflowException;
import pt.ua.tqs.hw1.service.RequestService;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
public class RequestController {
    
    private static final Logger log = LoggerFactory.getLogger(RequestController.class);

    private static final String ERROR_KEY = "error"; 
    private final RequestService requestService;

    private ResponseEntity<Object> requestNotFoundResponse() {
        log.warn("Request not found.");
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Object> invalidTokenResponse() {
        log.warn("Invalid token format received.");
        return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "invalid token"));
    }

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Object> submitRequest(@RequestBody ServiceRequest request) {
        log.info("/submit POST request received with description='{}', municipality='{}', date={}", 
                 request.getDescription(), request.getMunicipality(), request.getDate());
        
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
    public ResponseEntity<List<ServiceRequest>> getRequestsInMunicipality(@PathVariable(value = "municipality") String municipality) {
        log.info("/requests/municipalities/{} GET request", municipality);
        List<ServiceRequest> requests = requestService.getRequests(municipality);
        log.info("Found {} requests in municipality '{}'", requests.size(), municipality);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<Object> getRequest(@PathVariable(value = "id") String token) {
        log.info("/requests/{} GET request", token);

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
    public ResponseEntity<Object> getRequestStates(@PathVariable(value = "id") String token) {
        log.info("/requests/{}/states GET request", token);
        
        try {
            var states = requestService.getStateChanges(Long.valueOf(token));
            log.info("Returning {} state changes for request {}", states.size(), token);
            return ResponseEntity.ok(states);
        } catch (RequestNotFoundException e) {
            log.warn("Request with token={} not found for state history", token);
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        }
    }

    @PutMapping("/requests/{id}/cancel")
    public ResponseEntity<Object> cancelRequest(@PathVariable(value = "id") String token) {
        log.info("/requests/{}/cancel PUT request", token);
        
        try {
            var result = requestService.cancelRequest(Long.valueOf(token));
            log.info("Request {} successfully cancelled", token);
            return ResponseEntity.ok(result);
        } catch (RequestNotFoundException e) {
            log.warn("Attempted to cancel non-existent request with token={}", token);
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        } catch (InvalidStateTransitionException e) {
            log.warn("Invalid state transition while cancelling request {}", token);
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "request isn't in a valid state to be cancelled"));
        }
    }

    @PutMapping("/requests/{id}/assign")
    public ResponseEntity<Object> assignRequest(@PathVariable(value = "id") String token) {
        log.info("/requests/{}/assign PUT request", token);
        
        try {
            var result = requestService.assignRequest(Long.valueOf(token));
            log.info("Request {} successfully assigned", token);
            return ResponseEntity.ok(result);
        } catch (RequestNotFoundException e) {
            log.warn("Attempted to assign non-existent request with token={}", token);
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        } catch (InvalidStateTransitionException e) {
            log.warn("Invalid state transition while assigning request {}", token);
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "request isn't in a valid state to be assigned"));
        }
    }

    @PutMapping("/requests/{id}/start")
    public ResponseEntity<Object> startRequest(@PathVariable(value = "id") String token) {
        log.info("/requests/{}/start PUT request", token);
        
        try {
            var result = requestService.startRequest(Long.valueOf(token));
            log.info("Request {} successfully started", token);
            return ResponseEntity.ok(result);
        } catch (RequestNotFoundException e) {
            log.warn("Attempted to start non-existent request with token={}", token);
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        } catch (InvalidStateTransitionException e) {
            log.warn("Invalid state transition while starting request {}", token);
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "request isn't in a valid state to start"));
        }
    }

    @PutMapping("/requests/{id}/end")
    public ResponseEntity<Object> completeRequest(@PathVariable(value = "id") String token) {
        log.info("/requests/{}/end PUT request", token);
        
        try {
            var result = requestService.completeRequest(Long.valueOf(token));
            log.info("Request {} successfully completed", token);
            return ResponseEntity.ok(result);
        } catch (RequestNotFoundException e) {
            log.warn("Attempted to complete non-existent request with token={}", token);
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        } catch (InvalidStateTransitionException e) {
            log.warn("Invalid state transition while completing request {}", token);
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "request isn't in a valid state to end"));
        }
    }
}
