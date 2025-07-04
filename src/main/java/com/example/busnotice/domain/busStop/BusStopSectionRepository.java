package com.example.busnotice.domain.busStop;

import com.example.busnotice.domain.schedule.BusStopSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusStopSectionRepository extends JpaRepository<BusStopSection, Long> {

}
