package com.example.bankapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;
    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String keyword, Model model) {
        List<Customer> customerList;
        if (keyword != null && !keyword.isEmpty()) {
            customerList = customerRepository.searchByName(keyword);
        } else {
            customerList = customerRepository.findAllOrdered();
        }

        model.addAttribute("allCustomers", customerList);
        model.addAttribute("customers", customerList);
        model.addAttribute("transferRequest", new TransferRequest());
        
        if (!model.containsAttribute("customer")) {
            model.addAttribute("customer", new Customer());
        }

        model.addAttribute("totalBalance", customerRepository.getTotalBalance());
        model.addAttribute("totalCustomers", customerRepository.countCustomersNative());
        model.addAttribute("successCount", transactionRepository.countByStatus(TransactionStatus.SUCCESS));
        model.addAttribute("failedCount", transactionRepository.countByStatus(TransactionStatus.FAILED));
        model.addAttribute("transactions", transactionRepository.findAllLatest());
        return "bank";
    }

    @PostMapping("/customers")
    public ResponseEntity<?> addCustomer(@Valid @RequestBody Customer customer, 
                                         BindingResult result) {
        if (result.hasErrors()) {
            String errorMsg = result.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst().orElse("Customer validation failed!");
            return ResponseEntity.badRequest().body(Map.of("error", errorMsg));
        }
        try {
            bankService.createCustomer(customer);
            return ResponseEntity.ok(Map.of("message", "Customer added successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    public String transfer(@Valid @ModelAttribute TransferRequest request, 
                           BindingResult result, 
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Validation failed!");
            return "redirect:/";
        }

        try {
            bankService.transfer(
                request.getFromAccount(), 
                request.getToAccount(), 
                request.getAmount(), 
                request.isSimulateFailure()
            );
            redirectAttributes.addFlashAttribute("message", "Transfer completed successfully!");
        } catch (Exception e) {
            BankTransaction failedTx = new BankTransaction();
            failedTx.setAmount(request.getAmount());
            failedTx.setCreatedAt(LocalDateTime.now());
            failedTx.setDescription(e.getMessage());
            failedTx.setFromAccount(request.getFromAccount());
            failedTx.setToAccount(request.getToAccount());
            failedTx.setStatus(TransactionStatus.FAILED);
            auditService.recordFailure(failedTx);

            redirectAttributes.addFlashAttribute("error", "Transfer failed: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(@PathVariable String accountNumber, Model model) {
        Customer customer = bankService.getCustomerByAccountNumber(accountNumber);
        model.addAttribute("customer", customer);
        model.addAttribute("accountNumber", accountNumber);
        model.addAttribute("transactions", transactionRepository.findHistory(accountNumber));
        return "history";
    }
}