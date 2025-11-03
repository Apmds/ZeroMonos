package pt.ua.tqs.hw1.data;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RequestStateChangeId implements Serializable {

    private long serviceRequest;

    private LocalDateTime date;

    public RequestStateChangeId() {
    }

    public RequestStateChangeId(long serviceRequest, LocalDateTime date) {
        this.serviceRequest = serviceRequest;
        this.date = date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (serviceRequest ^ (serviceRequest >>> 32));
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequestStateChangeId other = (RequestStateChangeId) obj;
        if (serviceRequest != other.serviceRequest)
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        return true;
    }
}