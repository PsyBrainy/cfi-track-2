package com.wallet.alkemy.service;

import java.math.BigInteger;

import org.springframework.stereotype.Service;

import com.wallet.alkemy.dto.AccountDTO;
import com.wallet.alkemy.exception.AccountNotFoundException;
import com.wallet.alkemy.exception.UserNotFoundException;
import com.wallet.alkemy.models.tableBankAccount;
import com.wallet.alkemy.models.tableTransaction;
import com.wallet.alkemy.models.tableUser;
import com.wallet.alkemy.repository.AccountRepository;
import com.wallet.alkemy.repository.TransactionRepository;
import com.wallet.alkemy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final AccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /** Returns the latest balance and currency for the account owned by an email address. */
    public AccountDTO getBalanceByUser(String email) {
        tableUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BigInteger idUser = BigInteger.valueOf(user.getId());

        tableBankAccount account = bankAccountRepository.findByIdUser(idUser)
            .orElseThrow(() -> new AccountNotFoundException("Account not found for user"));

        double currentBalance = transactionRepository
            .findFirstByAccountNumberOrderByIdDesc(BigInteger.valueOf(account.getId()))
            .map(tableTransaction::getBalance)
            .orElse(0.0);

        return new AccountDTO(currentBalance, account.getCurrency());
    }

    /** Creates an active account with the application's default currency for a user. */
    public tableBankAccount createBankAccount(tableUser user) {
        tableBankAccount newAccount = new tableBankAccount();
        newAccount.setIdUser(BigInteger.valueOf(user.getId()));
        newAccount.setActive(true);
        newAccount.setCurrency("AR");
        newAccount.setBalance(0.0);
        return bankAccountRepository.save(newAccount);
    }
}
