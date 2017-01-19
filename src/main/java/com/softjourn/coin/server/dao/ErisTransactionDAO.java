package com.softjourn.coin.server.dao;

import com.softjourn.eris.transaction.type.ErisTransaction;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import java.io.IOException;
import java.util.List;

/**
 * ErisTransactionDAO created to map hibernate entity to ErisTransaction
 * Created by vromanchuk on 18.01.17.
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class ErisTransactionDAO {

    private ErisTransaction transaction;

    public ErisTransactionDAO(ErisTransaction transaction) {
        this.transaction = transaction;
    }

    public ErisTransactionDAO() {
    }

    public List<Object> parseCallingData(String abi) throws IOException {
        return transaction.parseCallingData(abi);
    }

    public String getIdentifier() {
        return transaction.getIdentifier();
    }

    public void setIdentifier(String identifier) {
        transaction.setIdentifier(identifier);
    }

    public String getAmount() {
        return transaction.getAmount();
    }

    public void setAmount(String amount) {
        transaction.setAmount(amount);
    }

    public String getCallerAddress() {
        return transaction.getCallerAddress();
    }

    public void setCallerAddress(String callerAddress) {
        transaction.setCallerAddress(callerAddress);
    }

    public String getCallerPubKey() {
        return transaction.getCallerPubKey();
    }

    public void setCallerPubKey(String callerPubKey) {
        transaction.setCallerPubKey(callerPubKey);
    }

    public String getContractAddress() {
        return transaction.getContractAddress();
    }

    public void setContractAddress(String contractAddress) {
        transaction.setContractAddress(contractAddress);
    }

    public String getAdditionalInfo() {
        return transaction.getAdditionalInfo();
    }

    public void setAdditionalInfo(String additionalInfo) {
        transaction.setAdditionalInfo(additionalInfo);
    }

    public String getCallingData() {
        return transaction.getCallingData();
    }

    public void setCallingData(String callingData) {
        transaction.setCallingData(callingData);
    }

    public String getHashCallingDataFunctionName() {
        return transaction.getHashCallingDataFunctionName();
    }

    public void setHashCallingDataFunctionName(String hashCallingDataFunctionName) {
        transaction.setHashCallingDataFunctionName(hashCallingDataFunctionName);
    }
}
