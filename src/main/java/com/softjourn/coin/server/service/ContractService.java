package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.NewContractDTO;
import com.softjourn.coin.server.dto.CreateContractResponseDTO;
import com.softjourn.coin.server.dto.NewContractInstanceDTO;

public interface ContractService {

    CreateContractResponseDTO newContract(NewContractDTO dto);

    CreateContractResponseDTO newInstance(NewContractInstanceDTO dto);

}
