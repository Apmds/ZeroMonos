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
    // TODO: logging

    private static final String ERROR_KEY = "error"; 
    private RequestService requestService;

    private ResponseEntity<Object> requestNotFoundResponse() {
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Object> invalidTokenResponse() {
        return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "invalid token"));
    }

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Object> submitRequest(@RequestBody ServiceRequest request) {
        HttpStatus status = HttpStatus.CREATED;
        try {
            ServiceRequest saved = requestService.submitRequest(request);
            return new ResponseEntity<>(saved, status);
        } catch (InvalidRequestDateException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "the request date is invalid"));
        } catch (RequestOverflowException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "too many requests for this day and place have already been booked"));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ServiceRequest>> getRequests() {
        return new ResponseEntity<>(requestService.getRequests(), HttpStatus.OK);
    }

    @GetMapping("/requests/municipalities/{municipality}")
    public ResponseEntity<List<ServiceRequest>> getRequestsInMunicipality(@PathVariable(value = "municipality") String municipality) {
        return new ResponseEntity<>(requestService.getRequests(municipality), HttpStatus.OK);
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<Object> getRequest(@PathVariable(value = "id") String token) {

        try {
            return ResponseEntity.ok(requestService.getRequest(Long.valueOf(token)));
        } catch (RequestNotFoundException e) {
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        }
    }

    @GetMapping("/requests/{id}/states")
    public ResponseEntity<Object> getRequestStates(@PathVariable(value = "id") String token) {
        try {
            return ResponseEntity.ok(requestService.getStateChanges(Long.valueOf(token)));
        } catch (RequestNotFoundException e) {
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        }
    }

    @PutMapping("/requests/{id}/cancel")
    public ResponseEntity<Object> cancelRequest(@PathVariable(value = "id") String token) {
        try {
            return ResponseEntity.ok(requestService.cancelRequest(Long.valueOf(token)));
        } catch (RequestNotFoundException e) {
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        } catch (InvalidStateTransitionException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "request isn't in a valid state to be cancelled"));
        }
    }

    @PutMapping("/requests/{id}/assign")
    public ResponseEntity<Object> assignRequest(@PathVariable(value = "id") String token) {
        try {
            return ResponseEntity.ok(requestService.assignRequest(Long.valueOf(token)));
        } catch (RequestNotFoundException e) {
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        } catch (InvalidStateTransitionException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "request isn't in a valid state to be assigned"));
        }
    }

    @PutMapping("/requests/{id}/start")
    public ResponseEntity<Object> startRequest(@PathVariable(value = "id") String token) {
        try {
            return ResponseEntity.ok(requestService.startRequest(Long.valueOf(token)));
        } catch (RequestNotFoundException e) {
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        } catch (InvalidStateTransitionException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "request isn't in a valid state to start"));
        }
    }

    @PutMapping("/requests/{id}/end")
    public ResponseEntity<Object> completeRequest(@PathVariable(value = "id") String token) {
        try {
            return ResponseEntity.ok(requestService.completeRequest(Long.valueOf(token)));
        } catch (RequestNotFoundException e) {
            return requestNotFoundResponse();
        } catch (NumberFormatException e) {
            return invalidTokenResponse();
        } catch (InvalidStateTransitionException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_KEY, "request isn't in a valid state to end"));
        }
    }
}