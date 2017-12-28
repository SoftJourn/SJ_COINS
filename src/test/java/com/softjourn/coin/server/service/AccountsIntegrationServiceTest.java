package com.softjourn.coin.server.service;

import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.exceptions.NotFoundException;
import com.softjourn.coin.server.repository.AccountRepository;
import com.softjourn.coin.server.repository.FabricAccountRepository;
import com.softjourn.common.auth.OAuthHelper;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static junit.framework.TestCase.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@Log
@DataJpaTest
@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
public class AccountsIntegrationServiceTest {

    private static final String NOT_EXISTING_LDAP_ID = "notExist";

    @InjectMocks
    private AccountsService accountsService;

    @Mock
    private ErisAccountsService erisAccountsService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private OAuthHelper oAuthHelper;
    @Mock
    private CoinService coinService;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private FabricAccountRepository fabricAccountRepository;

    @Value("${image.storage.path}")
    private String imageStoragePath;
    @Value("${image.storage.path}")
    String defaultImagePath;

    @Mock
    private MultipartFile testFile,testOverrideFile;
    private String testAccountName;

    @Test
    public void getAccountImage() throws Exception {
        testAccountName = "vromanchuk";
        Account account = this.accountsService.getAccount(testAccountName);
        assertNotNull(account);
        assertNotNull(account.getImage());
        log.warning(account.getImage());
        byte[] expected = readBytes(account.getImage());
        byte[] actual = this.accountsService.getImage(account.getImage());
        assertTrue(Arrays.equals(expected, actual));
    }

    @Test(expected = NotFoundException.class)
    public void getAccountImage_FileDoNotExists() throws Exception {
        this.accountsService.getImage("account/don't/exists");
    }

    @Test
    public void loadAccountImage() throws Exception {
        testAccountName = "ovovchuk";
        String uri = String.format("/account/%s/%s", testAccountName, testFile.getOriginalFilename());
        this.cleanFolder(uri);
        assertFalse(fileExists(uri));
        this.accountsService.loadAccountImage(testFile, testAccountName);
        assertTrue(fileExists(uri));
        Account account = this.accountsService.getAccount(testAccountName);
        assertEquals(uri, account.getImage());

        // Override test
        this.accountsService.loadAccountImage(testOverrideFile, testAccountName);
        assertFalse(fileExists(uri));

        uri = String.format("/account/%s/%s", testAccountName, testOverrideFile.getOriginalFilename());
        assertTrue(fileExists(uri));
        account = this.accountsService.getAccount(testAccountName);
        assertEquals(uri, account.getImage());
    }

    private void cleanFolder(String uri) throws IOException {
        String fullPath = imageStoragePath + uri;
        File file = new File(fullPath);
        //noinspection ResultOfMethodCallIgnored
        FileUtils.deleteDirectory(file.getParentFile());
    }

    private boolean fileExists(String uri) {
        String fullPath = imageStoragePath + uri;
        File file = new File(fullPath);
        return file.exists();
    }

    private byte[] readBytes(String uri) throws IOException {
        String fullPath = imageStoragePath + uri;
        File file = new File(fullPath);
        InputStream in = new FileInputStream(file);
        return IOUtils.toByteArray(in);
    }

    @Before
    public void setUp() throws Exception {

        this.accountsService = new AccountsService(accountRepository, fabricAccountRepository, coinService,
            erisAccountsService, imageStoragePath, oAuthHelper, imageStoragePath, defaultImagePath);

        this.mockFile(testFile,"loadingImageTest.jpg");
        this.mockFile(testOverrideFile,"loadingImageOverrideTest.png");

        // rest template mocks

        ReflectionTestUtils.setField(accountsService, "authServerUrl", "http://test.com");

        when(restTemplate.getForEntity(anyString(), any()))
            .thenReturn(new ResponseEntity<>(new Account(), HttpStatus.OK));

        when(restTemplate.getForEntity("http://test.com/users/" + NOT_EXISTING_LDAP_ID, Account.class))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // oAuthHelper mocks

        when(oAuthHelper.getForEntityWithToken(anyString(), any()))
            .thenReturn(new ResponseEntity<>(new Account(), HttpStatus.OK));

        when(oAuthHelper.getForEntityWithToken("http://test.com/v1/users/" + NOT_EXISTING_LDAP_ID, Account.class))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    private void mockFile(MultipartFile file, String fileName) throws IOException {
        String loadingImageTestUri = "/test/"+ fileName;
        when(file.getBytes()).thenReturn(readBytes(loadingImageTestUri));
        when(file.getOriginalFilename()).thenReturn(fileName);
    }
}