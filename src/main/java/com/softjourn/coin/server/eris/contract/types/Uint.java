package com.softjourn.coin.server.eris.contract.types;


import com.softjourn.coin.server.eris.contract.Util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Uint extends Type<BigDecimal> {

    private static final int DEFAULT_INPUT_LENGTH = 64;

    private static final char DEFAULT_PADDING_CHAR = '0';

    private final int length;

    public Uint() {
        length = 0;
    }

    public Uint(int length) {
        this.length = length;
    }

    public static boolean isType(String name) {
        return name.matches("^uint[0-9]*$");
    }

    @Override
    public String toString() {
        return "uint" + (length > 0 ? length : "");
    }

    @Override
    public Class<BigDecimal> valueClass() {
        return BigDecimal.class;
    }

    @Override
    public String formatInput(BigDecimal value) {
        return Util.leftPad(value.toBigInteger().toString(16), DEFAULT_INPUT_LENGTH, DEFAULT_PADDING_CHAR).toUpperCase();
    }

    @Override
    public BigDecimal formatOutput(String value) {
        return new BigDecimal(new BigInteger(value, 16));
    }

    @Override
    public boolean canRepresent(Object value) {
        return value instanceof BigDecimal ||
                value instanceof BigInteger ||
                value instanceof Integer ||
                value instanceof Long ||
                (value instanceof  String && canParse((String) value));
    }

    private boolean canParse(String val) {
        try {
            new BigInteger(val, 16);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Uint uint = (Uint) o;

        return length == uint.length;

    }

    @Override
    public int hashCode() {
        return length;
    }
}
