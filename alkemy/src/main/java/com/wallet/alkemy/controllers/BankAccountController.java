package com.wallet.alkemy.controllers;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wallet.alkemy.dto.AccountDTO;
import com.wallet.alkemy.dto.DepositRequest;
import com.wallet.alkemy.dto.GroupedExpenseDTO;
import com.wallet.alkemy.dto.TransactionHistoryDTO;
import com.wallet.alkemy.dto.TransferRequestDTO;
import com.wallet.alkemy.models.tableBankAccount;
import com.wallet.alkemy.repository.TransactionRepository;
import com.wallet.alkemy.service.BankAccountService;
import com.wallet.alkemy.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class BankAccountController {

    private final BankAccountService accountService;
    private final TransactionService transactionService;
    
    // 1. ¡NUEVO! Declaramos el repositorio como final para que Lombok lo inyecte de inmediato
    private final TransactionRepository transactionRepository;

    /** Returns the authenticated user's current account balance. */
    @GetMapping("/balance")
    public ResponseEntity<AccountDTO> getBalance() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); 
        AccountDTO accountDTO = accountService.getBalanceByUser(userEmail);
        return ResponseEntity.ok(accountDTO);
    }

    @PostMapping("/deposit")
    /** Records a deposit for the authenticated user. */
    public ResponseEntity<Void> makeDeposit(
            @Valid @RequestBody DepositRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        transactionService.makeDeposit(authentication.getName(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    /** Transfers funds from the authenticated user to the requested account. */
    public ResponseEntity<Void> makeTransfer(
            @Valid @RequestBody TransferRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        transactionService.makeTransfer(authentication.getName(), request.getDestinationEmail(), request.getAmount());
        return ResponseEntity.ok().build();
    }

@GetMapping("/transactions")
public ResponseEntity<List<TransactionHistoryDTO>> getHistory() {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    String userEmail = authentication.getName();

    return ResponseEntity.ok(
            transactionService.getHistoryByEmail(userEmail)
    );
}

    @GetMapping("/expenses-summary")
    /** Returns the authenticated user's outgoing transactions grouped by movement. */
    public ResponseEntity<List<GroupedExpenseDTO>> getGroupedExpenses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(transactionService.getGroupedExpenses(authentication.getName()));
    }

@GetMapping("/dashboard-metrics")
public ResponseEntity<Map<String, Object>> getDashboardMetrics() {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    String userEmail = authentication.getName();

    tableBankAccount account =
            accountService.getAccountEntityByEmail(userEmail);

    LocalDateTime startOfMonth = LocalDateTime.now()
            .with(java.time.temporal.TemporalAdjusters.firstDayOfMonth())
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);

    LocalDateTime endOfMonth = LocalDateTime.now()
            .with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
            .withHour(23)
            .withMinute(59)
            .withSecond(59)
            .withNano(999999999);

    double ingresosMes =
            transactionRepository.getMonthlyIncomeByAccount(
                    BigInteger.valueOf(account.getId()),
                    startOfMonth,
                    endOfMonth
            );

    double gastosMes =
            transactionRepository.getMonthlyExpensesByAccount(
                    BigInteger.valueOf(account.getId()),
                    startOfMonth,
                    endOfMonth
            );

    Map<String, Object> metrics = new java.util.HashMap<>();

    metrics.put("balance", account.getBalance());
    metrics.put("monthlyIncome", ingresosMes);
    metrics.put("monthlyExpenses", gastosMes);

    return ResponseEntity.ok(metrics);
}




}
