package pt.ua.tqs.hw1.integration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import pt.ua.tqs.hw1.data.RequestRepository;
import pt.ua.tqs.hw1.data.RequestState;
import pt.ua.tqs.hw1.data.ServiceRequest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

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

    @Test
    void submitRequest() {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)), LocalTime.of(15, 30));

        ServiceRequest request = new ServiceRequest(dateTime, "Description", RequestState.RECIEVED, "Beja");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/submit")
        .then()
            .statusCode(201)
            .body("date", containsString(dateTime.toString()))
            .body("state", equalTo("RECIEVED"));

        List<ServiceRequest> allRequests = repository.findAll();

        assertThat(allRequests).extracting(ServiceRequest::getDate).containsExactly(dateTime);
    }

    @Test
    void getRequests() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime dateTime = LocalDateTime.of(nextMonday, LocalTime.of(15, 30));
        LocalDateTime dateTime2 = LocalDateTime.of(nextMonday, LocalTime.of(16, 30));
        LocalDateTime dateTime3 = LocalDateTime.of(nextMonday, LocalTime.of(11, 14));

        repository.save(new ServiceRequest(dateTime, "Help with my items", RequestState.RECIEVED, "Lisboa"));
        repository.save(new ServiceRequest(dateTime2, "Make my fridge go away", RequestState.RECIEVED, "Amarante"));
        repository.save(new ServiceRequest(dateTime3, "Help me please", RequestState.RECIEVED, "Chamusca"));

        repository.flush();

        given()
        .when()
            .get("/api/requests")
        .then()
            .statusCode(200)
            .body("description", contains("Help with my items", "Make my fridge go away", "Help me please"))
            .body("municipality", contains("Lisboa", "Amarante", "Chamusca"));
    }

    @Test
    void getRequestsInMunicipality() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime dateTime = LocalDateTime.of(nextMonday, LocalTime.of(15, 30));
        LocalDateTime dateTime2 = LocalDateTime.of(nextMonday, LocalTime.of(16, 30));
        LocalDateTime dateTime3 = LocalDateTime.of(nextMonday, LocalTime.of(11, 14));

        repository.save(new ServiceRequest(dateTime, "Help with my items", RequestState.RECIEVED, "Aveiro"));
        repository.save(new ServiceRequest(dateTime2, "Make my fridge go away", RequestState.RECIEVED, "Vila Viçosa"));
        repository.save(new ServiceRequest(dateTime3, "Help me please", RequestState.RECIEVED, "Aveiro"));

        repository.flush();

        given()
        .when()
            .get("/api/requests/municipalities/{municipality}", "Aveiro")
        .then()
            .statusCode(200)
            .body("description", contains("Help with my items", "Help me please"))
            .body("municipality", everyItem(equalTo("Aveiro")));
    }
}