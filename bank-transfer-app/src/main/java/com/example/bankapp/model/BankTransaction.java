package com.example.bankapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bank_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    private LocalDateTime createdAt;

    private String description;

    private String fromAccount;

    private String toAccount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status; 
}