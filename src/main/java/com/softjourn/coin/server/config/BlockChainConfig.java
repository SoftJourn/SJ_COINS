package com.softjourn.coin.server.config;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.coin.server.entity.TransactionStoring;
import com.softjourn.coin.server.service.ContractService;
import com.softjourn.coin.server.service.ErisTransactionHistoryService;
import com.softjourn.eris.block.BlockChainService;
import com.softjourn.eris.block.ErisBlockChainService;
import com.softjourn.eris.transaction.ErisTransactionService;
import com.softjourn.eris.transaction.TransactionService;
import com.softjourn.eris.transaction.parser.ErisParser;
import com.softjourn.eris.transaction.parser.ErisParserService;
import com.softjourn.eris.transaction.parser.v11.Eris11CallTransactionParser;
import com.softjourn.eris.transaction.parser.v11.Eris11ParserService;
import com.softjourn.eris.transaction.pojo.ErisCallTransaction;
import com.softjourn.eris.transaction.pojo.ErisTransaction;
import com.softjourn.eris.transaction.pojo.ErisTransactionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class BlockChainConfig {

    @Bean
    public ErisParser parser() {
        return new Eris11CallTransactionParser();
    }

    @Bean
    public ErisParserService parserService(ErisParser... parsers) {
        return new Eris11ParserService(parsers);
    }

    @Bean
    public TransactionService transactionService(ErisParserService parserService,
                                                 ErisTransactionHistoryService historyService,
                                                 ContractService contractService) {
        Map<ErisTransactionType, Consumer<? extends ErisTransaction>> consumerMap = new HashMap<>();
        Consumer<? extends ErisCallTransaction> consumer;
        consumer = transaction -> historyService
                .storeTransaction(mapTransaction(transaction));
        consumerMap.put(ErisTransactionType.CALL,consumer);
        Function<String, String> getAbiFromContractAddress = address -> contractService
                .getContractsByAddress(address).getAbi();
        return new ErisTransactionService(parserService, consumerMap, getAbiFromContractAddress);
    }

    private TransactionStoring mapTransaction(ErisCallTransaction transaction){
        ErisTransactionDAO transactionDAO = new ErisTransactionDAO(transaction);
        return new TransactionStoring(transaction.getBlockHeader(),transaction.getFunctionName()
                ,transactionDAO,transaction.getFunctionArguments(),transaction.getTxId());
    }

    @Bean
    public BlockChainService blockChainService(@Value("${eris.chain.url}") String host,
                                               TransactionService transactionService) {
        return new ErisBlockChainService(host, transactionService);
    }
}
