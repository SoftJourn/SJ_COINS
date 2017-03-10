package com.softjourn.coin.server.aop.aspects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CallsLoggingAspectTest {

    @Spy
    private CallsLoggingAspect loggingAspect;

    @Test
    public void matchNamesWithArgs_empty() throws Exception {
        assertEquals("[]", loggingAspect.matchNamesWithArgs(new String[0], new String[0]));
    }

    @Test
    public void matchNamesWithArgs_null() throws Exception {
        assertEquals("[]", loggingAspect.matchNamesWithArgs(null, new String[0]));
        assertEquals("[]", loggingAspect.matchNamesWithArgs(new String[0], null));
        assertEquals("[]", loggingAspect.matchNamesWithArgs(null, null));
    }

    @Test
    public void matchNamesWithArgs_goodCase() throws Exception {
        assertEquals("[account=testAccount]", loggingAspect.matchNamesWithArgs(new String[]{"account"}, new String[]{"testAccount"}));
    }

    @Test
    public void matchNamesWithArgs_nullArgs() throws Exception {
        assertEquals("[account=null]", loggingAspect.matchNamesWithArgs(new String[]{"account"}, new String[]{null}));
    }

}