package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.ContractCreateResponseDTO;
import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.Type;
import com.softjourn.coin.server.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/contracts")
public class ContractController {

    private final ContractService contractService;


    @Autowired
    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PreAuthorize("authenticated")
    @RequestMapping(method = RequestMethod.POST)
    public ContractCreateResponseDTO createNewContract(@Valid @RequestBody NewContractDTO dto) {
        return this.contractService.newContract(dto);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(method = RequestMethod.GET)
    public List<Contract> getContracts() {
        return this.contractService.getContracts();
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/compile",method = RequestMethod.POST)
    public String  compile(@RequestBody String json) {
        RestTemplate template = new RestTemplate();
        return template.postForObject("http://46.101.203.71/compile",json, String.class);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Contract getContract(@PathVariable Long id) {
        return this.contractService.getContractById(id);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/address/{address}", method = RequestMethod.GET)
    public Contract getContractByAddress(@PathVariable String address) {
        return this.contractService.getContractsByAddress(address);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/types", method = RequestMethod.GET)
    public List<Type> getContractTypes() {
        return this.contractService.getTypes();
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/types/{type}", method = RequestMethod.GET)
    public List<Contract> getContractByType(@PathVariable String type) {
        return this.contractService.getContractsByType(type);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public List<Map<String, String>> getContractInfo(@PathVariable Long id) throws IOException {
        return this.contractService.getContractConstructorInfo(id);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/instances", method = RequestMethod.POST)
    public ContractCreateResponseDTO deployInstanceOfExistingContract(@Valid @RequestBody NewContractInstanceDTO dto) {
        return this.contractService.newInstance(dto);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/instances/{id}", method = RequestMethod.GET)
    public List<ContractCreateResponseDTO> getInstances(@PathVariable Long id) {
        return this.contractService.getInstances(id);
    }

}
