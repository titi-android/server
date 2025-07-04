package com.example.busnotice.domain.busStop;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(indexes = @Index(name = "idx_name", columnList = "name"))
public class CityCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
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
