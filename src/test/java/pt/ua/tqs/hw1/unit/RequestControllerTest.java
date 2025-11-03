package pt.ua.tqs.hw1.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pt.ua.tqs.hw1.boundary.RequestController;
import pt.ua.tqs.hw1.data.RequestState;
import pt.ua.tqs.hw1.data.RequestStateChange;
import pt.ua.tqs.hw1.data.ServiceRequest;
import pt.ua.tqs.hw1.service.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequestService requestService;

    @Test
    void submitRequestSuccess() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setToken(1);
        when(requestService.submitRequest(any(ServiceRequest.class))).thenReturn(request);

        mockMvc.perform(post("/api/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\": \"2000-03-12T07:21:00\", \"description\":\"Help with microwave!\",\"municipality\":\"Aveiro\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(1))
                .andExpect(jsonPath("$.state").value("RECIEVED"));
    }

    @Test
    void submitRequestInvalidDate() throws Exception {
        when(requestService.submitRequest(any(ServiceRequest.class)))
                .thenThrow(new InvalidRequestDateException());

        mockMvc.perform(post("/api/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\": \"2131-03-12T07:21:00\", \"description\":\"Help with microwave!\",\"municipality\":\"Aveiro\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("the request date is invalid"));
    }

    @Test
    void submitRequestOverflow() throws Exception {
        when(requestService.submitRequest(any(ServiceRequest.class)))
                .thenThrow(new RequestOverflowException());

        mockMvc.perform(post("/api/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\": \"2026-04-21T07:21:00\", \"description\":\"Help with microwave!\",\"municipality\":\"Aveiro\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("too many requests for this day and place have already been booked"));
    }


    @Test
    void getRequests() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setToken(10);
        ServiceRequest request2 = new ServiceRequest();
        request2.setToken(15);
        ServiceRequest request3 = new ServiceRequest();
        request3.setToken(20);
        when(requestService.getRequests()).thenReturn(List.of(request, request2, request3));

        mockMvc.perform(get("/api/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].token").value(10))
                .andExpect(jsonPath("$[1].token").value(15))
                .andExpect(jsonPath("$[2].token").value(20));
    }


    @Test
    void getRequestsInMunicipality() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setMunicipality("Aveiro");
        request.setToken(99);

        when(requestService.getRequests("Aveiro")).thenReturn(List.of(request));

        mockMvc.perform(get("/api/requests/municipalities/Aveiro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].token").value(99))
                .andExpect(jsonPath("$[0].municipality").value("Aveiro"));
    }


    @Test
    void getRequestSuccess() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setToken(42);
        when(requestService.getRequest(42)).thenReturn(request);

        mockMvc.perform(get("/api/requests/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(42));
    }

    @Test
    void getRequestInvalidToken() throws Exception {
        mockMvc.perform(get("/api/requests/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid token"));
    }

    @Test
    void getRequestNotFound() throws Exception {
        when(requestService.getRequest(999)).thenThrow(new RequestNotFoundException());

        mockMvc.perform(get("/api/requests/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("couldn't find the request"));
    }


    @Test
    void getRequestStatesSuccess() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setToken(42);
        
        RequestStateChange change1 = new RequestStateChange(LocalDateTime.now(), RequestState.ASSIGNED);
        change1.setServiceRequest(request);
        
        RequestStateChange change2 = new RequestStateChange(LocalDateTime.now().plusDays(1), RequestState.IN_PROGRESS);
        change2.setServiceRequest(request);
        
        RequestStateChange change3 = new RequestStateChange(LocalDateTime.now().plusDays(1), RequestState.DONE);
        change3.setServiceRequest(request);

        when(requestService.getStateChanges(42)).thenReturn(List.of(change1, change2, change3));

        mockMvc.perform(get("/api/requests/42/states"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].state").value("ASSIGNED"))
                .andExpect(jsonPath("$[1].state").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[2].state").value("DONE"));
    }

    @Test
    void getRequestStatesInvalidToken() throws Exception {
        mockMvc.perform(get("/api/requests/abc/states"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid token"));
    }

    @Test
    void getRequestStatesNotFound() throws Exception {
        when(requestService.getStateChanges(999)).thenThrow(new RequestNotFoundException());

        mockMvc.perform(get("/api/requests/999/states"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("couldn't find the request"));
    }

    
    @Test
    void cancelRequestSuccess() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setToken(5);
        when(requestService.cancelRequest(5)).thenReturn(request);

        mockMvc.perform(put("/api/requests/5/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(5));
    }

    @Test
    void cancelRequestInvalidState() throws Exception {
        when(requestService.cancelRequest(7))
                .thenThrow(new InvalidStateTransitionException());

        mockMvc.perform(put("/api/requests/7/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("request isn't in a valid state to be cancelled"));
    }

    @Test
    void cancelRequestInvalidToken() throws Exception {
        mockMvc.perform(put("/api/requests/abc/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid token"));
    }

    @Test
    void cancelRequestNotFound() throws Exception {
        when(requestService.cancelRequest(999)).thenThrow(new RequestNotFoundException());

        mockMvc.perform(put("/api/requests/999/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("couldn't find the request"));
    }


    @Test
    void assignRequestSuccess() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setToken(5);
        when(requestService.assignRequest(5)).thenReturn(request);

        mockMvc.perform(put("/api/requests/5/assign"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(5));
    }

    @Test
    void assignRequestInvalidState() throws Exception {
        when(requestService.assignRequest(7))
                .thenThrow(new InvalidStateTransitionException());

        mockMvc.perform(put("/api/requests/7/assign"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("request isn't in a valid state to be assigned"));
    }

    @Test
    void assignRequestInvalidToken() throws Exception {
        mockMvc.perform(put("/api/requests/abc/assign"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid token"));
    }

    @Test
    void assignRequestNotFound() throws Exception {
        when(requestService.assignRequest(999)).thenThrow(new RequestNotFoundException());

        mockMvc.perform(put("/api/requests/999/assign"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("couldn't find the request"));
    }


    @Test
    void startRequestSuccess() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setToken(5);
        when(requestService.startRequest(5)).thenReturn(request);

        mockMvc.perform(put("/api/requests/5/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(5));
    }

    @Test
    void startRequestInvalidState() throws Exception {
        when(requestService.startRequest(7))
                .thenThrow(new InvalidStateTransitionException());

        mockMvc.perform(put("/api/requests/7/start"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("request isn't in a valid state to start"));
    }

    @Test
    void startRequestInvalidToken() throws Exception {
        mockMvc.perform(put("/api/requests/abc/start"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid token"));
    }

    @Test
    void startRequestNotFound() throws Exception {
        when(requestService.startRequest(999)).thenThrow(new RequestNotFoundException());

        mockMvc.perform(put("/api/requests/999/start"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("couldn't find the request"));
    }


    @Test
    void endRequestSuccess() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setToken(5);
        when(requestService.completeRequest(5)).thenReturn(request);

        mockMvc.perform(put("/api/requests/5/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(5));
    }

    @Test
    void endRequestInvalidState() throws Exception {
        when(requestService.completeRequest(7))
                .thenThrow(new InvalidStateTransitionException());

        mockMvc.perform(put("/api/requests/7/end"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("request isn't in a valid state to end"));
    }

    @Test
    void endRequestInvalidToken() throws Exception {
        mockMvc.perform(put("/api/requests/abc/end"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid token"));
    }

    @Test
    void endRequestNotFound() throws Exception {
        when(requestService.completeRequest(999)).thenThrow(new RequestNotFoundException());

        mockMvc.perform(put("/api/requests/999/end"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("couldn't find the request"));
    }
}
