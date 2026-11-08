package com.example.bankapp.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public Customer getCustomerByAccountNumber(String accountNumber) {
        return customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found with account number: " + accountNumber));
    }

    @Transactional
    public void createCustomer(Customer customer) {
        if (customerRepository.countAccounts(customer.getAccountNumber()) > 0) {
            throw new RuntimeException("Account number already exists!");
        }

        if (customerRepository.countEmails(customer.getEmail()) > 0) {
            throw new RuntimeException("Email already exists!");
        }

        customerRepository.save(customer);
    }

    // Task 10 Complete: Restored original transactional protection with rollback support
    @Transactional(rollbackFor = Exception.class)
    public void transfer(String fromAccount, String toAccount, BigDecimal amount, boolean simulateFailure) {

        if (fromAccount.equals(toAccount)) {
            throw new RuntimeException("Source and destination accounts must be different");
        }

        Customer sender = customerRepository.getByAccountNumber(fromAccount)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        int debited = customerRepository.debit(fromAccount, amount);

        if (debited == 0) {
            throw new RuntimeException("Debit failed");
        }

        if (simulateFailure) {
            throw new RuntimeException("Simulation system failure after the withdrawal");
        }

        int credited = customerRepository.credit(toAccount, amount);

        if (credited == 0) {
            throw new RuntimeException("Credit failed");
        }
    }
}