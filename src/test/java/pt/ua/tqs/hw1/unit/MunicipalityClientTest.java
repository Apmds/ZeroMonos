package pt.ua.tqs.hw1.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import pt.ua.tqs.hw1.service.MunicipalityClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MunicipalityClientTest {

    @Spy
    private MunicipalityClient client;

    // Generated with GPT
    @BeforeEach
    void resetStaticCache() throws Exception {
        Field field = MunicipalityClient.class.getDeclaredField("municipalities");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void lazyLoading() throws IOException {
        // Generated with GPT
        doAnswer(invocation -> {
            Field field = MunicipalityClient.class.getDeclaredField("municipalities");
            field.setAccessible(true);
            field.set(null, List.of("Aveiro", "Porto"));
            return null;
        }).when(client).loadMunicipalities();

        List<String> firstCall = client.getMunicipalities();
        verify(client, times(1)).loadMunicipalities();

        List<String> secondCall = client.getMunicipalities();
        verify(client, times(1)).loadMunicipalities();

        assertEquals(List.of("Aveiro", "Porto"), firstCall);
        assertEquals(firstCall, secondCall);
    }

    @Test
    void emptyListOnIOException() throws IOException {
        doThrow(new IOException("Network Error")).when(client).loadMunicipalities();

        List<String> result = client.getMunicipalities();
        assertTrue(result.isEmpty());
        verify(client, times(1)).loadMunicipalities();
    }
}