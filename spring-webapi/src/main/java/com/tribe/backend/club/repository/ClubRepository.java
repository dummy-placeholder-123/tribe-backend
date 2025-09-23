package com.tribe.backend.club.repository;

import com.tribe.backend.club.domain.Club;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, UUID> {

    List<Club> findByCityIgnoreCase(String city);
}
