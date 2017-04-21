package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleReplenishmentRequestDTO {

    private String start;

    private String due;

    private PageRequestImpl pageable;

    @Override
    public String toString() {
        return "SingleReplenishmentRequestDTO{" +
                "start='" + start + '\'' +
                ", due='" + due + '\'' +
                ", pageable=" + pageable +
                '}';
    }
}
