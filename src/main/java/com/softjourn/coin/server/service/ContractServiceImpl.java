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
import com.softjourn.coin.server.exceptions.ErisContractNotAllowedToCreate;
import com.softjourn.coin.server.exceptions.TypeNotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.ContractRepository;
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
                               TypeRepository typeRepository, ErisContractService contractService, AccountRepository accountRepository) {
        this.contractRepository = contractRepository;
        this.instanceRepository = instanceRepository;
        this.typeRepository = typeRepository;
        this.contractService = contractService;
        this.accountRepository = accountRepository;
    }


    /**
     * Method creates deploys contract, if deployment was successful then saves contracts data into db
     * and returns result of deployment
     *
     * @param dto
     * @return Transaction
     */
    @Override
    @SaveTransaction(comment = "New contract deploying")
    @Transactional
    public Transaction newContract(NewContractDTO dto) {
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
            Contract newContract = contractRepository.save(contract);
            // create account and eris account for new contract instance
            Account account = this.prepareAccount(newContract.getType().toString(), dto.getName());
            Account newAccount = accountRepository.save(account);
            newAccount.setErisAccount(this.prepareErisAccount(newAccount, deployResponse.getContract().getAddress()));
            accountRepository.save(newAccount);
            // save data about new contact instance
            Instance instance = this.prepareInstance(newContract, deployResponse.getContract(), dto.getName());
            instance.setAccount(newAccount);
            Instance newInstance = instanceRepository.save(instance);
            // prepare response object
            ContractCreateResponseDTO contractDTO = new ContractCreateResponseDTO(newContract.getId(), newInstance.getName(),
                    newContract.getType().getType(), newInstance.getAddress());
            // prepare transaction
            Transaction transaction = TransactionsService.prepareTransaction(contractDTO, deployResponse.getResult().getTx_id(),
                    String.format("Deploy contract %s", account.getFullName()));

            transaction.setAccount(newAccount);

            return transaction;
        } else {
            throw new TypeNotFoundException(String.format("Such contract type as %s does not exist!", dto.getType()));
        }
    }

    /**
     * Method gets information about contracts
     *
     * @return List<Contract>
     */
    @Override
    public List<Contract> getContracts() {
        return contractRepository.findAll();
    }

    /**
     * Method gets information about one specific contract
     *
     * @param id
     * @return Contract
     */
    @Override
    public Contract getContractById(Long id) {
        return this.contractRepository.findOne(id);
    }

    /**
     * Method changes active property of one specific contract
     *
     * @param id
     * @return Contract
     */
    @Override
    public Contract changeActive(Long id) {
        Contract contract = contractRepository.findOne(id);
        contract.setActive(!contract.getActive());
        return contractRepository.save(contract);
    }

    /**
     * Method returns all types of contracts
     *
     * @return List<Type>
     */
    @Override
    public List<Type> getTypes() {
        return this.typeRepository.findAll();
    }

    /**
     * Method returns all contracts by specific type
     *
     * @param type
     * @return List<Contract>
     */
    @Override
    public List<Contract> getContractsByType(String type) {
        return contractRepository.findContractByTypeType(type);
    }

    /**
     * Method returns contract by specific address
     *
     * @param address
     * @return Contract
     * @throws ContractNotFoundException
     */
    @Override
    public Contract getContractsByAddress(String address) throws ContractNotFoundException {
        Instance instance = instanceRepository.findByAddress(address);
        if (instance == null)
            throw new ContractNotFoundException("There is no instance with address " + address);
        return instance.getContract();
    }

    /**
     * Method gets all instances of specific contract
     *
     * @param id
     * @return List<ContractCreateResponseDTO>
     */
    @Override
    public List<ContractCreateResponseDTO> getInstancesByContractId(Long id) {
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

    /**
     * Method creates new instance of existing contract
     *
     * @param dto
     * @return Transaction
     */
    @Override
    @Transactional
    @SaveTransaction(comment = "New contract instance creating")
    public Transaction newInstance(NewContractInstanceDTO dto) {
        // look for existing contract
        Contract contract = contractRepository.findOne(dto.getContractId());
        if (contract != null) {
            // check is contract active
            isAllowedToCreate(contract);
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
     *
     * @param contract     database Contract entity
     * @param erisContract Eris contract object
     * @param name         name for new instance
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
     *
     * @param type Contract type string
     * @param name Contract instance name
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
     *
     * @param account Database Account entity
     * @param address Eris address of account
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

    private void isAllowedToCreate(Contract contract) {
        if (!contract.getActive())
            throw new ErisContractNotAllowedToCreate("This contract impossible to create, because he isn't active.");
    }

    /**
     * Method gets contract by id, reads constructor parameters of contract and returns them
     *
     * @param id
     * @return List<Map<String, String>>
     * @throws IOException
     */
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
