package com.example.ingestion_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntry, Long> {
    // spring data will automatically be implemented
}
