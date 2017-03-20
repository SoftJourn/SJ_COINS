package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.CrowdsaleInfoDTO;
import com.softjourn.coin.server.dto.DonateDTO;
import com.softjourn.coin.server.entity.Transaction;

import java.io.IOException;
import java.security.Principal;

public interface CrowdsaleService {

    Transaction donate(DonateDTO dto, Principal principal) throws IOException;

    Transaction withDraw(String address) throws IOException;

    CrowdsaleInfoDTO getInfo(String address) throws IOException;

}
