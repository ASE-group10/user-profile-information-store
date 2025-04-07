package nl.ase_wayfinding.user_profile_information_store.repository;

import nl.ase_wayfinding.user_profile_information_store.model.JourneyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import nl.ase_wayfinding.user_profile_information_store.model.RouteHistory;

import java.util.List;

public interface JourneyLogRepository extends JpaRepository<JourneyLog, Long> {
    List<JourneyLog> findByRouteHistory(RouteHistory routeHistory);
}
