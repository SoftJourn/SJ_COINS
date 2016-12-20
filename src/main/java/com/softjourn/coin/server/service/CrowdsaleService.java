package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.CrowdsaleTransactionResultDTO;
import com.softjourn.coin.server.dto.DonateDTO;

import java.io.IOException;

public interface CrowdsaleService {

    CrowdsaleTransactionResultDTO donate(DonateDTO dto) throws IOException;

    CrowdsaleTransactionResultDTO withDraw(String address) throws IOException;

}
