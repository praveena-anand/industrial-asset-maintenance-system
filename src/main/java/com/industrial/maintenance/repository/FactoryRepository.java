package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactoryRepository extends JpaRepository<Factory, Long> {

    boolean existsByFactoryCode(String factoryCode);
}