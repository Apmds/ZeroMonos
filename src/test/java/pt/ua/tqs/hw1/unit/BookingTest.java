package pt.ua.tqs.hw1.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import pt.ua.tqs.hw1.data.RequestRepository;
import pt.ua.tqs.hw1.data.RequestState;
import pt.ua.tqs.hw1.data.ServiceRequest;
import pt.ua.tqs.hw1.service.InvalidRequestDateException;
import pt.ua.tqs.hw1.service.RequestOverflowException;
import pt.ua.tqs.hw1.service.RequestService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BookingTest {

    @Mock
    private RequestRepository repository;

    @InjectMocks
    private RequestService service;

    @Test
    public void normalBooking() {
        // Get next monday
        LocalDate now = LocalDate.now();
        LocalDate mondayDate = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime dateTime = LocalDateTime.of(mondayDate, LocalTime.of(14, 30));
        
        ServiceRequest request = new ServiceRequest(dateTime, "Matress removal request", RequestState.RECIEVED, "Almada");

        // This has the token set
        ServiceRequest returnRequest = new ServiceRequest(dateTime, "Matress removal request", RequestState.RECIEVED, "Almada");
        returnRequest.setToken(55);

        when(repository.save(request)).thenReturn(returnRequest);

        assertThat(service.submitRequest(request).getToken()).isEqualTo(55);
        verify(repository, times(1)).save(request);
    }

    @Test
    public void noBookingToBeforeNow() {
        ServiceRequest request = new ServiceRequest(LocalDateTime.now().minusDays(1), "Matress removal request", RequestState.RECIEVED, "Almada");
        
        assertThrows(InvalidRequestDateException.class, () -> service.submitRequest(request));
    }

    @Test
    public void noBookingToWeekends() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekendDate = now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        ServiceRequest request = new ServiceRequest(weekendDate, "Matress removal request", RequestState.RECIEVED, "Almada");
        
        assertThrows(InvalidRequestDateException.class, () -> service.submitRequest(request));
    }

    @Test
    public void noBookingAfter17() {
        // Next thursday after 17h00
        LocalDateTime afterHoursDate = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY)), LocalTime.of(18, 0, 0));

        ServiceRequest request = new ServiceRequest(afterHoursDate, "Matress removal request", RequestState.RECIEVED, "Almada");

        assertThrows(InvalidRequestDateException.class, () -> service.submitRequest(request));
    }

    @Test
    public void requestOverflow() {
        LocalDate now = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));

        // Only 2 requests per place and day
        when(repository.findByDateBetweenAndMunicipality(now.atStartOfDay(), now.atTime(LocalTime.MAX), "Arouca"))
        .thenReturn(Arrays.asList(
            new ServiceRequest(now.atTime(13, 52), "O meu antigo frigorífico.", RequestState.RECIEVED, "Arouca"),
            new ServiceRequest(now.atTime(8, 40), "Um colchão cheio de ratos.", RequestState.RECIEVED, "Arouca")));

        ServiceRequest request = new ServiceRequest(now.atTime(10, 30), "Caixa de sapatos muito grande", RequestState.RECIEVED, "Arouca");

        assertThrows(RequestOverflowException.class, () -> service.submitRequest(request));
        verify(repository, times(1)).findByDateBetweenAndMunicipality(any(), any(), any());
    }
}