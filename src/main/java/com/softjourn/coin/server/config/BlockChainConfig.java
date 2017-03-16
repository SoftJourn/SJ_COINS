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
import com.softjourn.eris.transaction.parser.v12.Eris12CallTransactionParser;
import com.softjourn.eris.transaction.parser.v12.Eris12ParserService;
import com.softjourn.eris.transaction.pojo.ErisCallTransaction;
import com.softjourn.eris.transaction.pojo.ErisTransaction;
import com.softjourn.eris.transaction.pojo.ErisTransactionType;
import com.softjourn.eris.transaction.pojo.ErisUndefinedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
public class BlockChainConfig {

    private static final Map<String, ErisParser[]> SUPPORTED_PARSERS = new HashMap<String, ErisParser[]>() {{
        put("0.11", new ErisParser[]{new Eris11CallTransactionParser()});
        put("0.12", new ErisParser[]{new Eris12CallTransactionParser()});
    }};

    private static final Map<String, Function<ErisParser[], ErisParserService>> SUPPORTED_PARSER_SERVICES = new HashMap<String, Function<ErisParser[], ErisParserService>>() {{
        put("0.11", Eris11ParserService::new);
        put("0.12", Eris12ParserService::new);
    }};

    @Value("${eris.chain.version}")
    private String erisChainVersion;

    @Bean
    public ErisParser[] parsers() {
        ErisParser[] parsers = SUPPORTED_PARSERS.get(getErisChainVersion());
        if (parsers == null) throw new IllegalStateException("Can't find any Eris parsers for version " + getErisChainVersion());
        return parsers;
    }

    @Bean
    public ErisParserService parserService(ErisParser... parsers) {
        Function<ErisParser[], ErisParserService> parserServiceFunction = SUPPORTED_PARSER_SERVICES.get(getErisChainVersion());
        if (parserServiceFunction == null) throw new IllegalStateException("Can't find any Eris parser service for version " + getErisChainVersion());
        return parserServiceFunction.apply(parsers);
    }

    private String getErisChainVersion() {
        if (erisChainVersion == null) {
            throw new IllegalStateException("Eris version not specified. Add \"eris.chain.version\" property. " +
                    "Currently supported: " + SUPPORTED_PARSERS.keySet());
        } else {
            return erisChainVersion;
        }
    }

    @Bean
    public TransactionService transactionService(ErisParserService parserService,
                                                 ErisTransactionHistoryService historyService,
                                                 ContractService contractService) {
        Map<ErisTransactionType, Consumer<? extends ErisTransaction>> consumerMap = new HashMap<>();
        consumerMap.put(ErisTransactionType.CALL, getCallTxConsumer(historyService));
        consumerMap.put(ErisTransactionType.UNDEFINED, getUndefinedTxConsumer());

        return new ErisTransactionService(parserService, consumerMap, getAbiFromContractAddress(contractService));
    }

    private Consumer<? extends ErisUndefinedTransaction> getUndefinedTxConsumer() {
        return transaction -> log.info("ERIS UNDEFINED TX: " + transaction.toString());
    }

    private Consumer<? extends ErisCallTransaction> getCallTxConsumer(ErisTransactionHistoryService historyService) {
        return transaction -> historyService
                .storeTransaction(mapTransaction(transaction));

    }

    private Function<String, String> getAbiFromContractAddress(ContractService contractService) {
        return address -> contractService
                .getContractsByAddress(address)
                .getAbi();
    }

    private TransactionStoring mapTransaction(ErisCallTransaction transaction) {
        ErisTransactionDAO transactionDAO = new ErisTransactionDAO(transaction);
        return new TransactionStoring(transactionDAO);
    }

    @Bean
    public BlockChainService blockChainService(@Value("${eris.chain.url}") String host,
                                               TransactionService transactionService) {
        return new ErisBlockChainService(host, transactionService);
    }
}
