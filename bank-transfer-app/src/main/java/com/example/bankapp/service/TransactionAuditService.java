package com.example.bankapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.repository.BankTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionAuditService {

    private final BankTransactionRepository transactionRepository;

    // REQUIRES_NEW ensures this runs in its own transaction and commits even if the main transfer rolls back
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(BankTransaction transaction) {
        transactionRepository.save(transaction);
    }
}