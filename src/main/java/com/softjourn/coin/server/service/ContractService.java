package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.ContractCreateResponseDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.Type;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ContractService {

    ContractCreateResponseDTO newContract(NewContractDTO dto);

    List<Contract> getContracts();

    List<Type> getTypes();

    List<Contract> getContractsByType(String type);

    Contract getContractsByAddress(String address);

    List<ContractCreateResponseDTO> getInstances(Long id);

    ContractCreateResponseDTO newInstance(NewContractInstanceDTO dto);

    List<Map<String, String>> getContractConstructorInfo(Long id) throws IOException;

}
