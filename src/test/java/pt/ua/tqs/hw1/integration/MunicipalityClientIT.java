package pt.ua.tqs.hw1.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import pt.ua.tqs.hw1.service.MunicipalityClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MunicipalityClientIT {

    @Spy
    private MunicipalityClient client;

    @Test
    void lazyLoading() throws IOException {
        List<String> firstCall = client.getMunicipalities();
        verify(client, times(1)).loadMunicipalities();

        List<String> secondCall = client.getMunicipalities();
        verify(client, times(1)).loadMunicipalities();

        assertEquals(List.of("Aveiro", "Porto"), firstCall);
        assertEquals(firstCall, secondCall);
    }
}