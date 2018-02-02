package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.EnrollResponseDTO;
import org.springframework.http.ResponseEntity;

public interface FabricService {

    ResponseEntity<EnrollResponseDTO> enroll(String email);

    <T> T invoke(String email, String function, String[] args, Class<T> responseType);
}
