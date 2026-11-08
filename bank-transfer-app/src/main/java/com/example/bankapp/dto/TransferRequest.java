package com.example.bankapp.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequest {

    @NotBlank(message = "From account cannot be blank")
    private String fromAccount;

    @NotBlank(message = "To account cannot be blank")
    private String toAccount;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private boolean simulateFailure;
}