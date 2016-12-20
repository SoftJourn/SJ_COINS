package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.CreateContractResponseDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.Instance;
import com.softjourn.coin.server.repository.ContractRepository;
import com.softjourn.coin.server.repository.InstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private ErisContractService contractService;


    @Override
    public CreateContractResponseDTO newContract(NewContractDTO dto) {
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

        return new CreateContractResponseDTO(newContract.getId(), newInstance.getAddress());
    }

    @Override
    public CreateContractResponseDTO newInstance(NewContractInstanceDTO dto) {
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

        return new CreateContractResponseDTO(contract.getId(), newInstance.getAddress());
    }

}
