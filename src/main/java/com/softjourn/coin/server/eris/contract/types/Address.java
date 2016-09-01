package com.softjourn.coin.server.eris.contract.types;


import static com.softjourn.coin.server.eris.contract.Util.leftPad;

public class Address extends Type<String> {

    private static final int DEFAULT_INPUT_LENGTH = 64;

    private static final int DEFAULT_ADDRESS_LENGTH = 40;

    private static final char DEFAULT_PADDING_CHAR = '0';

    private static final String HEX_PREFIX = "0x";

    private final int length;

    public Address() {
        length = 0;
    }

    public Address(int length) {
        this.length = length;
    }

    public static boolean isType(String name) {
        return name.matches("^address[0-9]*$");
    }

    @Override
    public String formatInput(String value) {
        if (value.startsWith(HEX_PREFIX)) return formatInput(value.substring(2));
        return leftPad(value, DEFAULT_INPUT_LENGTH, DEFAULT_PADDING_CHAR);
    }

    @Override
    public String formatOutput(String value) {
        int diff = value.length() - DEFAULT_ADDRESS_LENGTH;
        if (diff > 0) return value.substring(diff);
        return value;
    }

    @Override
    public boolean canRepresent(Object value) {
        return value instanceof String && ((String) value).matches("^(0x)?[0-9a-fA-F]+$");
    }

    @Override
    public String toString() {
        return "address";
    }

    @Override
    public Class<String> valueClass() {
        return String.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        return length == address.length;

    }

    @Override
    public int hashCode() {
        return length;
    }
}
