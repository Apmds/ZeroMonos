package pt.ua.tqs.hw1.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pt.ua.tqs.hw1.data.RequestRepository;
import pt.ua.tqs.hw1.data.RequestState;
import pt.ua.tqs.hw1.data.RequestStateChange;
import pt.ua.tqs.hw1.data.ServiceRequest;

@Service
public class RequestService {

    @Autowired
    private RequestRepository repository;

    private static final int MAX_REQUESTS_PER_DAY_AND_PLACE = 2;

    public ServiceRequest getRequest(long token) {
        Optional<ServiceRequest> opRequest = repository.findByToken(token);
        if (opRequest.isEmpty()) {
            throw new RequestNotFoundException();
        }

        return opRequest.get();
    }

    public ServiceRequest submitRequest(ServiceRequest request) {
        // The system can't accept dates before today
        if (request.getDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestDateException();
        }
        
        // The system can't accept dates in the weekend
        DayOfWeek weekday = request.getDate().getDayOfWeek();
        if (weekday == DayOfWeek.SATURDAY || weekday == DayOfWeek.SUNDAY) {
            throw new InvalidRequestDateException();
        }
        
        // The system can't accept times after 17:00h
        if (request.getDate().getHour() > 17) {
            throw new InvalidRequestDateException();
        }
        
        // During the week, the system can only have 2 requests per municipality
        LocalDate date = request.getDate().toLocalDate();
        List<ServiceRequest> samePlaceAndTime = repository.findByDateBetweenAndMunicipality(date.atStartOfDay(), date.atTime(LocalTime.MAX), request.getMunicipality());
        if (samePlaceAndTime.size() >= MAX_REQUESTS_PER_DAY_AND_PLACE) {
            throw new RequestOverflowException();
        }

        return repository.save(request);
    }
    
    public List<ServiceRequest> getRequests() {
        return repository.findAll();
    }

    public List<ServiceRequest> getRequests(String municipality) {
        return repository.findByDateAfterAndMunicipality(LocalDateTime.now(), municipality);
    }
    
    public List<RequestStateChange> getStateChanges(long token) {
        ServiceRequest request = getRequest(token);

        // Order by timestamp
        return request.getStateChanges().stream().sorted((r1, r2)-> r1.getDate().compareTo(r2.getDate())).toList();
    }

    public ServiceRequest cancelRequest(long token) {
        ServiceRequest request = getRequest(token);
        if (request.getState() != RequestState.RECIEVED && request.getState() != RequestState.ASSIGNED) {
            throw new InvalidStateTransitionException();
        }
        request.setState(RequestState.CANCELLED);

        return repository.save(request);
    }

    public ServiceRequest assignRequest(long token) {
        ServiceRequest request = getRequest(token);
        if (request.getState() != RequestState.RECIEVED) {
            throw new InvalidStateTransitionException();
        }
        request.setState(RequestState.ASSIGNED);

        return repository.save(request);
    }

    public ServiceRequest startRequest(long token) {
        ServiceRequest request = getRequest(token);
        if (request.getState() != RequestState.ASSIGNED) {
            throw new InvalidStateTransitionException();
        }
        request.setState(RequestState.IN_PROGRESS);

        return repository.save(request);
    }

    public ServiceRequest completeRequest(long token) {
        ServiceRequest request = getRequest(token);
        if (request.getState() != RequestState.IN_PROGRESS) {
            throw new InvalidStateTransitionException();
        }
        request.setState(RequestState.DONE);
        
        return repository.save(request);
    }
}