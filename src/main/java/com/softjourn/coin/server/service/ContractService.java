package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.ContractCreateResponseDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.entity.Contract;

import java.util.List;

public interface ContractService {

    ContractCreateResponseDTO newContract(NewContractDTO dto);

    List<Contract> getContracts();

    Contract getContract(Long id);

    List<ContractCreateResponseDTO> getInstances(Long id);

    ContractCreateResponseDTO newInstance(NewContractInstanceDTO dto);

}
