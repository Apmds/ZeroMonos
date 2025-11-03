package pt.ua.tqs.hw1.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.RestAssured;
import pt.ua.tqs.hw1.data.RequestRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RequestControllerIT {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private RequestRepository repository;

    @BeforeEach
    void setup() {
        RestAssured.port = randomServerPort;
    }

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    
}