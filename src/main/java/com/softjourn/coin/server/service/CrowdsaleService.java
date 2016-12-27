package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.CrowdsaleTransactionResultDTO;
import com.softjourn.coin.server.dto.DonateDTO;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

public interface CrowdsaleService {

    CrowdsaleTransactionResultDTO donate(DonateDTO dto, Principal principal) throws IOException;

    CrowdsaleTransactionResultDTO withDraw(String address) throws IOException;

    Map<String, Object> getInfo(String address) throws IOException;

}
