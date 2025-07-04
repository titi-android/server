package com.example.busnotice.domain.busStop;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityCodeRepository extends JpaRepository<CityCode, Long> {

    boolean existsByCode(String code);

    Optional<CityCode> findByName(String cityName);
}
