package com.softjourn.coin.server.eris.contract.types;

import junit.framework.TestCase;


public class AddressTest extends TestCase {

    private Address address = new Address();

    public void testIsType() throws Exception {
        assertTrue(Address.isType("address"));
        assertTrue(Address.isType("address16"));
        assertTrue(Address.isType("address256"));
        assertFalse(Address.isType("uint"));
    }

    public void testFormatInput() throws Exception {
        String actual = address.formatInput("1111111111111111111111111111111111111111");
        String expected = "0000000000000000000000001111111111111111111111111111111111111111";

        assertEquals(expected, actual);
    }

    public void testFormatOutput() throws Exception {
        String actual = address.formatOutput("0000000000000000000000001111111111111111111111111111111111111111");
        String expected = "1111111111111111111111111111111111111111";

        assertEquals(expected, actual);
    }

    public void testCanRepresent() throws Exception {
        assertTrue(address.canRepresent("1111111111111111111111111111111111111111"));
        assertFalse(address.canRepresent(1111111));
    }

    public void testToString() throws Exception {
        assertEquals("address", address.toString());
    }

}