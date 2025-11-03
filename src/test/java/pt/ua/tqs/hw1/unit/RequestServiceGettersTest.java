package pt.ua.tqs.hw1.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import pt.ua.tqs.hw1.service.RequestNotFoundException;
import pt.ua.tqs.hw1.service.RequestService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestServiceGettersTest {

    @Mock
    private RequestRepository repository;

    @InjectMocks
    private RequestService service;

    @Test
    void getRequest() {
        ServiceRequest request = new ServiceRequest();
        request.setToken(1);

        when(repository.findByToken(1)).thenReturn(Optional.of(request));

        ServiceRequest result = service.getRequest(1);

        assertThat(request).isEqualTo(result);
        verify(repository, times(1)).findByToken(1);
    }

    @Test
    void getRequestNonExistent() {
        when(repository.findByToken(anyLong())).thenReturn(Optional.empty());

        assertThrows(RequestNotFoundException.class, () -> service.getRequest(1L));
        verify(repository, times(1)).findByToken(1);
    }

    @Test
    void getRequests() {
        ServiceRequest req1 = new ServiceRequest(); req1.setToken(1);
        ServiceRequest req2 = new ServiceRequest(); req1.setToken(2);
        ServiceRequest req3 = new ServiceRequest(); req1.setToken(3);
        List<ServiceRequest> list = Arrays.asList(req1, req2, req3);

        when(repository.findAll()).thenReturn(list);

        List<ServiceRequest> result = service.getRequests();

        assertThat(result.size()).isEqualTo(3);
        assertThat(result).containsExactlyInAnyOrder(req1, req2, req3);
        verify(repository, times(1)).findAll();
    }

    @Test
    void getStateChanges_shouldReturnSortedByDate() {
        RequestStateChange c1 = new RequestStateChange(LocalDateTime.of(2025, 1, 1, 10, 0), RequestState.RECIEVED); // Second
        RequestStateChange c2 = new RequestStateChange(LocalDateTime.of(2025, 1, 1, 9, 0), RequestState.RECIEVED); // First
        RequestStateChange c3 = new RequestStateChange(LocalDateTime.of(2025, 1, 1, 11, 0), RequestState.RECIEVED); // Third
        Set<RequestStateChange> unordered = new HashSet<>(Set.of(c1, c2, c3));

        ServiceRequest request = new ServiceRequest();
        request.setToken(1);
        request.setStateChanges(unordered);
        when(repository.findByToken(1)).thenReturn(Optional.of(request));

        List<RequestStateChange> result = service.getStateChanges(1);

        // Assert
        assertThat(result.size()).isEqualTo(3);
        assertThat(result).containsExactly(c2, c1, c3);
    }
}