package com.softjourn.coin.server.eris.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.softjourn.coin.server.eris.contract.types.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represent input or output variable in contract method
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Variable<T> {

    private String name;

    private Type<T> type;

}
