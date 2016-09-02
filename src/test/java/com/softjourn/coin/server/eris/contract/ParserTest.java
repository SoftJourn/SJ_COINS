package com.softjourn.coin.server.eris.contract;

import com.softjourn.coin.server.eris.contract.types.Address;
import com.softjourn.coin.server.eris.contract.types.Type;
import com.softjourn.coin.server.eris.contract.types.Uint;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class ParserTest extends TestCase {

    private ContractManager.ContractImpl testContract;
    private ContractManager.ContractImpl testContract1;

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

        Map<String, ContractUnit> contractUnits = new HashMap<String, ContractUnit>() {{
            put("get", getFunction);
            put("set", setFunction);
        }};

        final ContractUnit queryBalance = new ContractUnit();
        queryBalance.setName("queryBalance");
        queryBalance.setConstant(true);
        queryBalance.setType(ContractUnitType.function);
        queryBalance.setInputs(new Variable[]{new Variable("addr", new Address())});
        queryBalance.setOutputs(new Variable[]{new Variable("balance", uintType)});

        final ContractUnit mint = new ContractUnit();
        mint.setName("mint");
        mint.setType(ContractUnitType.function);
        mint.setConstant(false);
        mint.setInputs(new Variable[]{
                new Variable("owner", new Address()),
                new Variable("amount", uintType)
        });
        mint.setOutputs(new Variable[0]);

        final ContractUnit send = new ContractUnit();
        send.setName("send");
        send.setType(ContractUnitType.function);
        send.setConstant(false);
        send.setInputs(new Variable[]{
                new Variable("receiver", new Address()),
                new Variable("amount", uintType)
        });
        send.setOutputs(new Variable[0]);

        final ContractUnit sendEvent = new ContractUnit();
        sendEvent.setName("Send");
        sendEvent.setType(ContractUnitType.event);
        sendEvent.setAnonymous(false);
        sendEvent.setInputs(new Variable[]{
                new Variable("from", new Address()),
                new Variable("to", new Address()),
                new Variable("value", uintType)
        });

        Map<String, ContractUnit> testUnits1 = new HashMap<String, ContractUnit>() {{
            put("queryBalance", queryBalance);
            put("mint", mint);
            put("send", send);
            put("Send", sendEvent);
        }};


        testContract1 = new ContractManager("", "").new ContractImpl("", null, testUnits1, null);

        testContract = new ContractManager("", "").new ContractImpl("", null, contractUnits, null);

    }

    public void testParse() throws Exception {
        String TEST_DATA = "[\n" +
                "  {\n" +
                "    \"constant\": false,\n" +
                "    \"inputs\": [\n" +
                "      {\n" +
                "        \"name\": \"x\",\n" +
                "        \"type\": \"uint256\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"set\",\n" +
                "    \"outputs\": [],\n" +
                "    \"type\": \"function\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"constant\": true,\n" +
                "    \"inputs\": [],\n" +
                "    \"name\": \"get\",\n" +
                "    \"outputs\": [\n" +
                "      {\n" +
                "        \"name\": \"retVal\",\n" +
                "        \"type\": \"uint256\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"type\": \"function\"\n" +
                "  }\n" +
                "]";
        assertEquals(testContract, new ContractManager("", TEST_DATA).parseContract(TEST_DATA).build());
        String TEST_DATA_1 = "[\n" +
                "  {\n" +
                "    \"constant\": true,\n" +
                "    \"inputs\": [\n" +
                "      {\n" +
                "        \"name\": \"addr\",\n" +
                "        \"type\": \"address\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"queryBalance\",\n" +
                "    \"outputs\": [\n" +
                "      {\n" +
                "        \"name\": \"balance\",\n" +
                "        \"type\": \"uint256\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"type\": \"function\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"constant\": false,\n" +
                "    \"inputs\": [\n" +
                "      {\n" +
                "        \"name\": \"owner\",\n" +
                "        \"type\": \"address\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"amount\",\n" +
                "        \"type\": \"uint256\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"mint\",\n" +
                "    \"outputs\": [],\n" +
                "    \"type\": \"function\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"constant\": false,\n" +
                "    \"inputs\": [\n" +
                "      {\n" +
                "        \"name\": \"receiver\",\n" +
                "        \"type\": \"address\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"amount\",\n" +
                "        \"type\": \"uint256\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"send\",\n" +
                "    \"outputs\": [],\n" +
                "    \"type\": \"function\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"inputs\": [],\n" +
                "    \"type\": \"constructor\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"anonymous\": false,\n" +
                "    \"inputs\": [\n" +
                "      {\n" +
                "        \"indexed\": false,\n" +
                "        \"name\": \"from\",\n" +
                "        \"type\": \"address\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"indexed\": false,\n" +
                "        \"name\": \"to\",\n" +
                "        \"type\": \"address\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"indexed\": false,\n" +
                "        \"name\": \"value\",\n" +
                "        \"type\": \"uint256\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"Send\",\n" +
                "    \"type\": \"event\"\n" +
                "  }\n" +
                "]";
        assertEquals(testContract1, new ContractManager("", TEST_DATA_1).parseContract(TEST_DATA_1).build());
    }

}