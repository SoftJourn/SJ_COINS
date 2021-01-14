package com.softjourn.coin.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvokeResponseDTO {

    private String transactionID;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class General extends InvokeResponseDTO {
        private byte[] payload;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Uint extends InvokeResponseDTO {
        private Integer payload;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StringList extends InvokeResponseDTO {
        private List<String> payload;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectList extends InvokeResponseDTO {
        private List<FoundationProjectDTO> payload;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Balance extends InvokeResponseDTO {
        private BalancesDTO payload;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Balances extends InvokeResponseDTO {
        private List<BalancesDTO> payload;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoundationProject extends InvokeResponseDTO {
        private FoundationProjectDTO payload;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoundationViewList extends InvokeResponseDTO {
        private List<FoundationViewDTO> payload;
    }
}
