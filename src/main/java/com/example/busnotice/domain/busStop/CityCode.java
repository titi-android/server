package com.example.busnotice.domain.busStop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class CityCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String code;

    @Column(unique = true, nullable = false)
    String name;

    public CityCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public CityCode() {

    }
}
