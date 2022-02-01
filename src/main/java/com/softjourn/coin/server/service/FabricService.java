package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.EnrollResponseDTO;
import com.softjourn.coin.server.entity.enums.Chaincode;
import com.softjourn.coin.server.entity.enums.ChaincodeFunction;
import org.springframework.http.ResponseEntity;

public interface FabricService {

  ResponseEntity<EnrollResponseDTO> enroll(String email);

  <T> T invoke(
      String email, Chaincode chaincode, ChaincodeFunction function,
      Object args, Class<T> responseType);

  <T> T query(
      String email, Chaincode chaincode, ChaincodeFunction function,
      Object args, Class<T> responseType);
}
