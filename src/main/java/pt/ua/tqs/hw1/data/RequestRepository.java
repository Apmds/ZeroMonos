
package pt.ua.tqs.hw1.data;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<ServiceRequest, Long> {

    @EntityGraph(attributePaths = "stateChanges") // Makes it so stateChanges is populated
    public Optional<ServiceRequest> findByToken(long token);

    public List<ServiceRequest> findByDateBetween(LocalDateTime start, LocalDateTime end);

    public List<ServiceRequest> findByDateBetweenAndMunicipality(LocalDateTime start, LocalDateTime end, String municipality);

    public List<ServiceRequest> findByDateAfterAndMunicipality(LocalDateTime date, String municipality);
}