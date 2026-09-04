package com.wallet.alkemy.service;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wallet.alkemy.enums.MovementType;
import com.wallet.alkemy.enums.TransactionType;
import com.wallet.alkemy.dto.GroupedExpenseDTO;
import com.wallet.alkemy.dto.TransactionHistoryDTO;
import com.wallet.alkemy.exception.AccountNotFoundException;
import com.wallet.alkemy.exception.InactiveAccountException;
import com.wallet.alkemy.exception.InsufficientBalanceException;
import com.wallet.alkemy.exception.UserNotFoundException;
import com.wallet.alkemy.models.tableBankAccount;
import com.wallet.alkemy.models.tableTransaction;
import com.wallet.alkemy.repository.AccountRepository;
import com.wallet.alkemy.repository.TransactionRepository;
import com.wallet.alkemy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    /** Adds a positive deposit to the authenticated user's active account. */
    public void makeDeposit(String email, Double amount) {
        if (email == null || email.isBlank() || amount == null || !Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("The deposit amount must be greater than zero");
        }

        tableBankAccount account = userRepository.findByEmail(email)
                .map(user -> accountRepository.findByIdUser(BigInteger.valueOf(user.getId()))
                        .orElseThrow(() -> new AccountNotFoundException("Account not found for user")))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        validateActiveAccount(account);

        Long accountId = account.getId();
        BigInteger accountNumber = BigInteger.valueOf(accountId);
        double currentBalance = transactionRepository
            .findFirstByAccountNumberOrderByIdDesc(accountNumber)
            .map(tableTransaction::getBalance)
            .orElse(0.0);

        double newBalance = currentBalance + amount;

        int updatedAccounts = accountRepository.updateBalance(accountId, newBalance);
        if (updatedAccounts != 1) {
            throw new AccountNotFoundException("Account not found");
        }

        tableTransaction transaction = new tableTransaction();
        transaction.setDateTransaction(LocalDateTime.now());
        transaction.setBalance(newBalance);
        transaction.setAmount(amount);
        transaction.setMovementType(MovementType.DEPOSIT.databaseValue());
        transaction.setType(TransactionType.INCOME.databaseValue());
        transaction.setAccountNumber(accountNumber);
        transactionRepository.save(transaction);
    }

    /** Resolves the user's source account and delegates the transfer to the ID-based operation. */
    @Transactional
    public void makeTransfer(String sourceEmail, Long destinationAccountId, Double amount) {
        tableBankAccount sourceAccount = userRepository.findByEmail(sourceEmail)
                .map(user -> accountRepository.findByIdUser(BigInteger.valueOf(user.getId()))
                        .orElseThrow(() -> new AccountNotFoundException("Account not found for user")))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        validateActiveAccount(sourceAccount);

        makeTransfer(sourceAccount.getId(), destinationAccountId, amount);
    }

    /** Transfers funds between two active accounts and records both movements. */
    @Transactional
    public void makeTransfer(Long sourceAccountId, Long destinationAccountId, Double amount) {
        if (sourceAccountId == null || destinationAccountId == null || amount == null || !Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Transfer data is invalid");
        }
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        tableBankAccount sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));
        tableBankAccount destinationAccount = accountRepository.findById(destinationAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));
        validateActiveAccount(sourceAccount);
        validateActiveAccount(destinationAccount);

        BigInteger sourceAccountNumber = BigInteger.valueOf(sourceAccountId);
        BigInteger destinationAccountNumber = BigInteger.valueOf(destinationAccountId);

        double sourceBalance = getCurrentBalance(sourceAccountNumber);
        if (sourceBalance < amount) {
            throw new InsufficientBalanceException("Insufficient balance in source account");
        }

        double newSourceBalance = sourceBalance - amount;
        int updatedSourceAccounts = accountRepository.updateBalance(sourceAccountId, newSourceBalance);
        if (updatedSourceAccounts != 1) {
            throw new AccountNotFoundException("Source account not found");
        }

        tableTransaction expense = new tableTransaction();
        expense.setDateTransaction(LocalDateTime.now());
        expense.setBalance(newSourceBalance);
        expense.setAmount(amount);
        expense.setMovementType(MovementType.TRANSFER.databaseValue());
        expense.setType(TransactionType.EXPENSE.databaseValue());
        expense.setAccountNumber(sourceAccountNumber);
        transactionRepository.save(expense);

        double destinationBalance = getCurrentBalance(destinationAccountNumber);
        double newDestinationBalance = destinationBalance + amount;
        int updatedDestinationAccounts = accountRepository.updateBalance(destinationAccountId, newDestinationBalance);
        if (updatedDestinationAccounts != 1) {
            throw new AccountNotFoundException("Destination account not found");
        }

        tableTransaction income = new tableTransaction();
        income.setDateTransaction(LocalDateTime.now());
        income.setBalance(newDestinationBalance);
        income.setAmount(amount);
        income.setMovementType(MovementType.TRANSFER.databaseValue());
        income.setType(TransactionType.INCOME.databaseValue());
        income.setAccountNumber(destinationAccountNumber);
        transactionRepository.save(income);
    }

    /** Returns the authenticated user's transaction history. */
    @Transactional(readOnly = true)
    public List<TransactionHistoryDTO> getHistory(String email) {
        tableBankAccount account = getActiveAccount(email);
        return transactionRepository.getHistoryByAccount(BigInteger.valueOf(account.getId()));
    }

    /** Returns the authenticated user's outgoing transactions grouped by movement type. */
    @Transactional(readOnly = true)
    public List<GroupedExpenseDTO> getGroupedExpenses(String email) {
        tableBankAccount account = getActiveAccount(email);
        return transactionRepository.getGroupedExpensesByAccount(BigInteger.valueOf(account.getId()));
    }

    /** Reads the latest recorded balance, defaulting to zero for a new account. */
    private double getCurrentBalance(BigInteger accountNumber) {
        return transactionRepository.findFirstByAccountNumberOrderByIdDesc(accountNumber)
                .map(tableTransaction::getBalance)
                .orElse(0.0);
    }

    /** Rejects operations against a closed bank account. */
    private void validateActiveAccount(tableBankAccount account) {
        if (!account.isActive()) {
            throw new InactiveAccountException("The bank account is not open");
        }
    }

    /** Resolves and validates the active account belonging to an email address. */
    private tableBankAccount getActiveAccount(String email) {
        if (email == null || email.isBlank()) {
            throw new UserNotFoundException("User not found");
        }

        tableBankAccount account = userRepository.findByEmail(email)
                .map(user -> accountRepository.findByIdUser(BigInteger.valueOf(user.getId()))
                        .orElseThrow(() -> new AccountNotFoundException("Account not found for user")))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        validateActiveAccount(account);
        return account;
    }
}
