package com.softjourn.coin.server.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.softjourn.coin.server.entity.TransactionStatus;
import com.softjourn.coin.server.util.InstantJsonSerializer;
import com.softjourn.coin.server.util.JsonViews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleReplenichmentResponseDTO {

    private String fullName;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    private BigDecimal amount;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    private TransactionStatus status;

    @JsonView({JsonViews.DETAILED.class, JsonViews.REGULAR.class})
    @JsonSerialize(using = InstantJsonSerializer.class)
    private Instant time;

}
