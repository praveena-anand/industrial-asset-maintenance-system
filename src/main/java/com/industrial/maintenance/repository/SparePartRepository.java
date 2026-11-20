package com.industrial.maintenance.repository;

import com.industrial.maintenance.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    boolean existsByPartNumber(String partNumber);
}