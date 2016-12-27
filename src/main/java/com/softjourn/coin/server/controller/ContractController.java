package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.ContractCreateResponseDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @PreAuthorize("authenticated")
    @RequestMapping(method = RequestMethod.POST)
    public ContractCreateResponseDTO createNewContract(@RequestBody NewContractDTO dto) {
        return this.contractService.newContract(dto);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(method = RequestMethod.GET)
    public List<Contract> getContracts() {
        return this.contractService.getContracts();
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Contract getContract(@PathVariable Long id) {
        return this.contractService.getContract(id);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/instances", method = RequestMethod.POST)
    public ContractCreateResponseDTO deployInstanceOfExistingContract(@RequestBody NewContractInstanceDTO dto) {
        return this.contractService.newInstance(dto);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/instances/{id}", method = RequestMethod.GET)
    public List<ContractCreateResponseDTO> getInstances(@PathVariable Long id) {
        return this.contractService.getInstances(id);
    }

}
