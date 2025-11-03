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

import com.fasterxml.jackson.databind.ObjectMapper;

public class MunicipalityClient {

    // Lazy loaded to save API requests
    private static List<String> municipalities;

    private static final String API_URL = "https://json.geoapi.pt/municipios";

    // HTTP request (wanted this to be private, but mockito requires it to be public)
    public void loadMunicipalities() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(API_URL);
            CloseableHttpResponse response = client.execute(request);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);

                    // Map json to list
                    ObjectMapper mapper = new ObjectMapper();
                    municipalities = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                }

            } finally {
                response.close();
            }
        }
    }

    public List<String> getMunicipalities() {
        // Lazy loading
        if (municipalities == null) {
            try {
                loadMunicipalities();
            } catch (IOException e) {
                return Collections.emptyList();
            }
        }

        return municipalities;
    }
}