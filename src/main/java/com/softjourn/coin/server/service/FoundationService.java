package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.FoundationInfoDTO;
import com.softjourn.coin.server.dto.ApproveDTO;
import com.softjourn.coin.server.dto.WithdrawDTO;
import com.softjourn.coin.server.entity.Transaction;

import java.io.IOException;
import java.security.Principal;

public interface FoundationService {

    Transaction approve(ApproveDTO dto, Principal principal) throws IOException;

    Transaction close(String address, Principal principal) throws IOException;

    Transaction withdraw(String contractAddress, WithdrawDTO dto) throws IOException;

    FoundationInfoDTO getInfo(String address) throws IOException;

}
