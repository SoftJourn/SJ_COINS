package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class CrowdsaleInfoDTO {

    public CrowdsaleInfoDTO() {
        info = new ArrayList<>();
        tokens = new ArrayList<>();
    }

    List<Map<String, Object>> info;

    List<Map<String, Object>> tokens;

}
