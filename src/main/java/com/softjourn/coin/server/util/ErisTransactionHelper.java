package com.softjourn.coin.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softjourn.coin.server.blockchain.Block;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;

/**
 * ErisTransactionHelper
 * Created by vromanchuk on 12.01.17.
 */
@Component
public class ErisTransactionHelper {

    private ObjectMapper objectMapper = new ObjectMapper();

    public Block getBlock(BigInteger blockNumber) throws IOException {
        String blockJSON = this.getBlockJSON(blockNumber);
        return objectMapper.readValue(blockJSON, Block.class);
    }

    public String getBlockJSON(BigInteger blockNumber) {
        if (blockNumber.intValue() == 10) return "{\"header\": {\n" +
                "    \"chain_id\": \"test\",\n" +
                "    \"height\": 10,\n" +
                "    \"time\": \"2017-01-12T12:57:30.323Z\",\n" +
                "    \"num_txs\": 1,\n" +
                "    \"last_block_hash\": \"BEF1FFB7E6BA03430CC99B84FA63F4AA48BBE611\",\n" +
                "    \"last_block_parts\": {\n" +
                "      \"total\": 1,\n" +
                "      \"hash\": \"01036B2E2C198E0477CC2F218BE021BC78ED5067\"\n" +
                "    },\n" +
                "    \"last_commit_hash\": \"D3A0C64DD856F6844409664BDE454D7D9B8B12AC\",\n" +
                "    \"data_hash\": \"C0C2368E4E4560501BB8D9B24F184F5B8A6581D9\",\n" +
                "    \"validators_hash\": \"24741F32460A331BFAFB71CE5C17871799C32069\",\n" +
                "    \"app_hash\": \"FCF4E8CF751816EB48DA10A9294339BD4BC9E44D\"\n" +
                "  },\n" +
                "  \"data\": {\n" +
                "    \"txs\": [\n" +
                "      \"0201011490CCB0132FA9287AB3C3283978C0E523FA1450A0000000000000000101070177F493FA8F938E09C077BAD480B77420DB6B4E82DA2BFF69C468CA3A8667DF6A6D49DE8BD2AD9FB19235A15D80238E47C08DCA693DBC5CA5DDC61195E8262B0A01CE92BABD1B4BEED36B5314DA468B2C16BE0E0380948C67ED2C352ADD8099E67301143E5C5EBAEAA66C24785D04F33F2B62667001474A00000000000F42400000000000000000014440C10F1900000000000000000000000090CCB0132FA9287AB3C3283978C0E523FA1450A0000000000000000000000000000000000000000000000000000000000000006E\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"last_commit\": {\n" +
                "    \"precommits\": [\n" +
                "      {\n" +
                "        \"height\": 3846,\n" +
                "        \"round\": 0,\n" +
                "        \"type\": 2,\n" +
                "        \"block_hash\": \"BEF1FFB7E6BA03430CC99B84FA63F4AA48BBE611\",\n" +
                "        \"block_parts_header\": {\n" +
                "          \"total\": 1,\n" +
                "          \"hash\": \"01036B2E2C198E0477CC2F218BE021BC78ED5067\"\n" +
                "        },\n" +
                "        \"signature\": \"5EFD1990604988478F4E28742FA9293AF9A60DBBC638123C45DB1595D4BA7F654C1C0F721F609E99613312BB549EAEFFD491C8961F518A2A5FB3F81E139D6C0E\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }}";
        else
            return
                    "{\"header\": {\n" +
                            "    \"chain_id\": \"test\",\n" +
                            "    \"height\": 3847,\n" +
                            "    \"time\": \"2017-01-12T12:57:30.323Z\",\n" +
                            "    \"num_txs\": 1,\n" +
                            "    \"last_block_hash\": \"BEF1FFB7E6BA03430CC99B84FA63F4AA48BBE611\",\n" +
                            "    \"last_block_parts\": {\n" +
                            "      \"total\": 1,\n" +
                            "      \"hash\": \"01036B2E2C198E0477CC2F218BE021BC78ED5067\"\n" +
                            "    },\n" +
                            "    \"last_commit_hash\": \"D3A0C64DD856F6844409664BDE454D7D9B8B12AC\",\n" +
                            "    \"data_hash\": \"C0C2368E4E4560501BB8D9B24F184F5B8A6581D9\",\n" +
                            "    \"validators_hash\": \"24741F32460A331BFAFB71CE5C17871799C32069\",\n" +
                            "    \"app_hash\": \"FCF4E8CF751816EB48DA10A9294339BD4BC9E44D\"\n" +
                            "  },\n" +
                            "  \"data\": {\n" +
                            "    \"txs\": [\n" +
                            "      \"0201011490CCB0132FA9287AB3C3283978C0E523FA1450A0000000000000000101070177F493FA8F938E09C077BAD480B77420DB6B4E82DA2BFF69C468CA3A8667DF6A6D49DE8BD2AD9FB19235A15D80238E47C08DCA693DBC5CA5DDC61195E8262B0A01CE92BABD1B4BEED36B5314DA468B2C16BE0E0380948C67ED2C352ADD8099E67301143E5C5EBAEAA66C24785D04F33F2B62667001474A00000000000F42400000000000000000014440C10F1900000000000000000000000090CCB0132FA9287AB3C3283978C0E523FA1450A0000000000000000000000000000000000000000000000000000000000000006E\"\n" +
                            "    ]\n" +
                            "  },\n" +
                            "  \"last_commit\": {\n" +
                            "    \"precommits\": [\n" +
                            "      {\n" +
                            "        \"height\": 3846,\n" +
                            "        \"round\": 0,\n" +
                            "        \"type\": 2,\n" +
                            "        \"block_hash\": \"BEF1FFB7E6BA03430CC99B84FA63F4AA48BBE611\",\n" +
                            "        \"block_parts_header\": {\n" +
                            "          \"total\": 1,\n" +
                            "          \"hash\": \"01036B2E2C198E0477CC2F218BE021BC78ED5067\"\n" +
                            "        },\n" +
                            "        \"signature\": \"5EFD1990604988478F4E28742FA9293AF9A60DBBC638123C45DB1595D4BA7F654C1C0F721F609E99613312BB549EAEFFD491C8961F518A2A5FB3F81E139D6C0E\"\n" +
                            "      }\n" +
                            "    ]\n" +
                            "  }}";
    }

    public Block getLatestBlock() throws IOException {
        String blockJSON = this.getBlockJSON(new BigInteger("1111"));
        return objectMapper.readValue(blockJSON, Block.class);
    }

    public BigInteger getLatestBlockNumber() throws IOException {
        Block latestBlock = this.getLatestBlock();
        return latestBlock.getHeader().getHeight();
    }
}
