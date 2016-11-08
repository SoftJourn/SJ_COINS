package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDTO {
    private String name;
    private String uniqueId;
}
