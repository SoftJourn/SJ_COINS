package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.ContractCreateResponseDTO;
import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.Instance;
import com.softjourn.coin.server.entity.Type;
import com.softjourn.coin.server.exceptions.ContractNotFoundException;
import com.softjourn.coin.server.exceptions.TypeNotFoundException;
import com.softjourn.coin.server.repository.ContractRepository;
import com.softjourn.coin.server.repository.InstanceRepository;
import com.softjourn.coin.server.repository.TypeRepository;
import com.softjourn.eris.contract.ContractUnit;
import com.softjourn.eris.contract.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.softjourn.eris.contract.Util.parseAbi;

@Service
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;

    private final InstanceRepository instanceRepository;

    private final TypeRepository typeRepository;

    private final ErisContractService contractService;

    @Autowired
    public ContractServiceImpl(ContractRepository contractRepository, InstanceRepository instanceRepository,
                               TypeRepository typeRepository, ErisContractService contractService) {
        this.contractRepository = contractRepository;
        this.instanceRepository = instanceRepository;
        this.typeRepository = typeRepository;
        this.contractService = contractService;
    }


    @Override
    public ContractCreateResponseDTO newContract(NewContractDTO dto) {
        // deploy contract on eris
        com.softjourn.coin.server.entity.Type type = typeRepository.findOne(dto.getType());
        if (type != null) {
            com.softjourn.eris.contract.Contract erisContract = contractService.deploy(dto.getCode(),
                    dto.getAbi(), dto.getParameters());
            // if deploy was successful save Contract data into db
            Contract contract = new Contract();
            contract.setName(dto.getName());
            contract.setType(type);
            contract.setAbi(dto.getAbi());
            contract.setCode(dto.getCode());
            Contract newContract = contractRepository.save(contract);
            // save data about new contact instance
            Instance instance = new Instance();
            instance.setName(contract.getName());
            instance.setAddress(erisContract.getAddress());
            instance.setContract(newContract);
            Instance newInstance = instanceRepository.save(instance);

            return new ContractCreateResponseDTO(newContract.getId(), newContract.getName(),
                    newContract.getType().getType(), newInstance.getAddress());
        } else {
            throw new TypeNotFoundException(String.format("Such contract type as %s does not exist!", dto.getType()));
        }
    }

    @Override
    public List<Contract> getContracts() {
        return contractRepository.findAll();
    }

    @Override
    public Contract getContractById(Long id) {
        return this.contractRepository.findOne(id);
    }

    @Override
    public List<Type> getTypes() {
        return this.typeRepository.findAll();
    }

    @Override
    public List<Contract> getContractsByType(String type) {
        return contractRepository.findContractByTypeType(type);
    }

    @Override
    public Contract getContractsByAddress(String address) {
        return instanceRepository.findByAddress(address).getContract();
    }

    @Override
    public List<ContractCreateResponseDTO> getInstances(Long id) {
        List<Instance> instances = instanceRepository.findByContractId(id);
        if (instances != null) {
            return instances.stream().map(instance ->
                    new ContractCreateResponseDTO(instance.getId(), instance.getName(),
                            instance.getContract().getType().getType(), instance.getAddress()))
                    .collect(Collectors.toList());
        } else {
            throw new ContractNotFoundException(String.format("Contract with id %d was not found", id));
        }
    }

    @Override
    public ContractCreateResponseDTO newInstance(NewContractInstanceDTO dto) {
        // look for existing contract
        Contract contract = contractRepository.findOne(dto.getContractId());
        if (contract != null) {
            // deploy contract on eris
            com.softjourn.eris.contract.Contract erisContract = contractService.deploy(contract.getCode(),
                    contract.getAbi(), dto.getParameters());
            // save data about new contact instance
            Instance instance = new Instance();
            instance.setAddress(erisContract.getAddress());
            instance.setName(dto.getName());
            instance.setContract(contract);
            Instance newInstance = instanceRepository.save(instance);

            return new ContractCreateResponseDTO(contract.getId(), instance.getName(),
                    contract.getType().getType(), newInstance.getAddress());
        } else {
            throw new ContractNotFoundException(String.format("Contract with id %d was not found", dto.getContractId()));
        }
    }

    @Override
    public List<Map<String, String>> getContractConstructorInfo(Long id) throws IOException {
        Contract contract = contractRepository.findOne(id);
        List<Map<String, String>> list = new ArrayList<>();
        if (contract != null) {
            HashMap<String, ContractUnit> map = parseAbi(contract.getAbi());
            for (Variable variable : map.get(null).getInputs()) {
                Map<String, String> inputs = new HashMap<>();
                inputs.put("name", variable.getName());
                inputs.put("type", variable.getType().toString());
                list.add(inputs);
            }
            return list;
        } else {
            throw new ContractNotFoundException(String.format("Contract with id %d was not found", id));
        }
    }

}
