package pt.ua.tqs.hw1.data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "request")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long token;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private RequestState state = RequestState.RECIEVED;

    @OneToMany(mappedBy="serviceRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<RequestStateChange> stateChanges = new HashSet<>();

    @Column(nullable = false)
    private String municipality;

    public ServiceRequest() {
    }

    public ServiceRequest(LocalDateTime date, String description, RequestState state, String municipality) {
        this.date = date;
        this.description = description;
        this.state = state;
        this.municipality = municipality;
    }

    public long getToken() {
        return token;
    }

    public void setToken(long token) {
        this.token = token;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;

        // Logic for adding new state
        RequestStateChange newChange = new RequestStateChange(LocalDateTime.now(), state);
        newChange.setServiceRequest(this);
        this.stateChanges.add(newChange);
    }

    public Set<RequestStateChange> getStateChanges() {
        return stateChanges;
    }

    public void setStateChanges(Set<RequestStateChange> stateChanges) {
        this.stateChanges = stateChanges;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    
}