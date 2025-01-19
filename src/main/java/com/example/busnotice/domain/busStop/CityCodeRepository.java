package com.example.busnotice.domain.busStop;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CityCodeRepository extends JpaRepository<CityCode, Long> {

    boolean existsByCode(String code);
}
