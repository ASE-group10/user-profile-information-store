package nl.ase_wayfinding.user_profile_information_store.repository;

import nl.ase_wayfinding.user_profile_information_store.model.Route;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query(value = """
        SELECT * 
        FROM routes 
        WHERE (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(cast(source_location->>'latitude' AS double precision))) * 
                cos(radians(cast(source_location->>'longitude' AS double precision)) - radians(:longitude)) + 
                sin(radians(:latitude)) * sin(radians(cast(source_location->>'latitude' AS double precision)))
            )
        ) <= :radius
        OR (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(cast(destination_location->>'latitude' AS double precision))) * 
                cos(radians(cast(destination_location->>'longitude' AS double precision)) - radians(:longitude)) + 
                sin(radians(:latitude)) * sin(radians(cast(destination_location->>'latitude' AS double precision)))
            )
        ) <= :radius
        """, nativeQuery = true)
    List<Route> findRoutesNearLocation(@Param("latitude") double latitude,
                                       @Param("longitude") double longitude,
                                       @Param("radius") double radius);
}
