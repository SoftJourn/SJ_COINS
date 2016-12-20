package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.CreateContractResponseDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @PreAuthorize("authenticated")
    @RequestMapping(method = RequestMethod.POST)
    public CreateContractResponseDTO createNewContract(@RequestBody NewContractDTO dto) {
        return this.contractService.newContract(dto);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/instance", method = RequestMethod.POST)
    public CreateContractResponseDTO deployInstanceOfExistingContract(@RequestBody NewContractInstanceDTO dto) {
        return this.contractService.newInstance(dto);
    }

}
