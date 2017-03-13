package com.softjourn.coin.server.service;

import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.dto.CrowdsaleInfoDTO;
import com.softjourn.coin.server.dto.CrowdsaleTransactionResultDTO;
import com.softjourn.coin.server.dto.DonateDTO;
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
import java.math.BigInteger;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CrowdsaleServiceImpl implements CrowdsaleService {

    private static final String DONATE = "approveAndCall";
    private static final String WITHDRAW = "safeWithdrawal";
    private static final String GET_TOKENS_COUNT = "getTokensCount";
    private static final String TOKENS_ACCUMULATED = "tokensAccumulated";
    private static final String TOKEN_AMOUNTS = "tokenAmounts";

    @Autowired
    private ErisContractService contractService;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private ErisAccountsService erisAccountsService;

    @Override
    @SaveTransaction
    public Transaction donate(DonateDTO dto, Principal principal) throws IOException {
        // look for instance address and eris account
        Instance instance = instanceRepository.findByAddress(dto.getContractAddress());
        ErisAccount erisAccount = erisAccountsService.getByName(principal.getName());
        if (instance == null) {
            throw new ErisContractInstanceNotFound(
                    String.format("Contract with such %s address was not found", dto.getContractAddress()));
        } else if (erisAccount == null) {
            throw new ErisAccountNotFoundException(String.format("Can not find eris account: %s", principal.getName()));
        } else {
            // get contract
            Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), erisAccount);
            // donate
            Response response = contract.call(DONATE, dto.getSpenderAddress(), dto.getAmount());
            if (response.getError() != null) {
                log.error(response.getError().toString());
                throw new ErisProcessingException(response.getError().getMessage());
            } else {
                Transaction transaction = TransactionsService.prepareTransaction(new CrowdsaleTransactionResultDTO((Boolean) response.getReturnValues().get(0)),
                        response.getTxParams().getTxId(), String.format("Donate coins %d", dto.getAmount()));

                transaction.setAccount(erisAccount.getAccount());
                transaction.setDestination(instance.getAccount());
                return transaction;
            }
        }
    }

    @Override
    @SaveTransaction
    public Transaction withDraw(String address) throws IOException {
        // look for instance address
        Instance instance = instanceRepository.findByAddress(address);
        if (instance == null) {
            throw new ErisContractInstanceNotFound(
                    String.format("Contract with such %s address was not found", address));
        } else {
            // get contract
            Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), null);
            // withdraw
            Response response = contract.call(WITHDRAW);
            if (response.getError() != null) {
                log.error(response.getError().toString());
                throw new ErisProcessingException(response.getError().getMessage());
            } else {

                Transaction transaction = TransactionsService.prepareTransaction(new CrowdsaleTransactionResultDTO((Boolean) response.getReturnValues().get(0)),
                        response.getTxParams().getTxId(), "Withdraw coins");

                transaction.setAccount(instance.getAccount());
                return transaction;
            }
        }
    }

    @Override
    public CrowdsaleInfoDTO getInfo(String address) throws IOException {
        CrowdsaleInfoDTO infoDTO = new CrowdsaleInfoDTO();
        // look for instance address
        Instance instance = instanceRepository.findByAddress(address);
        if (instance == null) {
            throw new ErisContractInstanceNotFound(
                    String.format("Contract with such %s address was not found", address));
        } else {
            // get contracts field info
            Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), null);
            for (Crowdsale crowdsale : Crowdsale.values()) {
                Map<String, Object> map = new HashMap<>();
                Response response = contract.call(crowdsale.getField());
                processResponseError(response);
                map.put("name", crowdsale.getField());
                map.put("value", response.getReturnValues().get(0));
                infoDTO.getInfo().add(map);
            }
            infoDTO.setTokens(getTokensInfo(contract));
            return infoDTO;
        }
    }


    // information about each currency that contract using
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

    private void processResponseError(Response response) {
        if (response.getError() != null) {
            log.error(response.getError().toString());
            throw new ErisProcessingException(response.getError().getMessage());
        }
    }

    enum Crowdsale {

        Beneficiary("beneficiary"),
        Creator("creator"),
        FundingGoal("fundingGoal"),
        AmountRaised("amountRaised"),
        DurationInMinutes("deadline"),
        OnGoalReached("closeOnGoalReached");

        private String field;

        Crowdsale(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

}
