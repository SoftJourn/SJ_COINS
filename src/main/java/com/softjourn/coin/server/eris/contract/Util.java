package com.softjourn.coin.server.eris.contract;


import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import java.security.MessageDigest;
import java.util.Arrays;

public class Util {

    private static final MessageDigest DEFAULT_DIGEST = new Keccak.Digest256();

    /**
     * Get hexadecimal string representation of Keccak256 hash of passed param
     * @param val value to be hashed
     * @return hashed value
     */
    public static String hash(String val) {
        byte[] hash = DEFAULT_DIGEST.digest(val.getBytes());
        return Hex.encodeHexString(hash);
    }

    /**
     * Fill left side of passed value with passed {@param c}
     * to make it required {@param length}.
     * If value length is already greater or equals returns {@param val} without any changes
     * @param val value to be padded
     * @param length required length
     * @param c chat to fill with
     * @return padded to required length value
     */
    public static String leftPad(String val, int length, char c) {
        int diff = length - val.length();
        if (diff > 0) {
            char[] pad = new char[diff];
            Arrays.fill(pad, c);
            return new String(pad) + val;
        }
        return val;
    }
}
