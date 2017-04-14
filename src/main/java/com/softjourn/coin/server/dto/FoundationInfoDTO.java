package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class FoundationInfoDTO {

    public FoundationInfoDTO() {
        info = new ArrayList<>();
        withdrawInfo = new ArrayList<>();
        tokens = new ArrayList<>();
    }

    List<Map<String, Object>> info;

    List<Map<String, Object>> tokens;

    List<Map<String, Object>> withdrawInfo;

}
