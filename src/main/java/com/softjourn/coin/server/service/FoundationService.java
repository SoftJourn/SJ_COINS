package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.FoundationProjectDTO;
import com.softjourn.coin.server.dto.InvokeResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoundationService {

  private final FabricService fabricService;

  public String create(String account, FoundationProjectDTO project) {
    return fabricService.invoke(
        account,
        "createFoundation",
        project,
        InvokeResponseDTO.class
    ).getTransactionID();
  }

  public String getAll(String account) {
    return fabricService.query(
        account,
        "getFoundations",
        new String[]{},
        InvokeResponseDTO.class
    ).getTransactionID();
  }
}
