package com.softjourn.coin.server.service;

import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.dto.CrowdsaleTransactionResultDTO;
import com.softjourn.coin.server.dto.DonateDTO;
import com.softjourn.coin.server.entity.Instance;
import com.softjourn.coin.server.exceptions.ErisContractInstanceNotFound;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ContractRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.coin.server.repository.InstanceRepository;
import com.softjourn.eris.contract.Contract;
import com.softjourn.eris.contract.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class CrowdsaleServiceImpl implements CrowdsaleService {

    private static final String DONATE = "approveAndCall";
    private static final String WITHDRAW = "safeWithdrawal";

    @Autowired
    private ErisContractService contractService;

    @Autowired
    private InstanceRepository instanceRepository;

    @Override
    @SaveTransaction(comment = "Donate money.")
    public CrowdsaleTransactionResultDTO donate(DonateDTO dto) throws IOException {
        Instance instance = instanceRepository.findByAddress(dto.getContractAddress());
        if (instance == null) {
            throw new ErisContractInstanceNotFound(
                    String.format("Contract with such %s address was not found", dto.getContractAddress()));
        } else {
            Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress());
            Response response = contract.call(DONATE, dto.getSpenderAddress(), dto.getAmount());
            if (response.getError() != null) {
                log.error(response.getError().toString());
                return new CrowdsaleTransactionResultDTO(false);
            } else {
                return new CrowdsaleTransactionResultDTO(true);
            }
        }
    }

    @Override
    @SaveTransaction(comment = "Withdraw money.")
    public CrowdsaleTransactionResultDTO withDraw(String address) throws IOException {
        Instance instance = instanceRepository.findByAddress(address);
        if (instance == null) {
            throw new ErisContractInstanceNotFound(
                    String.format("Contract with such %s address was not found", address));
        } else {
            Contract contract = contractService.getContract(instance.getContract().getAbi(), instance.getAddress());
            Response response = contract.call(WITHDRAW);
            if (response.getError() != null) {
                log.error(response.getError().toString());
                return new CrowdsaleTransactionResultDTO(false);
            } else {
                return new CrowdsaleTransactionResultDTO(true);
            }
        }
    }

}
