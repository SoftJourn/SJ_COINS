package com.softjourn.coin.server.dao;

import com.softjourn.eris.transaction.type.ErisTransaction;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

/**
 * ErisTransactionDAO created to map hibernate entity to ErisTransaction
 * Created by vromanchuk on 18.01.17.
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class ErisTransactionDAO extends ErisTransaction {

    public ErisTransactionDAO(String transactionString) throws StringIndexOutOfBoundsException {
        super(transactionString);
    }

    public ErisTransactionDAO() {
        super();
    }

    @Override
    public String getIdentifier() {
        return super.getIdentifier();
    }

    @Override
    public void setIdentifier(String identifier) {
        super.setIdentifier(identifier);
    }

    @Override
    public String getAmount() {
        return super.getAmount();
    }

    @Override
    public void setAmount(String amount) {
        super.setAmount(amount);
    }

    @Override
    public String getCallerAddress() {
        return super.getCallerAddress();
    }

    @Override
    public void setCallerAddress(String callerAddress) {
        super.setCallerAddress(callerAddress);
    }

    @Override
    public String getCallerPubKey() {
        return super.getCallerPubKey();
    }

    @Override
    public void setCallerPubKey(String callerPubKey) {
        super.setCallerPubKey(callerPubKey);
    }

    @Override
    public String getContractAddress() {
        return super.getContractAddress();
    }

    @Override
    public void setContractAddress(String contractAddress) {
        super.setContractAddress(contractAddress);
    }

    @Override
    public String getCallingData() {
        return super.getCallingData();
    }

    @Override
    public void setCallingData(String callingData) {
        super.setCallingData(callingData);
    }

    @Override
    public String getHashCallingDataFunctionName() {
        return super.getHashCallingDataFunctionName();
    }

    @Override
    public void setHashCallingDataFunctionName(String hashCallingDataFunctionName) {
        super.setHashCallingDataFunctionName(hashCallingDataFunctionName);
    }
}
