package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.AllowanceRequestDTO;
import com.softjourn.coin.server.dto.CreateFoundationProjectDTO;
import com.softjourn.coin.server.dto.FoundationDonationDTO;
import com.softjourn.coin.server.dto.FoundationProjectDTO;
import com.softjourn.coin.server.dto.FoundationViewDTO;
import com.softjourn.coin.server.dto.InvokeResponseDTO;
import com.softjourn.coin.server.dto.WithdrawRequestDTO;
import com.softjourn.coin.server.entity.enums.Chaincode;
import com.softjourn.coin.server.entity.enums.FabricFoundationsFunction;
import com.softjourn.coin.server.exceptions.NotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoundationService {

  private final FabricService fabricService;

  @Value("${image.path:/static/images/project}")
  private String imagePath;

  /**
   * Create new foundation project.
   *
   * TODO: Change return type to foundation object.
   * @param account Account name.
   * @param createDto Create project dto.
   * @return
   */
  public String create(String account, CreateFoundationProjectDTO createDto) {
    StringBuffer imageFilename = new StringBuffer();
    imageFilename.append(UUID.randomUUID().toString().replaceAll("-", ""));

    if (Objects.isNull(createDto.getImage()) || createDto.getImage().isEmpty()) {
      throw new ValidationException("Image is not uploaded");
    } else if (createDto.getImage().contains("data:image/png;")) {
      createDto.setImage(createDto.getImage().replace("data:image/png;base64,", ""));
      imageFilename.append(".png");
    } else if (createDto.getImage().contains("data:image/jpeg;")) {
      createDto.setImage(createDto.getImage().replace("data:image/jpeg;base64,", ""));
      imageFilename.append(".jpeg");
    } else {
      throw new ValidationException("Image has not supported type.");
    }

    String path = getClass().getResource("/static/images/project").getPath();
    String filename = imageFilename.toString();
    File file = new File(path, filename);
    byte[] fileBytes = Base64.getDecoder().decode(createDto.getImage());
    try {
      FileUtils.writeByteArrayToFile(file, fileBytes);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ValidationException(e.getMessage());
    }

    Map<String, Boolean> currencies = new HashMap<>();
    currencies.put("coins", true);
    FoundationProjectDTO project = new FoundationProjectDTO();
    project.setName(createDto.getName());
    project.setImage(imageFilename.toString());
    project.setFundingGoal(createDto.getFundingGoal());
    project.setCloseOnGoalReached(createDto.isCloseOnGoalReached());
    project.setDeadline(createDto.getDeadline());
    project.setAdminId("vzaichuk@softjourn.com");
    project.setCreatorId("vzaichuk@softjourn.com");
    project.setMainCurrency("coins");
    project.setAcceptCurrencies(currencies);
    project.setDeadline(200L);
    project.setWithdrawAllowed(true);
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
  public List<FoundationViewDTO> getAll(String account) {
    InvokeResponseDTO.FoundationViewList response = fabricService.query(
        account,
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.GET_ALL.getName(),
        new String[]{},
        InvokeResponseDTO.FoundationViewList.class
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
  public FoundationViewDTO getOneByName(String account, String name) {
//    coinService.fillAccount("vzaichuk@softjourn.com", new BigDecimal(100), null);
    InvokeResponseDTO.FoundationView response = fabricService.query(
        account,
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.GET_ONE.getName(),
        new String[]{name},
        InvokeResponseDTO.FoundationView.class
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
  public String donate(String account, FoundationDonationDTO donation) {
    return fabricService.invoke(
        account,
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.DONATE.getName(),
        donation,
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
   * Set withdraw allowance for project.
   *
   * @param account Account name.
   * @param request Allowance request payload.
   * @return Transaction id.
   */
  public String setAllowance(String account, AllowanceRequestDTO request) {
    return fabricService.invoke(
        account,
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.SET_ALLOWANCE.getName(),
        request,
        InvokeResponseDTO.class
    ).getTransactionID();
  }

  /**
   * Withdraw foundation project.
   *
   * @param account Account name.
   * @param request Withdraw request payload.
   * @return Transaction id.
   */
  public String withdraw(String account, WithdrawRequestDTO request) {
    return fabricService.invoke(
        account,
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.WITHDRAW.getName(),
        request,
        InvokeResponseDTO.class
    ).getTransactionID();
  }

  /**
   * Get project image.
   *
   * @param uri Image URI.
   * @return
   */
  public byte[] getImage(String uri) {
    String fullPath = getClass().getResource(imagePath).getPath() + "/" + uri;
    File file = new File(fullPath);
    InputStream in;
    try {
      in = new FileInputStream(file);
      return IOUtils.toByteArray(in);
    } catch (FileNotFoundException e) {
      throw new NotFoundException("There is no image with this passed uri");
    } catch (IOException e) {
      // Can't read file. Should never happened
      log.error("Method getImage uri. File can't be read", e);
      throw new RuntimeException("File can't be read");
    }
  }
}
