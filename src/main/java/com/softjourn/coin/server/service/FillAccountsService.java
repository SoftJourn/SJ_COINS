package com.softjourn.coin.server.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.dto.*;
import com.softjourn.coin.server.entity.*;
import com.softjourn.coin.server.exceptions.CouldNotReadFileException;
import com.softjourn.coin.server.exceptions.NotEnoughAmountInTreasuryException;
import com.softjourn.coin.server.repository.TransactionRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.softjourn.coin.server.util.Util.dataToCSV;
import static com.softjourn.coin.server.util.Util.getDataFromCSV;
import static com.softjourn.coin.server.util.Util.validateMultipartFileMimeType;

@Slf4j
@Service
public class FillAccountsService {

    private final CoinService coinService;

    private final FabricService fabricService;

    private final AccountsService accountsService;

    private final TransactionRepository transactionRepository;

    @Autowired
    public FillAccountsService(CoinService coinService, FabricService fabricService, AccountsService accountsService, TransactionRepository transactionRepository) {
        this.coinService = coinService;
        this.fabricService = fabricService;
        this.accountsService = accountsService;
        this.transactionRepository = transactionRepository;
    }

    public void fillAccounts(MultipartFile multipartFile) {
        // is file valid
        validateMultipartFileMimeType(multipartFile, "text/csv|application/vnd.ms-excel");
        try {
            // reading file content
            MappingIterator<AccountFillDTO> iterator = getDataFromCSV(multipartFile.getBytes(), AccountFillDTO.class);
            // get data from read file
            List<AccountFillDTO> accountsToFill = iterator.readAll();
            // Check if all values are positive
            this.checkAmountIsPositive(accountsToFill);
            // Are enough coins in treasury
            if (!isEnoughInTreasury(BigDecimal.valueOf(accountsToFill.stream()
                    .mapToDouble(value -> value.getCoins().doubleValue()).sum()))) {
                throw new NotEnoughAmountInTreasuryException("Not enough coins in treasury!");
            } else {
                List<BatchTransferDTO> requestArray = accountsToFill.stream().
                        map(FillAccountsService::apply).collect(Collectors.toList());
                ObjectMapper mapper = new ObjectMapper();
                InvokeResponseDTO batchTransfer = fabricService.
                        invoke(coinService.getTreasuryAccount(),
                                "batchTransfer",
                                new String[]{mapper.writeValueAsString(requestArray)}, InvokeResponseDTO.class);
                saveTransactions(accountsToFill, batchTransfer);
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            throw new CouldNotReadFileException("File contains mistakes");
        }
    }

    private void saveTransactions(List<AccountFillDTO> accountsToFill, InvokeResponseDTO batchTransfer) {
        accountsService.getAll().forEach(account -> {
            accountsToFill.forEach(dto -> {
                if (account.getEmail().equals(dto.getAccount())) {
                    Transaction transaction = new Transaction();
                    transaction.setStatus(TransactionStatus.SUCCESS);
                    transaction.setTransactionId(batchTransfer.getTransactionID());
                    transaction.setAmount(dto.getCoins());
                    transaction.setDestination(account);
                    transaction.setType(TransactionType.REGULAR_REPLENISHMENT);
                    transactionRepository.save(transaction);
                }
            });
        });
    }

    public void getAccountDTOTemplate(Writer writer) throws IOException {
        List<Account> accounts = this.accountsService.getAll(AccountType.REGULAR);
        List<AccountFillDTO> collect = accounts.stream().map((Account a) -> {
            AccountFillDTO dto = new AccountFillDTO();
            dto.setAccount(a.getEmail());
            dto.setFullName(a.getFullName());
            dto.setCoins(new BigDecimal(0));
            return dto;
        }).sorted(Comparator.comparing(AccountFillDTO::getAccount)).collect(Collectors.toList());
        dataToCSV(writer, collect, AccountFillDTO.class);
    }

    private Boolean isEnoughInTreasury(BigDecimal decimal) {
        BigDecimal treasuryAmount = this.coinService.getTreasuryAmount();
        return decimal.compareTo(treasuryAmount) < 0;
    }

    private void checkAmountIsPositive(List<AccountFillDTO> accountDTOs) {
        accountDTOs.forEach(accountDTO -> this.checkAmountIsPositive(accountDTO.getCoins()));
    }

    private void checkAmountIsPositive(@NonNull BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
    }

    private static BatchTransferDTO apply(AccountFillDTO accountFillDTO) {
        return new BatchTransferDTO(accountFillDTO.getAccount(), accountFillDTO.getCoins());
    }

}
