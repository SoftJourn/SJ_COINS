package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.CrowdsaleTransactionResultDTO;
import com.softjourn.coin.server.dto.DonateDTO;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.Instance;
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
    public CrowdsaleTransactionResultDTO donate(DonateDTO dto, Principal principal) throws IOException {
        Instance instance = instanceRepository.findByAddress(dto.getContractAddress());
        ErisAccount erisAccount = erisAccountsService.getByName(principal.getName());
        if (instance == null) {
            throw new ErisContractInstanceNotFound(
                    String.format("Contract with such %s address was not found", dto.getContractAddress()));
        } else if (erisAccount == null) {
            throw new ErisAccountNotFoundException(String.format("Can not find eris account: %s", principal.getName()));
        } else {
            Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), erisAccount);
            Response response = contract.call(DONATE, dto.getSpenderAddress(), dto.getAmount());
            if (response.getError() != null) {
                log.error(response.getError().toString());
                throw new ErisProcessingException(response.getError().getMessage());
            } else {
                return new CrowdsaleTransactionResultDTO((Boolean) response.getReturnValues().get(0));
            }
        }
    }

    @Override
    public CrowdsaleTransactionResultDTO withDraw(String address) throws IOException {
        Instance instance = instanceRepository.findByAddress(address);
        if (instance == null) {
            throw new ErisContractInstanceNotFound(
                    String.format("Contract with such %s address was not found", address));
        } else {
            Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), null);
            Response response = contract.call(WITHDRAW);
            if (response.getError() != null) {
                log.error(response.getError().toString());
                throw new ErisProcessingException(response.getError().getMessage());
            } else {
                return new CrowdsaleTransactionResultDTO((Boolean) response.getReturnValues().get(0));
            }
        }
    }

    @Override
    public Map<String, Object> getInfo(String address) throws IOException {
        Instance instance = instanceRepository.findByAddress(address);
        if (instance == null) {
            throw new ErisContractInstanceNotFound(
                    String.format("Contract with such %s address was not found", address));
        } else {
            Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress(), null);
            Map<String, Object> map = new HashMap<>();
            for (Crowdsale crowdsale : Crowdsale.values()) {
                Response response = contract.call(crowdsale.getField());
                processResponseError(response);
                map.put(crowdsale.getField(), response.getReturnValues().get(0));
            }
            map.put("tokenAmounts", getTokensInfo(contract));
            return map;
        }
    }


    private List<Map<String, Object>> getTokensInfo(Contract contract) throws IOException {
        List<Map<String, Object>> maps = new ArrayList<>();
        Response tokensCountResponse = contract.call(GET_TOKENS_COUNT);
        processResponseError(tokensCountResponse);
        BigInteger tokensCount = (BigInteger) tokensCountResponse.getReturnValues().get(0);
        for (int i = 0; i < tokensCount.intValue(); i++) {
            Response token = contract.call(TOKENS_ACCUMULATED, BigInteger.valueOf(i));
            processResponseError(token);
            Response amount = contract.call(TOKEN_AMOUNTS, token.getReturnValues().get(0));
            processResponseError(amount);
            Map<String, Object> map = new HashMap<>();
            map.put("contractAddress", token.getReturnValues().get(0));
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
