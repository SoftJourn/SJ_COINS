package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.DonationDTO;
import com.softjourn.coin.server.dto.FoundationProjectDTO;
import com.softjourn.coin.server.dto.InvokeResponseDTO;
import com.softjourn.coin.server.entity.enums.Chaincode;
import com.softjourn.coin.server.entity.enums.FabricFoundationsFunction;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoundationService {

  private final FabricService fabricService;

  /**
   * Create new foundation project.
   *
   * TODO: Change return type to foundation object.
   * @param account Account name.
   * @param project Project object.
   * @return
   */
  public String create(String account, FoundationProjectDTO project) {
    return fabricService.invoke(
        account,
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.CREATE.getName(),
        project,
        InvokeResponseDTO.class
    ).getTransactionID();
  }

  /**
   * Get names of existed projects.
   *
   * @param account Account name.
   * @return List on names of projects.
   */
  public List<String> getAll(String account) {
    InvokeResponseDTO.StringList response = fabricService.query(
        account,
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.GET_ALL.getName(),
        new String[]{},
        InvokeResponseDTO.StringList.class
    );
    return response.getPayload();
  }

  /**
   * Get one project by its name.
   *
   * @param account Account name.
   * @param name Foundation project name.
   * @return Foundation project object.
   */
  public FoundationProjectDTO getOneByName(String account, String name) {
    InvokeResponseDTO.FoundationProject response = fabricService.query(
        account,
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.GET_ONE.getName(),
        new String[]{name},
        InvokeResponseDTO.FoundationProject.class
    );
    return response.getPayload();
  }

  /**
   * Donate to project.
   *
   * @param account Account name.
   * @param donation Dotation data.
   * @return Transaction id.
   */
  public String donate(String account, String projectName, DonationDTO donation) {
    return fabricService.invoke(
        account,
        FabricFoundationsFunction.DONATE.getName(),
        new Object[]{projectName, donation},
        InvokeResponseDTO.class
    ).getTransactionID();
  }

  /**
   * Close foundation project.
   *
   * @param account Account name.
   * @param projectName Project name.
   * @return Remained amount of funds on project.
   */
  public Integer close(String account, String projectName) {
    return fabricService.invoke(
        account,
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.CLOSE.getName(),
        new String[]{projectName},
        InvokeResponseDTO.Uint.class
    ).getPayload();
  }

  /**
   * Withdraw foundation project.
   *
   * @param account Account name.
   * @param projectName Project name.
   * @return
   */
  public String withdraw(String account, String projectName) {
    return fabricService.invoke(
        account,
        FabricFoundationsFunction.WITHDRAW.getName(),
        new String[]{projectName},
        InvokeResponseDTO.class
    ).getTransactionID();
  }
}
