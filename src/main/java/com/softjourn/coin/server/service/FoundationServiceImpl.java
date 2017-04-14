package com.softjourn.coin.server.service;

import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.dto.FoundationInfoDTO;
import com.softjourn.coin.server.dto.FoundationTransactionResultDTO;
import com.softjourn.coin.server.dto.ApproveDTO;
import com.softjourn.coin.server.dto.WithdrawDTO;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.Instance;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.exceptions.ErisAccountNotFoundException;
import com.softjourn.coin.server.exceptions.ErisContractInstanceNotFound;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.repository.InstanceRepository;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FoundationServiceImpl implements FoundationService {

    private static final String DONATE = "approveAndCall";
    private static final String WITHDRAW = "withdraw";
    private static final String GET_TOKENS_COUNT = "getTokensCount";
    private static final String TOKENS_ACCUMULATED = "tokensAccumulated";
    private static final String TOKEN_AMOUNTS = "tokenAmounts";
    private static final String CLOSE = "close";
    private static final String WITHDRAWAL_LENGTH = "getContractFulfilmentRecordLength";
    private static final String WITHDRAWAL_RECORD = "getContractFulfilmentRecord";

    private ErisContractService contractService;

    private InstanceRepository instanceRepository;

    private ErisAccountsService erisAccountsService;

    @Autowired
    public FoundationServiceImpl(ErisContractService contractService, InstanceRepository instanceRepository, ErisAccountsService erisAccountsService) {
        this.contractService = contractService;
        this.instanceRepository = instanceRepository;
        this.erisAccountsService = erisAccountsService;
    }

    /**
     * This method should be used to do donates and exchanges
     * @param dto
     * @param principal
     * @return Transaction
     * @throws IOException
     */
    @Override
    @SaveTransaction(comment = "Donate to project")
    public Transaction approve(ApproveDTO dto, Principal principal) throws IOException {
        // look for instance address and eris account
        Instance instance = prepareInstance(dto.getContractAddress());
        ErisAccount erisAccount = prepareErisAccount(principal.getName());
        Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), erisAccount);
        // approve
        Response response = contract.call(DONATE, dto.getSpenderAddress(), dto.getAmount());
        if (response.getError() != null) {
            log.error(response.getError().toString());
            throw new ErisProcessingException(response.getError().getMessage());
        } else {
            Transaction transaction = TransactionsService.prepareTransaction(new FoundationTransactionResultDTO((Boolean) response.getReturnValues().get(0)),
                    response.getTxParams().getTxId(), String.format("Donate coins %d", dto.getAmount()));

            transaction.setAccount(erisAccount.getAccount());
            transaction.setAmount(new BigDecimal(dto.getAmount()));
            transaction.setDestination(instance.getAccount());
            return transaction;
        }
    }

    /**
     * Method sends request to contract to stop his crawdsale campaign
     * @param address
     * @param principal
     * @return Transaction
     * @throws IOException
     */
    @Override
    @SaveTransaction(comment = "Close project")
    public Transaction close(String address, Principal principal) throws IOException {
        // look for instance address and eris account
        Instance instance = prepareInstance(address);
        ErisAccount erisAccount = prepareErisAccount(principal.getName());
        // get contract
        Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), null);
        // close
        Response response = contract.call(CLOSE);
        if (response.getError() != null) {
            log.error(response.getError().toString());
            throw new ErisProcessingException(response.getError().getMessage());
        } else {
            Transaction transaction = TransactionsService.prepareTransaction(new FoundationTransactionResultDTO((Boolean) response.getReturnValues().get(0)),
                    response.getTxParams().getTxId(), String.format("Close foundation contract: %s", instance.getName()));

            transaction.setAccount(erisAccount.getAccount());
            transaction.setDestination(instance.getAccount());
            return transaction;
        }
    }

    /**
     * Method sends request to withdraw coins
     * @param contractAddress
     * @param dto
     * @return Transaction
     * @throws IOException
     */
    @Override
    @SaveTransaction(comment = "Withdraw coins")
    public Transaction withdraw(String contractAddress, WithdrawDTO dto) throws IOException {
        // look for instance address
        Instance instance = prepareInstance(contractAddress);
        // get contract
        Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), null);
        // withdraw
        Response response = contract.call(WITHDRAW, dto.getAmount(), dto.getId(), dto.getNote());
        if (response.getError() != null) {
            log.error(response.getError().toString());
            throw new ErisProcessingException(response.getError().getMessage());
        } else {

            Transaction transaction = TransactionsService.prepareTransaction(new FoundationTransactionResultDTO((Boolean) response.getReturnValues().get(0)),
                    response.getTxParams().getTxId(), String.format("Withdraw %d coins from %s", dto.getAmount(), instance.getAddress()));

            transaction.setAccount(instance.getAccount());
            return transaction;
        }
    }

    /**
     * Method sends request to get current values of foundation contract fields
     * @param address
     * @return FoundationInfoDTO
     * @throws IOException
     */
    @Override
    public FoundationInfoDTO getInfo(String address) throws IOException {
        FoundationInfoDTO infoDTO = new FoundationInfoDTO();
        // look for instance address
        Instance instance = prepareInstance(address);
        // get contracts field info
        Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), null);
        for (Foundation foundation : Foundation.values()) {
            Map<String, Object> map = new HashMap<>();
            Response response = contract.call(foundation.getField());
            processResponseError(response);
            map.put("name", foundation.getField());
            map.put("value", response.getReturnValues().get(0));
            infoDTO.getInfo().add(map);
        }
        infoDTO.setTokens(getTokensInfo(contract));
        infoDTO.setWithdrawInfo(getWithdrawalInfo(contract));
        return infoDTO;
    }


    /**
     * Method gets information about tokens that are used in foundation contract
     * @param contract
     * @return List<Map<String, Object>>
     * @throws IOException
     */
    private List<Map<String, Object>> getTokensInfo(Contract contract) throws IOException {
        List<Map<String, Object>> maps = new ArrayList<>();
        // get tokens count
        Response tokensCountResponse = contract.call(GET_TOKENS_COUNT);
        processResponseError(tokensCountResponse);
        BigInteger tokensCount = (BigInteger) tokensCountResponse.getReturnValues().get(0);
        for (int i = 0; i < tokensCount.intValue(); i++) {
            // get token address
            Response token = contract.call(TOKENS_ACCUMULATED, BigInteger.valueOf(i));
            processResponseError(token);
            // get token amount
            Response amount = contract.call(TOKEN_AMOUNTS, token.getReturnValues().get(0));
            processResponseError(amount);
            Map<String, Object> map = new HashMap<>();
            map.put("address", token.getReturnValues().get(0));
            map.put("amount", amount.getReturnValues().get(0));
            maps.add(map);
        }
        return maps;
    }

    /**
     * Method gets information about withdrawal operations
     * @param contract
     * @return List<Map<String, Object>>
     * @throws IOException
     */
    private List<Map<String, Object>> getWithdrawalInfo(Contract contract) throws IOException {
        List<Map<String, Object>> withdrawals = new ArrayList<>();
        // get length of withdrawal records
        Response tokensCountResponse = contract.call(WITHDRAWAL_LENGTH);
        processResponseError(tokensCountResponse);
        BigInteger withdrawalLength = (BigInteger) tokensCountResponse.getReturnValues().get(0);
        for (int i = 0; i < withdrawalLength.intValue(); i++) {
            // get withdrawal
            Response withdrawal = contract.call(WITHDRAWAL_RECORD, BigInteger.valueOf(i));
            processResponseError(withdrawal);
            withdrawals.add(new HashMap<String, Object>(){{
                put("amount", withdrawal.getReturnValues().get(0));
                put("id", withdrawal.getReturnValues().get(1));
                put("timestamp", withdrawal.getReturnValues().get(2));
                put("note", withdrawal.getReturnValues().get(3));
            }});

        }
        return withdrawals;
    }

    private void processResponseError(Response response) {
        if (response.getError() != null) {
            log.error(response.getError().toString());
            throw new ErisProcessingException(response.getError().getMessage());
        }
    }

    private Instance prepareInstance(String address) {
        Instance instance = instanceRepository.findByAddress(address);
        if (instance == null) {
            throw new ErisContractInstanceNotFound(
                    String.format("Contract with such %s address was not found", address));
        }
        return instance;
    }

    private ErisAccount prepareErisAccount(String name) {
        ErisAccount erisAccount = erisAccountsService.getByName(name);
        if (erisAccount == null) {
            throw new ErisAccountNotFoundException(String.format("Can not find eris account: %s", name));
        }
        return erisAccount;
    }

    enum Foundation {

        Foundation("foundation"),
        Creator("creator"),
        FundingGoal("fundingGoal"),
        AmountRaised("amountRaised"),
        DurationInMinutes("deadline"),
        OnGoalReached("closeOnGoalReached"),
        ContractRemains("contractRemains"),
        MainToken("mainToken");

        private String field;

        Foundation(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }
}
