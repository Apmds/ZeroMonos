package pt.ua.tqs.hw1.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import pt.ua.tqs.hw1.data.RequestRepository;
import pt.ua.tqs.hw1.data.RequestState;
import pt.ua.tqs.hw1.data.RequestStateChange;
import pt.ua.tqs.hw1.data.ServiceRequest;

import static java.lang.invoke.MethodHandles.lookup;

@Service
public class RequestService {

    static final Logger log = LoggerFactory.getLogger(lookup().lookupClass());

    private RequestRepository repository;

    public RequestService(RequestRepository repository) {
        this.repository = repository;
    }

    private static final int MAX_REQUESTS_PER_DAY_AND_PLACE = 2;

    public ServiceRequest getRequest(long token) {
        log.info("Fetching request with token {}", token);
        Optional<ServiceRequest> opRequest = repository.findByToken(token);
        if (opRequest.isEmpty()) {
            log.warn("Request with token {} not found", token);
            throw new RequestNotFoundException();
        }

        log.info("Found request with token {} and state {}", token, opRequest.get().getState());
        return opRequest.get();
    }

    public ServiceRequest submitRequest(ServiceRequest request) {
        log.info("Submitting new request for municipality {} at {}", request.getMunicipality(), request.getDate());

        // The system can't accept dates before today
        if (request.getDate().isBefore(LocalDateTime.now())) {
            log.warn("Rejected request: date {} is before now", request.getDate());
            throw new InvalidRequestDateException();
        }

        // The system can't accept dates in the weekend
        DayOfWeek weekday = request.getDate().getDayOfWeek();
        if (weekday == DayOfWeek.SATURDAY || weekday == DayOfWeek.SUNDAY) {
            log.warn("Rejected request: date {} falls on weekend ({})", request.getDate(), weekday);
            throw new InvalidRequestDateException();
        }

        // The system can't accept times after 17:00h
        if (request.getDate().getHour() > 17) {
            log.warn("Rejected request: time {} is after 17:00h", request.getDate().toLocalTime());
            throw new InvalidRequestDateException();
        }

        // During the week, the system can only have 2 requests per municipality
        LocalDate date = request.getDate().toLocalDate();
        List<ServiceRequest> samePlaceAndTime = repository.findByDateBetweenAndMunicipality(date.atStartOfDay(), date.atTime(LocalTime.MAX), request.getMunicipality());

        if (samePlaceAndTime.size() >= MAX_REQUESTS_PER_DAY_AND_PLACE) {
            log.warn("Rejected request: municipality {} already has {} requests on {}", request.getMunicipality(), samePlaceAndTime.size(), date);
            throw new RequestOverflowException();
        }

        ServiceRequest saved = repository.save(request);
        log.info("Request successfully saved with token {}", saved.getToken());
        return saved;
    }

    public List<ServiceRequest> getRequests() {
        log.info("Fetching all requests");
        return repository.findAll();
    }

    public List<ServiceRequest> getRequests(String municipality) {
        log.info("Fetching requests for municipality {}", municipality);
        return repository.findByDateAfterAndMunicipality(LocalDateTime.now(), municipality);
    }

    public List<RequestStateChange> getStateChanges(long token) {
        log.info("Fetching state change history for token {}", token);
        ServiceRequest request = getRequest(token);

        List<RequestStateChange> changes = request.getStateChanges().stream().sorted((r1, r2) -> r1.getDate().compareTo(r2.getDate())).toList();

        log.info("Found {} state changes for token {}", changes.size(), token);
        return changes;
    }

    public ServiceRequest cancelRequest(long token) {
        log.info("Attempting to cancel request with token {}", token);
        ServiceRequest request = getRequest(token);
        if (request.getState() != RequestState.RECIEVED && request.getState() != RequestState.ASSIGNED) {
            log.warn("Invalid state transition: cannot cancel request {} in state {}", token, request.getState());
            throw new InvalidStateTransitionException();
        }
        request.setState(RequestState.CANCELLED);
        ServiceRequest saved = repository.save(request);
        log.info("Request {} successfully cancelled", token);
        return saved;
    }

    public ServiceRequest assignRequest(long token) {
        log.info("Attempting to assign request with token {}", token);
        ServiceRequest request = getRequest(token);
        if (request.getState() != RequestState.RECIEVED) {
            log.warn("Invalid state transition: cannot assign request {} in state {}", token, request.getState());
            throw new InvalidStateTransitionException();
        }
        request.setState(RequestState.ASSIGNED);
        ServiceRequest saved = repository.save(request);
        log.info("Request {} successfully assigned", token);
        return saved;
    }

    public ServiceRequest startRequest(long token) {
        log.info("Attempting to start request with token {}", token);
        ServiceRequest request = getRequest(token);
        if (request.getState() != RequestState.ASSIGNED) {
            log.warn("Invalid state transition: cannot start request {} in state {}", 
                     token, request.getState());
            throw new InvalidStateTransitionException();
        }
        request.setState(RequestState.IN_PROGRESS);
        ServiceRequest saved = repository.save(request);
        log.info("Request {} started successfully", token);
        return saved;
    }

    public ServiceRequest completeRequest(long token) {
        log.info("Attempting to complete request with token {}", token);
        ServiceRequest request = getRequest(token);
        if (request.getState() != RequestState.IN_PROGRESS) {
            log.warn("Invalid state transition: cannot complete request {} in state {}", token, request.getState());
            throw new InvalidStateTransitionException();
        }
        request.setState(RequestState.DONE);
        ServiceRequest saved = repository.save(request);
        log.info("Request {} completed successfully", token);
        return saved;
    }
}
