package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.ContractCreateResponseDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.Instance;
import com.softjourn.coin.server.repository.ContractRepository;
import com.softjourn.coin.server.repository.InstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private ErisContractService contractService;


    @Override
    public ContractCreateResponseDTO newContract(NewContractDTO dto) {
        // deploy contract on eris
        com.softjourn.eris.contract.Contract erisContract = contractService.deploy(dto.getCode(),
                dto.getAbi(), dto.getParameters());
        // if deploy was successful save Contract data into db
        Contract contract = new Contract();
        contract.setName(dto.getName());
        contract.setAbi(dto.getAbi());
        contract.setCode(dto.getCode());
        Contract newContract = contractRepository.save(contract);
        // save data about new contact instance
        Instance instance = new Instance();
        instance.setAddress(erisContract.getAddress());
        instance.setContract(newContract);
        Instance newInstance = instanceRepository.save(instance);

        return new ContractCreateResponseDTO(newContract.getId(), newContract.getName(), newInstance.getAddress());
    }

    @Override
    public List<Contract> getContracts() {
        return contractRepository.findAll();
    }

    @Override
    public Contract getContract(Long id) {
        return contractRepository.findOne(id);
    }

    @Override
    public List<ContractCreateResponseDTO> getInstances(Long id) {
        List<Instance> instances = instanceRepository.findByContractId(id);
        return instances.stream().map(instance ->
                new ContractCreateResponseDTO(instance.getContract().getId(), instance.getContract().getName(),
                        instance.getAddress()))
                .collect(Collectors.toList());
    }

    @Override
    public ContractCreateResponseDTO newInstance(NewContractInstanceDTO dto) {
        // look for existing contract
        Contract contract = contractRepository.findOne(dto.getContractId());
        // deploy contract on eris
        com.softjourn.eris.contract.Contract erisContract = contractService.deploy(contract.getCode(),
                contract.getAbi(), dto.getParameters());
        // save data about new contact instance
        Instance instance = new Instance();
        instance.setAddress(erisContract.getAddress());
        instance.setContract(contract);
        Instance newInstance = instanceRepository.save(instance);

        return new ContractCreateResponseDTO(contract.getId(), contract.getName(), newInstance.getAddress());
    }

}
