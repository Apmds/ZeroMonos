
package pt.ua.tqs.hw1.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import pt.ua.tqs.hw1.data.RequestRepository;
import pt.ua.tqs.hw1.data.RequestState;
import pt.ua.tqs.hw1.data.RequestStateChange;
import pt.ua.tqs.hw1.data.ServiceRequest;
import pt.ua.tqs.hw1.service.InvalidStateTransitionException;
import pt.ua.tqs.hw1.service.RequestService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StateTransitionTest {

    @Mock
    private RequestRepository repository;

    @InjectMocks
    private RequestService service;

    @BeforeEach
    void setup() {
        // When save is called, it returns the argument
        when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void stateChangeRecording() {
        ServiceRequest request = new ServiceRequest();
        request.setState(RequestState.RECIEVED);
        request.setToken(1);

        when(repository.findByToken(1L)).thenReturn(Optional.of(request));

        int before = request.getStateChanges().size();

        ServiceRequest result = service.cancelRequest(1L);

        int after = result.getStateChanges().size();

        assertThat(after).isEqualTo(before + 1);

        RequestStateChange change = result.getStateChanges().stream().reduce((f, s) -> s).orElse(null);

        assertThat(change).isNotNull();
        assertThat(result.getState()).isEqualTo(RequestState.CANCELLED);
        assertThat(change.getServiceRequest()).isEqualTo(result);
    }

    @Test
    void cancelRequestSetsState() {
        ServiceRequest request = new ServiceRequest();
        request.setState(RequestState.RECIEVED);
        request.setToken(1);

        ServiceRequest request2 = new ServiceRequest();
        request2.setState(RequestState.ASSIGNED);
        request.setToken(2);
        
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        when(repository.findByToken(2)).thenReturn(Optional.of(request2));


        ServiceRequest result = service.cancelRequest(1);
        ServiceRequest result2 = service.cancelRequest(2);

        assertThat(result.getState()).isEqualTo(RequestState.CANCELLED);
        assertThat(result2.getState()).isEqualTo(RequestState.CANCELLED);
        verify(repository, times(2)).save(any());
    }

    @Test
    void cancelRequestInvalidState() {
        ServiceRequest request = new ServiceRequest();
        request.setToken(1);
        
        request.setState(RequestState.IN_PROGRESS);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.cancelRequest(1));

        request.setState(RequestState.CANCELLED);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.cancelRequest(1));

        request.setState(RequestState.DONE);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.cancelRequest(1));

        verify(repository, never()).save(any());
    }

    @Test
    void assignRequestSetsState() {
        ServiceRequest request = new ServiceRequest();
        request.setState(RequestState.RECIEVED);
        request.setToken(1);
        
        when(repository.findByToken(1)).thenReturn(Optional.of(request));

        when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceRequest result = service.assignRequest(1);

        assertThat(result.getState()).isEqualTo(RequestState.ASSIGNED);
        verify(repository, times(1)).save(request);
    }

    @Test
    void assignRequestInvalidState() {
        ServiceRequest request = new ServiceRequest();
        request.setToken(1);
        
        request.setState(RequestState.IN_PROGRESS);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.assignRequest(1));

        request.setState(RequestState.CANCELLED);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.assignRequest(1));

        request.setState(RequestState.DONE);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.assignRequest(1));

        request.setState(RequestState.ASSIGNED);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.assignRequest(1));

        verify(repository, never()).save(any());
    }

    @Test
    void startRequestSetsState() {
        ServiceRequest request = new ServiceRequest();
        request.setState(RequestState.ASSIGNED);
        request.setToken(1);
        
        when(repository.findByToken(1)).thenReturn(Optional.of(request));

        when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceRequest result = service.startRequest(1);

        assertThat(result.getState()).isEqualTo(RequestState.IN_PROGRESS);
        verify(repository, times(1)).save(request);
    }

    @Test
    void startRequestInvalidState() {
        ServiceRequest request = new ServiceRequest();
        request.setToken(1);
        
        request.setState(RequestState.RECIEVED);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.startRequest(1));

        request.setState(RequestState.IN_PROGRESS);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.startRequest(1));

        request.setState(RequestState.DONE);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.startRequest(1));

        request.setState(RequestState.CANCELLED);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.startRequest(1));

        verify(repository, never()).save(any());
    }

    @Test
    void completeRequestSetsState() {
        ServiceRequest request = new ServiceRequest();
        request.setState(RequestState.IN_PROGRESS);
        request.setToken(1);
        
        when(repository.findByToken(1)).thenReturn(Optional.of(request));

        when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceRequest result = service.completeRequest(1);

        assertThat(result.getState()).isEqualTo(RequestState.DONE);
        verify(repository, times(1)).save(request);
    }

    @Test
    void compleeteRequestInvalidState() {
        ServiceRequest request = new ServiceRequest();
        request.setToken(1);
        
        request.setState(RequestState.RECIEVED);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.completeRequest(1));

        request.setState(RequestState.ASSIGNED);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.completeRequest(1));

        request.setState(RequestState.DONE);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.completeRequest(1));

        request.setState(RequestState.CANCELLED);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));
        assertThrows(InvalidStateTransitionException.class, () -> service.completeRequest(1));

        verify(repository, never()).save(any());
    }
}