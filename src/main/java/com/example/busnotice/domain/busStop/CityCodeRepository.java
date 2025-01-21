package com.example.busnotice.domain.busStop;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityCodeRepository extends JpaRepository<CityCode, Long> {

    boolean existsByCode(String code);

    Optional<CityCode> findByName(String cityName);
}
