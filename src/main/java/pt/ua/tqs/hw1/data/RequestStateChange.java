package pt.ua.tqs.hw1.data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "request_state")
@IdClass(RequestStateChangeId.class)
public class RequestStateChange {

    @Id
    @ManyToOne
    @JoinColumn(name = "request_token")
    @JsonBackReference
    private ServiceRequest serviceRequest;

    @Id
    private LocalDateTime date;

    @Column
    private RequestState state;

    public RequestStateChange() {
    }

    public RequestStateChange(LocalDateTime date, RequestState state) {
        this.date = date;
        this.state = state;
    }

    public ServiceRequest getServiceRequest() {
        return serviceRequest;
    }

    public void setServiceRequest(ServiceRequest serviceRequest) {
        this.serviceRequest = serviceRequest;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;
    }
}