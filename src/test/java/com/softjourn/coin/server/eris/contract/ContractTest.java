package com.softjourn.coin.server.eris.contract;

import com.softjourn.coin.server.eris.contract.types.Address;
import com.softjourn.coin.server.eris.contract.types.Type;
import com.softjourn.coin.server.eris.contract.types.Uint;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public class ContractTest extends TestCase {

    private ContractManager.ContractImpl testContract;

    public void setUp() throws Exception {
        Type uintType = new Uint(256);


        final ContractUnit getFunction = new ContractUnit();
        getFunction.setConstant(true);
        getFunction.setInputs(new Variable[0]);
        getFunction.setName("get");
        getFunction.setOutputs(new Variable[]{new Variable("retVal", uintType)});
        getFunction.setType(ContractUnitType.function);

        final ContractUnit setFunction = new ContractUnit();
        setFunction.setConstant(false);
        setFunction.setInputs(new Variable[]{new Variable("x", uintType)});
        setFunction.setName("set");
        setFunction.setOutputs(new Variable[0]);
        setFunction.setType(ContractUnitType.function);

        final ContractUnit sendFunction = new ContractUnit();
        sendFunction.setConstant(false);
        sendFunction.setInputs(new Variable[]{
                new Variable("account", new Address()),
                new Variable("amount", uintType)
        });
        sendFunction.setName("send");
        sendFunction.setOutputs(new Variable[0]);
        sendFunction.setType(ContractUnitType.function);

        Map<String, ContractUnit> contractUnits = new HashMap<String, ContractUnit>() {{
            put("get", getFunction);
            put("set", setFunction);
            put("send", sendFunction);
        }};

        testContract = new ContractManager("", "").new ContractImpl("", null, contractUnits, null);

    }

    public void testCallRPC() throws Exception {
        String EXPECTED_GET = "6D4CE63C";
        assertEquals(EXPECTED_GET, testContract.callRPCData("get"));
        String EXPECTED_SET = "60FE47B10000000000000000000000000000000000000000000000000000000000000005";
        assertEquals(EXPECTED_SET, testContract.callRPCData("set", new BigDecimal(5)));
        String EXPECTED_SEND = "D0679D3400000000000000000000000011111111111111111111111111111111111111110000000000000000000000000000000000000000000000000000000000000005";
        assertEquals(EXPECTED_SEND, testContract.callRPCData("send", "1111111111111111111111111111111111111111",  new BigDecimal(5)));
    }


}