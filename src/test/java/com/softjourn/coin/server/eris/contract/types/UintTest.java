package com.softjourn.coin.server.eris.contract.types;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.math.BigInteger;

public class UintTest extends TestCase {

    Uint uint = new Uint();

    public void testIsType() throws Exception {
        assertTrue(Uint.isType("uint"));
        assertTrue(Uint.isType("uint256"));
        assertTrue(Uint.isType("uint32"));
        assertFalse(Uint.isType("int"));
        assertFalse(Uint.isType("address"));
    }

    public void testToString() throws Exception {
        assertEquals("uint", uint.toString());
        assertEquals("uint256", new Uint(256).toString());
    }

    public void testFormatInput() throws Exception {

        assertEquals("0000000000000000000000000000000000000000000000000000000000000005", uint.formatInput(new BigDecimal(5)));
        assertEquals("000000000000000000000000000000000000000000000000000000000000000A", uint.formatInput(new BigDecimal(10)));
        assertEquals("0000000000000000000000000000000000000000000000000000000000000100", uint.formatInput(new BigDecimal(256)));
    }

    public void testFormatOutput() throws Exception {
        assertEquals(new BigDecimal(5), uint.formatOutput("0000000000000000000000000000000000000000000000000000000000000005"));
        assertEquals(new BigDecimal(10), uint.formatOutput("000000000000000000000000000000000000000000000000000000000000000A"));
        assertEquals(new BigDecimal(256), uint.formatOutput("0000000000000000000000000000000000000000000000000000000000000100"));
    }

    public void testCanRepresent() throws Exception {
        assertTrue(uint.canRepresent(5));
        assertTrue(uint.canRepresent(new BigInteger("5")));
        assertTrue(uint.canRepresent(new BigDecimal(5)));
        assertTrue(uint.canRepresent("5"));
        assertFalse(uint.canRepresent("Test"));
        assertFalse(uint.canRepresent(new Object()));
    }

}