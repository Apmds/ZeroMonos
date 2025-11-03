package pt.ua.tqs.hw1.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;



@Service
public class MunicipalityClient {

    private static final Logger log = LoggerFactory.getLogger(MunicipalityClient.class);

    // Lazy loaded to save API requests
    private static List<String> municipalities;

    private static final String API_URL = "https://json.geoapi.pt/municipios";

    // HTTP request (wanted this to be private, but mockito requires it to be public)
    public void loadMunicipalities() throws IOException {
        log.info("Attempting to load municipalities from API: {}", API_URL);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(API_URL);
            log.debug("Executing GET request to {}", API_URL);

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                log.info("Received response from API with status code {}", statusCode);

                if (statusCode != 200) {
                    log.warn("Unexpected status code {} while fetching municipalities", statusCode);
                }

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    log.debug("Response body successfully read ({} bytes)", json.length());

                    // Map json to list
                    ObjectMapper mapper = new ObjectMapper();
                    municipalities = mapper.readValue(json,
                            mapper.getTypeFactory().constructCollectionType(List.class, String.class));

                    log.info("Successfully parsed {} municipalities from API", municipalities.size());
                } else {
                    log.warn("No entity found in API response");
                }
            }
        } catch (IOException e) {
            log.error("Failed to load municipalities from API: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<String> getMunicipalities() {
        // Lazy loading
        if (municipalities == null) {
            log.info("Municipality list not loaded yet, loading now...");
            try {
                loadMunicipalities();
            } catch (IOException e) {
                log.error("Error while loading municipalities: {}", e.getMessage(), e);
                return Collections.emptyList();
            }
        } else {
            log.debug("Returning cached list of {} municipalities", municipalities.size());
        }

        return municipalities;
    }
}
