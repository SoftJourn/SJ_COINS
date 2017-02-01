package com.softjourn.coin.server.entity;

import com.softjourn.coin.server.dao.ErisTransactionDAO;
import com.softjourn.eris.transaction.pojo.ErisTransaction;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jcajce.provider.digest.RIPEMD160;

import javax.persistence.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * TransactionStoring is an object that will have all valuable information
 * about transaction. It should contain information that can be checked via blockchain
 * Created by vromanchuk on 17.01.17.
 */
@Data
@Entity
@Table(name = "transaction_history")
@NoArgsConstructor
public class TransactionStoring {

    public static final int TX_TYPE_CALL = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "BIGINT")
    private Long blockNumber;
    private LocalDateTime time;
    private String functionName;
    private String chainId;
    private String txId;

    @Embedded
    private ErisTransactionDAO transaction;

    @ElementCollection
    @JoinTable(name = "tx_calling_data", joinColumns = @JoinColumn(name = "tx_id"))
    @MapKeyColumn(name = "function_name")
    private Map<String, String> callingValue;

    public TransactionStoring(Long blockNumber, LocalDateTime time, String functionName
            , String chainId, ErisTransactionDAO transaction, Map<String, String> callingValue) {
        this.blockNumber = blockNumber;
        this.time = time;
        this.functionName = functionName;
        this.chainId = chainId;
        this.transaction = transaction;
        this.callingValue = callingValue;
        if(this.transaction == null)
            throw new IllegalArgumentException("transaction can not be null");
        this.txId = getTxId(chainId, transaction);
    }

    public void setTransaction(ErisTransaction transaction) {
        if (this.transaction == null) {
            this.transaction = new ErisTransactionDAO(transaction);
        } else {
            this.transaction.setTransaction(transaction);
        }
    }

    public void setTransaction(ErisTransactionDAO transaction) {
        this.transaction = transaction;
    }

    private static String getTxId(String tx) {
        MessageDigest messageDigest = new RIPEMD160.Digest();
        return Hex.encodeHexString(messageDigest.digest(tx.getBytes()));
    }

    private static String getTxJson(String chainId, String contractAddress, String txData, long fee, long gasLimit, String txInput) {
        return "{\"chain_id\":\"" + chainId + "\","
                + "\"tx\":[" + TX_TYPE_CALL
                + ",{\"address\":\"" + contractAddress
                + "\",\"data\":\"" + txData
                + "\"," + "\"fee\":"
                + fee + ",\"gas_limit\":"
                + gasLimit + ",\"input\":"
                + txInput + "" + "}]}";
    }

    private static String getTxJson(String chainId, ErisTransactionDAO transaction){

        String txInputJson = getTxInputJson(transaction);
            return getTxJson(chainId,transaction.getContractAddress(),transaction.getCallingData()
                    ,transaction.getFee(),transaction.getGasLimit(),txInputJson);
    }

    private static String getTxInputJson(String userAddress, long amount, long sequence) {
        return "{\"address\":\"" + userAddress + "\",\"amount\":" + amount + ",\"sequence\":" + sequence + "}";
    }

    private static String getTxInputJson(ErisTransactionDAO transaction){
            return getTxInputJson(transaction.getCallerAddress(),transaction.getAmount(),transaction.getSequence());
    }

    public static String getTxId(String chainId, ErisTransactionDAO transaction){
        String txJson = getTxJson(chainId,transaction);
        return getTxId(txJson);
    }

    public String getTxId() {
        if(this.chainId == null || this.transaction == null)
            return "";
        getTxId(this.chainId,this.transaction);
        return txId;
    }
}
