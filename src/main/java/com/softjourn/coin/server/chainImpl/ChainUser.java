package com.softjourn.coin.server.chainImpl;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Set;

public class ChainUser implements User {

    private final String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment;
    private final String mspId;

    public ChainUser(String name, File certificate, File privateKey, String mspId) {
        this.name = name;
        this.mspId = mspId;
        try {
            String userCert = readCertificate(certificate);
            this.enrollment = ChainEnrolment.builder()
                    .certificate(userCert)
                    .privateKey(readPrivateKey(privateKey))
                    .build();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public ChainUser(String name, String certificate, String privateKey, String mspId){
        this.name = name;
        this.mspId = mspId;
        try {
            this.enrollment = ChainEnrolment.builder()
                    .certificate(certificate)
                    .privateKey(readPrivateKey(privateKey))
                    .build();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    @Override
    public String getAccount() {
        return this.account;
    }

    @Override
    public String getAffiliation() {
        return this.affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    @Override
    public String getMspId() {
        return this.mspId;
    }

    private String readCertificate(File file) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(in, writer, String.valueOf(StandardCharsets.UTF_8));
            return writer.toString();
        }
    }

    private PrivateKey readPrivateKey(File file) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        PEMParser parser = new PEMParser(new FileReader(file));
        PrivateKeyInfo keyInfo = PrivateKeyInfo.getInstance(parser.readObject());
        KeyFactory keyFactory = KeyFactory.getInstance(keyInfo.getPrivateKeyAlgorithm().getAlgorithm().toString());
        KeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyInfo.getEncoded());
        return keyFactory.generatePrivate(privateKeySpec);
    }

    private PrivateKey readPrivateKey(String privateKey) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        PEMParser parser = new PEMParser(new StringReader(privateKey));
        PrivateKeyInfo keyInfo = PrivateKeyInfo.getInstance(parser.readObject());
        KeyFactory keyFactory = KeyFactory.getInstance(keyInfo.getPrivateKeyAlgorithm().getAlgorithm().toString());
        KeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyInfo.getEncoded());
        return keyFactory.generatePrivate(privateKeySpec);
    }
}
