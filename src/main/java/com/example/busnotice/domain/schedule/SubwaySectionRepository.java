package com.example.busnotice.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubwaySectionRepository extends JpaRepository<SubwaySection, Long> {

}
