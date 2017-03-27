package com.softjourn.coin.server.service;

import com.softjourn.coin.server.aop.annotations.SaveTransaction;
import com.softjourn.coin.server.dto.ContractCreateResponseDTO;
import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.ErisAccountType;
import com.softjourn.coin.server.entity.Instance;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.Type;
import com.softjourn.coin.server.exceptions.ContractNotFoundException;
import com.softjourn.coin.server.exceptions.TypeNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ContractRepository;
import com.softjourn.coin.server.repository.ErisAccountRepository;
import com.softjourn.coin.server.repository.InstanceRepository;
import com.softjourn.coin.server.repository.TypeRepository;
import com.softjourn.eris.contract.ContractUnit;
import com.softjourn.eris.contract.Variable;
import com.softjourn.eris.contract.response.DeployResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.softjourn.eris.contract.Util.parseAbi;

@Service
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;

    private final InstanceRepository instanceRepository;

    private final TypeRepository typeRepository;

    private final ErisContractService contractService;

    private final AccountRepository accountRepository;

    @Autowired
    public ContractServiceImpl(ContractRepository contractRepository, InstanceRepository instanceRepository,
                               TypeRepository typeRepository, ErisContractService contractService, AccountRepository accountRepository, ErisAccountRepository erisAccountRepository) {
        this.contractRepository = contractRepository;
        this.instanceRepository = instanceRepository;
        this.typeRepository = typeRepository;
        this.contractService = contractService;
        this.accountRepository = accountRepository;
    }


    @Override
    @SaveTransaction
    @Transactional
    public synchronized Transaction newContract(NewContractDTO dto) {
        // deploy contract on eris
        com.softjourn.coin.server.entity.Type type = typeRepository.findOne(dto.getType());
        if (type != null) {
            DeployResponse deployResponse = contractService.deploy(dto.getCode(),
                    dto.getAbi(), dto.getParameters());
            // if deploy was successful save Contract data into db
            Contract contract = new Contract();
            contract.setName(dto.getName());
            contract.setType(type);
            contract.setAbi(dto.getAbi());
            contract.setCode(dto.getCode());
            contract.setActive(true);
            contractRepository.save(contract);
            // create account and eris account for new contract instance
            Account account = this.prepareAccount(contract.getType().toString(), dto.getName());
            Account newAccount = accountRepository.save(account);
            newAccount.setErisAccount(this.prepareErisAccount(newAccount, deployResponse.getContract().getAddress()));
            accountRepository.save(newAccount);
            // save data about new contact instance
            Instance instance = this.prepareInstance(contract, deployResponse.getContract(), dto.getName());
            instance.setAccount(newAccount);
            Instance newInstance = instanceRepository.save(instance);
            // prepare response object
            ContractCreateResponseDTO contractDTO = new ContractCreateResponseDTO(contract.getId(), newInstance.getName(),
                    contract.getType().getType(), newInstance.getAddress());
            // prepare transaction
            Transaction transaction = TransactionsService.prepareTransaction(contractDTO, deployResponse.getResult().getTx_id(),
                    String.format("Deploy contract %s", account.getFullName()));

            transaction.setAccount(newAccount);

            return transaction;
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
    public Contract changeActive(Long id) {
        Contract contract = contractRepository.findOne(id);
        contract.setActive(!contract.getActive());
        return contractRepository.save(contract);
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
    public Contract getContractsByAddress(String address) throws ContractNotFoundException {
        Instance instance = instanceRepository.findByAddress(address);
        if (instance == null)
            throw new ContractNotFoundException("There is no instance with address " + address);
        return instance.getContract();
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
    @Transactional
    @SaveTransaction
    public synchronized Transaction newInstance(NewContractInstanceDTO dto) {
        // look for existing contract
        Contract contract = contractRepository.findOne(dto.getContractId());
        if (contract != null) {
            // deploy contract on eris
            DeployResponse deployResponse = contractService.deploy(contract.getCode(),
                    contract.getAbi(), dto.getParameters());
            // create account and eris account for new contract instance
            Account account = this.prepareAccount(contract.getType().toString(), dto.getName());
            Account newAccount = accountRepository.save(account);
            newAccount.setErisAccount(this.prepareErisAccount(newAccount, deployResponse.getContract().getAddress()));
            accountRepository.save(newAccount);
            // save data about new contact instance
            Instance instance = this.prepareInstance(contract, deployResponse.getContract(), dto.getName());
            instance.setAccount(newAccount);
            Instance newInstance = instanceRepository.save(instance);
            // prepare response object
            ContractCreateResponseDTO contractDTO = new ContractCreateResponseDTO(contract.getId(), newInstance.getName(),
                    contract.getType().getType(), newInstance.getAddress());
            // prepare transaction
            Transaction transaction = TransactionsService.prepareTransaction(contractDTO, deployResponse.getResult().getTx_id(),
                    String.format("Deploy contract %s", account.getFullName()));

            transaction.setAccount(newAccount);

            return transaction;
        } else {
            throw new ContractNotFoundException(String.format("Contract with id %d was not found", dto.getContractId()));
        }
    }

    /**
     * Method prepares Instance object
     * @param contract
     * @param erisContract
     * @param name
     * @return Instance
     */
    private Instance prepareInstance(Contract contract, com.softjourn.eris.contract.Contract erisContract, String name) {
        Instance instance = new Instance();
        instance.setAddress(erisContract.getAddress());
        instance.setName(name);
        instance.setContract(contract);
        return instance;
    }

    /**
     * Method prepares Account object
     * @param type
     * @param name
     * @return Account
     */
    private Account prepareAccount(String type, String name) {
        Account account = new Account();
        if (type.equals("project")) {
            account.setAccountType(AccountType.PROJECT);
        } else {
            account.setAccountType(AccountType.CURRENCY);
        }
        account.setLdapId(UUID.randomUUID().toString());
        account.setFullName(name);
        return account;
    }

    /**
     * Method prepares ErisAccount object
     * @param account
     * @param address
     * @return ErisAccount
     */
    private ErisAccount prepareErisAccount(Account account, String address) {
        ErisAccount erisAccount = new ErisAccount();
        erisAccount.setAccount(account);
        erisAccount.setAddress(address);
        erisAccount.setPubKey("0000000000000000000000000000000000000000000000000000000000000000");
        erisAccount.setPrivKey("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        erisAccount.setType(ErisAccountType.PARTICIPANT);
        return erisAccount;
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
