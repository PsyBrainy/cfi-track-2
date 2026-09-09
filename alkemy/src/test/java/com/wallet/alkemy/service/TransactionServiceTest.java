package com.wallet.alkemy.service;

import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wallet.alkemy.exception.InsufficientBalanceException;
import com.wallet.alkemy.models.tableBankAccount;
import com.wallet.alkemy.models.tableTransaction;
import com.wallet.alkemy.repository.AccountRepository;
import com.wallet.alkemy.repository.TransactionRepository;
import com.wallet.alkemy.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final Long SOURCE_ACCOUNT_ID = 1L;
    private static final Long DESTINATION_ACCOUNT_ID = 2L;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(accountRepository, transactionRepository, userRepository);
    }

    @Test
    void makeTransfer_happyPath_debitsSourceAndCreditsDestination() {
        double initialSourceBalance = 1000.0;
        double initialDestinationBalance = 500.0;
        double amount = 300.0;

        when(accountRepository.findById(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(activeAccount()));
        when(accountRepository.findById(DESTINATION_ACCOUNT_ID)).thenReturn(Optional.of(activeAccount()));
        when(transactionRepository.findFirstByAccountNumberOrderByIdDesc(BigInteger.valueOf(SOURCE_ACCOUNT_ID)))
            .thenReturn(Optional.of(transactionWithBalance(initialSourceBalance)));
        when(transactionRepository.findFirstByAccountNumberOrderByIdDesc(BigInteger.valueOf(DESTINATION_ACCOUNT_ID)))
            .thenReturn(Optional.of(transactionWithBalance(initialDestinationBalance)));
        when(accountRepository.updateBalance(anyLong(), anyDouble())).thenReturn(1);

        transactionService.makeTransfer(SOURCE_ACCOUNT_ID, DESTINATION_ACCOUNT_ID, amount);

        verify(accountRepository).updateBalance(SOURCE_ACCOUNT_ID, initialSourceBalance - amount);
        verify(accountRepository).updateBalance(DESTINATION_ACCOUNT_ID, initialDestinationBalance + amount);

        ArgumentCaptor<tableTransaction> transactionCaptor = ArgumentCaptor.forClass(tableTransaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());

        tableTransaction expense = transactionCaptor.getAllValues().get(0);
        tableTransaction income = transactionCaptor.getAllValues().get(1);

        assertEquals(initialSourceBalance - amount, expense.getBalance());
        assertEquals(initialDestinationBalance + amount, income.getBalance());
    }

    @Test
    void makeTransfer_insufficientBalance_throwsAndPersistsNothing() {
        double initialSourceBalance = 100.0;
        double amount = 500.0;

        when(accountRepository.findById(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(activeAccount()));
        when(accountRepository.findById(DESTINATION_ACCOUNT_ID)).thenReturn(Optional.of(activeAccount()));
        when(transactionRepository.findFirstByAccountNumberOrderByIdDesc(BigInteger.valueOf(SOURCE_ACCOUNT_ID)))
            .thenReturn(Optional.of(transactionWithBalance(initialSourceBalance)));

        assertThrows(InsufficientBalanceException.class,
            () -> transactionService.makeTransfer(SOURCE_ACCOUNT_ID, DESTINATION_ACCOUNT_ID, amount));

        verify(accountRepository, never()).updateBalance(anyLong(), anyDouble());
        verify(transactionRepository, never()).save(any(tableTransaction.class));
    }

    private tableTransaction transactionWithBalance(double balance) {
        tableTransaction transaction = new tableTransaction();
        transaction.setBalance(balance);
        return transaction;
    }

    private tableBankAccount activeAccount() {
        tableBankAccount account = new tableBankAccount();
        account.setActive(true);
        return account;
    }
}
