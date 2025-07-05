package com.example.busnotice.domain.busStop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusStopSectionRepository extends JpaRepository<BusStopSection, Long> {

}
