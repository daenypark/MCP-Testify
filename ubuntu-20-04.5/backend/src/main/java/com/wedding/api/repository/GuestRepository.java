package com.wedding.api.repository;

import com.wedding.api.model.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    
    Optional<Guest> findByEmail(String email);
    
    @Query("SELECT g FROM Guest g WHERE " +
           "LOWER(g.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Guest> findBySearchTerm(@Param("search") String search, Pageable pageable);
    
    List<Guest> findByPlusOneTrue();
    
    @Query(value = "SELECT * FROM guests ORDER BY RANDOM() LIMIT 10", nativeQuery = true)
    List<Guest> findRandomGuests();
    
    // Intentionally slow query for APM testing
    @Query(value = "SELECT g.* FROM guests g " +
           "CROSS JOIN generate_series(1, 1000) " +
           "ORDER BY g.id LIMIT 100", nativeQuery = true)
    List<Guest> findGuestsWithSlowQuery();
    
    long countByPlusOneTrue();
} 