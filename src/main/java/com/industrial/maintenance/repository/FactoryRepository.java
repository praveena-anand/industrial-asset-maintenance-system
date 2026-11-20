package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FactoryRepository extends JpaRepository<Factory, Long> {

    boolean existsByFactoryCode(String factoryCode);

    Optional<Factory> findByFactoryCode(String factoryCode);
}