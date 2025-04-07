package nl.ase_wayfinding.user_profile_information_store.repository;

import nl.ase_wayfinding.user_profile_information_store.model.RouteStartWaypoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteStartWaypointRepository extends JpaRepository<RouteStartWaypoint, Long> {

    @Query(value = """
        SELECT rsw.* FROM route_start_waypoints rsw
        JOIN routes r ON rsw.route_id = r.route_id
        WHERE r.status = 'started'
        AND (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(rsw.latitude)) *
                cos(radians(rsw.longitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(rsw.latitude))
            )
        ) <= :radius
        """, nativeQuery = true)
    List<RouteStartWaypoint> findStartedWaypointsNear(@Param("latitude") double latitude,
                                                      @Param("longitude") double longitude,
                                                      @Param("radius") double radius);
}
