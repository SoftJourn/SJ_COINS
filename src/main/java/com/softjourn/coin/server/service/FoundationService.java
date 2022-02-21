package com.softjourn.coin.server.service;

import com.softjourn.coin.server.config.ApplicationProperties;
import com.softjourn.coin.server.dto.AllowanceRequestDTO;
import com.softjourn.coin.server.dto.CreateFoundationProjectDTO;
import com.softjourn.coin.server.dto.FilterDTO;
import com.softjourn.coin.server.dto.FoundationDonationDTO;
import com.softjourn.coin.server.dto.FoundationProjectDTO;
import com.softjourn.coin.server.dto.FoundationViewDTO;
import com.softjourn.coin.server.dto.InvokeResponseDTO;
import com.softjourn.coin.server.dto.UpdateFoundationDTO;
import com.softjourn.coin.server.dto.WithdrawRequestDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.enums.Chaincode;
import com.softjourn.coin.server.entity.enums.Currency;
import com.softjourn.coin.server.entity.enums.FabricFoundationsFunction;
import com.softjourn.coin.server.entity.enums.ProjectStatus;
import com.softjourn.coin.server.exceptions.AccountNotFoundException;
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
import java.util.Optional;
import java.util.UUID;
import javax.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoundationService {

  private final FabricService fabricService;
  private final AccountsService accountsService;
  private final ApplicationProperties applicationProperties;

  /**
   * Create new foundation project.
   *
   * @param accountName Account name.
   * @param createDto Create project dto.
   * @return Transaction id.
   */
  public String create(String accountName, CreateFoundationProjectDTO createDto) {
    Account account = getAccount(accountName);

    StringBuilder imageFilename = new StringBuilder();
    imageFilename.append(UUID.randomUUID().toString().replaceAll("-", ""));

    // Check main image data.
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

    String filename = imageFilename.toString();
    File file = new File(applicationProperties.getImage().getStorage().getPath(), filename);
    byte[] fileBytes = Base64.getDecoder().decode(createDto.getImage());
    try {
      FileUtils.writeByteArrayToFile(file, fileBytes);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ValidationException(e.getMessage());
    }

    Map<String, Boolean> currencies = new HashMap<>();
    currencies.put(Currency.COINS.getValue(), true);
    FoundationProjectDTO project = new FoundationProjectDTO();
    project.setName(createDto.getName());
    project.setImage(imageFilename.toString());
    project.setFundingGoal(createDto.getFundingGoal());
    project.setCloseOnGoalReached(createDto.isCloseOnGoalReached());
    project.setDeadline(createDto.getDeadline());
    project.setAdminId(account.getEmail());
    project.setMainCurrency(Currency.COINS.getValue());
    project.setAcceptCurrencies(currencies);
    project.setDeadline(createDto.getDeadline());
    project.setWithdrawAllowed(true);
    project.setCategoryId(createDto.getCategoryId());
    project.setStatus(
        createDto.isDraft() ? ProjectStatus.DRAFT.getValue() : ProjectStatus.REVIEW.getValue());
    project.setDescription(createDto.getDescription());
    return fabricService.invoke(
        account.getEmail(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.CREATE,
        project,
        InvokeResponseDTO.class
    ).getTransactionID();
  }

  /**
   * Update existing project with new data.
   *
   * @param accountName Current account name.
   * @param updateDto Update DTO with new data.
   * @return Transaction id.
   */
  public String update(String accountName, UpdateFoundationDTO updateDto) {
    Account account = getAccount(accountName);

    if (StringUtils.hasText(updateDto.getImage())) {
      updateDto.setImage(saveImage(updateDto.getImage()).getName());
    }

    String txid = fabricService.invoke(
        account.getEmail(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.UPDATE,
        updateDto,
        InvokeResponseDTO.class
    ).getTransactionID();

    // TODO: Add deleting old image.

    return txid;
  }

  /**
   * Get list of existed projects.
   *
   * @return List on names of projects.
   */
  public List<FoundationViewDTO> getAll() {
    InvokeResponseDTO.FoundationViewList response = fabricService.query(
        applicationProperties.getTreasury().getAccount(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.GET_ALL,
        new FilterDTO(
            null,
            ProjectStatus.ACTIVE.getValue() | ProjectStatus.CLOSED.getValue()),
        InvokeResponseDTO.FoundationViewList.class
    );
    return response.getPayload();
  }

  /**
   * Get list of existed projects.
   *
   * @param accountName Account name.
   * @return List of projects.
   */
  public List<FoundationViewDTO> getAllByUser(String accountName) {
    Account account = getAccount(accountName);

    InvokeResponseDTO.FoundationViewList response = fabricService.query(
        account.getEmail(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.GET_ALL,
        new FilterDTO(
            account.getEmail(),
            ProjectStatus.ACTIVE.getValue() | ProjectStatus.CLOSED.getValue()),
        InvokeResponseDTO.FoundationViewList.class
    );
    return response.getPayload();
  }

  /**
   * Get list of existed projects.
   *
   * @param accountName Account name.
   * @return List of projects.
   */
  public List<FoundationViewDTO> getMy(String accountName) {
    Account account = getAccount(accountName);

    InvokeResponseDTO.FoundationViewList response = fabricService.query(
        account.getEmail(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.GET_ALL,
        new FilterDTO(account.getEmail(), 0),
        InvokeResponseDTO.FoundationViewList.class
    );
    return response.getPayload();
  }

  /**
   * Get one project by its name.
   *
   * @param accountName Account name.
   * @param name Foundation project name.
   * @return Foundation project object.
   */
  public FoundationViewDTO getOneByName(String accountName, String name) {
    Account account = getAccount(accountName);

    InvokeResponseDTO.FoundationView response = fabricService.query(
        account.getEmail(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.GET_ONE,
        new String[]{name},
        InvokeResponseDTO.FoundationView.class
    );
    return response.getPayload();
  }

  /**
   * Donate to project.
   *
   * @param accountName Account name.
   * @param donation Dotation data.
   * @return Transaction id.
   */
  public String donate(String accountName, FoundationDonationDTO donation) {
    Account account = getAccount(accountName);

    return fabricService.invoke(
        account.getEmail(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.DONATE,
        donation,
        InvokeResponseDTO.class
    ).getTransactionID();
  }

  /**
   * Close foundation project.
   *
   * @param accountName Account name.
   * @param projectName Project name.
   * @return Remained amount of funds on project.
   */
  public Integer close(String accountName, String projectName) {
    Account account = getAccount(accountName);

    return fabricService.invoke(
        account.getEmail(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.CLOSE,
        new String[]{projectName},
        InvokeResponseDTO.Uint.class
    ).getPayload();
  }

  /**
   * Set withdraw allowance for project.
   *
   * @param accountName Account name.
   * @param request Allowance request payload.
   * @return Transaction id.
   */
  public String setAllowance(String accountName, AllowanceRequestDTO request) {
    Account account = getAccount(accountName);

    return fabricService.invoke(
        account.getEmail(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.SET_ALLOWANCE,
        request,
        InvokeResponseDTO.class
    ).getTransactionID();
  }

  /**
   * Withdraw foundation project.
   *
   * @param accountName Account name.
   * @param request Withdraw request payload.
   * @return Transaction id.
   */
  public String withdraw(String accountName, WithdrawRequestDTO request) {
    Account account = getAccount(accountName);

    return fabricService.invoke(
        account.getEmail(),
        Chaincode.FOUNDATION,
        FabricFoundationsFunction.WITHDRAW,
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
    String fullPath = applicationProperties.getImage().getStorage().getPath() + "/" + uri;
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

  private Account getAccount(String name) {
    return Optional.of(accountsService.getAccount(name))
        .orElseThrow(() -> new AccountNotFoundException(name));
  }

  /**
   * Save image and return file of image.
   *
   * @param image Image data.
   * @return File of saved image.
   */
  private File saveImage(String image) {
    StringBuilder imageFilename = new StringBuilder();
    imageFilename.append(UUID.randomUUID().toString().replaceAll("-", ""));

    // Check main image data.
    if (image.contains("data:image/png;")) {
      image = image.replace("data:image/png;base64,", "");
      imageFilename.append(".png");
    } else if (image.contains("data:image/jpeg;")) {
      image = image.replace("data:image/jpeg;base64,", "");
      imageFilename.append(".jpeg");
    } else {
      throw new ValidationException("Image has not supported type.");
    }

    String imagePath = "/static/images/project";
    String path = getClass().getResource(imagePath).getPath();
    String filename = imageFilename.toString();
    File file = new File(path, filename);
    byte[] fileBytes = Base64.getDecoder().decode(image);
    try {
      FileUtils.writeByteArrayToFile(file, fileBytes);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ValidationException(e.getMessage());
    }

    return file;
  }
}
