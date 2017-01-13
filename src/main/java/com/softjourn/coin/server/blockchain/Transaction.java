package com.softjourn.coin.server.blockchain;

/**
 * Transaction in Eris chain
 * Created by vromanchuk on 12.01.17.
 */
public class Transaction {

    private final String transactionString;

    private final String identifier;
    private final String amount;
    private final String callerAddress;
    private final String callerPubKey;
    private final String contractAddress;
    private final String callingData;

    public Transaction(String transactionString) throws StringIndexOutOfBoundsException {
        this.transactionString = transactionString;
        // 4 digits of some identifier
        this.identifier = transactionString.substring(0, 4);
        // 4 digits of delimiter 0114
        this.callerAddress = transactionString.substring(8, 48);
        this.amount = transactionString.substring(48, 64);
        this.callerPubKey = transactionString.substring(200, 264);
        //delimiter 0114
        this.contractAddress = transactionString.substring(268, 308);
        // Some info gas_limit fee 0144 - some delimiter
        this.callingData = transactionString.substring(344);
    }

    public static Object parseCallingData(String callingData) {
        return null;
    }

    public String getTransactionString() {
        return transactionString;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getAmount() {
        return amount;
    }

    public String getCallerAddress() {
        return callerAddress;
    }

    public String getCallerPubKey() {
        return callerPubKey;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getCallingData() {
        return callingData;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionString='" + transactionString + '\'' +
                ", identifier='" + identifier + '\'' +
                ", amount='" + amount + '\'' +
                ", callerAddress='" + callerAddress + '\'' +
                ", callerPubKey='" + callerPubKey + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", callingData='" + callingData + '\'' +
                '}';
    }
}
