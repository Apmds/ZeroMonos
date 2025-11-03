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

    @Test
    void getRequest() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime dateTime = LocalDateTime.of(nextMonday, LocalTime.of(15, 30));

        ServiceRequest request = repository.saveAndFlush(new ServiceRequest(dateTime, "Help me please", RequestState.RECIEVED, "Aveiro"));

        given()
        .when()
            .get("/api/requests/{id}", String.valueOf(request.getToken()))
        .then()
            .statusCode(200)
            .body("token", equalTo((int) request.getToken()))
            .body("description", equalTo("Help me please"));
    }

    @Test
    void getRequestStates() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime dateTime = LocalDateTime.of(nextMonday, LocalTime.of(15, 30));

        ServiceRequest request = repository.saveAndFlush(new ServiceRequest(dateTime, "Help me please", RequestState.RECIEVED, "Aveiro"));

        request.setState(RequestState.ASSIGNED);
        request.setState(RequestState.CANCELLED);

        request = repository.saveAndFlush(request);

        given()
        .when()
            .get("/api/requests/{id}/states", String.valueOf(request.getToken()))
        .then()
            .statusCode(200)
            .body("state", contains("ASSIGNED", "CANCELLED"));
    }

    @Test
    void cancelRequest() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime dateTime = LocalDateTime.of(nextMonday, LocalTime.of(15, 30));

        ServiceRequest request = repository.saveAndFlush(new ServiceRequest(dateTime, "Help me please", RequestState.RECIEVED, "Aveiro"));

        given()
        .when()
            .put("/api/requests/{id}/cancel", String.valueOf(request.getToken()))
        .then()
            .statusCode(200)
            .body("token", equalTo((int) request.getToken()))
            .body("state", equalTo("CANCELLED"));
    }

    @Test
    void assignRequest() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime dateTime = LocalDateTime.of(nextMonday, LocalTime.of(15, 30));

        ServiceRequest request = repository.saveAndFlush(new ServiceRequest(dateTime, "Help me please", RequestState.RECIEVED, "Aveiro"));

        given()
        .when()
            .put("/api/requests/{id}/assign", String.valueOf(request.getToken()))
        .then()
            .statusCode(200)
            .body("token", equalTo((int) request.getToken()))
            .body("state", equalTo("ASSIGNED"));
    }

    @Test
    void startRequest() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime dateTime = LocalDateTime.of(nextMonday, LocalTime.of(15, 30));

        ServiceRequest request = repository.saveAndFlush(new ServiceRequest(dateTime, "Help me please", RequestState.ASSIGNED, "Aveiro"));

        given()
        .when()
            .put("/api/requests/{id}/start", String.valueOf(request.getToken()))
        .then()
            .statusCode(200)
            .body("token", equalTo((int) request.getToken()))
            .body("state", equalTo("IN_PROGRESS"));
    }

    @Test
    void completeRequest() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDateTime dateTime = LocalDateTime.of(nextMonday, LocalTime.of(15, 30));

        ServiceRequest request = repository.saveAndFlush(new ServiceRequest(dateTime, "Help me please", RequestState.IN_PROGRESS, "Aveiro"));

        given()
        .when()
            .put("/api/requests/{id}/end", String.valueOf(request.getToken()))
        .then()
            .statusCode(200)
            .body("token", equalTo((int) request.getToken()))
            .body("state", equalTo("DONE"));
    }

    // Generated by chatGPT
    @Test
    void getRequest_withInvalidToken_returnsBadRequest() {
        given()
        .when()
            .get("/api/requests/{id}", "abc") // invalid number
        .then()
            .statusCode(400)
            .body("error", equalTo("invalid token"));
    }

    @Test
    void getRequest_withNonexistentRequest_returnsNotFound() {
        given()
        .when()
            .get("/api/requests/{id}", 999999)
        .then()
            .statusCode(404);
    }

    @Test
    void cancelRequest_withInvalidToken_returnsBadRequest() {
        given()
        .when()
            .put("/api/requests/{id}/cancel", "invalidToken")
        .then()
            .statusCode(400)
            .body("error", equalTo("invalid token"));
    }

    @Test
    void cancelRequest_withNonexistentRequest_returnsNotFound() {
        given()
        .when()
            .put("/api/requests/{id}/cancel", 12345)
        .then()
            .statusCode(404);
    }

    @Test
    void assignRequest_withInvalidState_returnsBadRequest() {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)), LocalTime.of(15, 30));

        // Request already cancelled, so assigning should fail
        ServiceRequest request = new ServiceRequest(dateTime, "Already cancelled", RequestState.CANCELLED, "Aveiro");
        repository.saveAndFlush(request);

        given()
        .when()
            .put("/api/requests/{id}/assign", request.getToken())
        .then()
            .statusCode(400)
            .body("error", equalTo("request isn't in a valid state to be assigned"));
    }

    @Test
    void startRequest_withInvalidState_returnsBadRequest() {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)), LocalTime.of(15, 30));

        // Not assigned yet, so can't start
        ServiceRequest request = new ServiceRequest(dateTime, "Not ready yet", RequestState.RECIEVED, "Aveiro");
        repository.saveAndFlush(request);

        given()
        .when()
            .put("/api/requests/{id}/start", request.getToken())
        .then()
            .statusCode(400)
            .body("error", equalTo("request isn't in a valid state to start"));
    }

    @Test
    void completeRequest_withInvalidState_returnsBadRequest() {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)), LocalTime.of(15, 30));

        // Not in progress, so can't complete
        ServiceRequest request = new ServiceRequest(dateTime, "Too early to complete", RequestState.RECIEVED, "Aveiro");
        repository.saveAndFlush(request);

        given()
        .when()
            .put("/api/requests/{id}/end", request.getToken())
        .then()
            .statusCode(400)
            .body("error", equalTo("request isn't in a valid state to end"));
    }
}