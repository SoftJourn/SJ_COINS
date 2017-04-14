package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.ApproveDTO;
import com.softjourn.coin.server.dto.FoundationTransactionResultDTO;
import com.softjourn.coin.server.dto.WithdrawDTO;
import com.softjourn.coin.server.entity.Account;
import com.softjourn.coin.server.entity.Contract;
import com.softjourn.coin.server.entity.ErisAccount;
import com.softjourn.coin.server.entity.ErisAccountType;
import com.softjourn.coin.server.entity.Instance;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.exceptions.ErisAccountNotFoundException;
import com.softjourn.coin.server.exceptions.ErisContractInstanceNotFound;
import com.softjourn.coin.server.exceptions.ErisProcessingException;
import com.softjourn.coin.server.repository.InstanceRepository;
import com.softjourn.eris.contract.response.Error;
import com.softjourn.eris.contract.response.Response;
import com.softjourn.eris.contract.response.TxParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sun.security.acl.PrincipalImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FoundationServiceTest {


    @Mock
    private
    com.softjourn.eris.contract.Contract erisContract;


    @Mock
    private ErisContractService contractService;

    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private ErisAccountsService erisAccountsService;

    private FoundationService foundationService;

    @Before
    public void setUp() throws IOException {
        Contract contract = new Contract();
        contract.setActive(true);
        contract.setName("Foundation");
        contract.setCode("some bytecode");
        contract.setAbi("some abi");
        contract.setId(1L);

        ErisAccount erisAccount = new ErisAccount();
        erisAccount.setAddress("some address");
        erisAccount.setPrivKey("some privKey");
        erisAccount.setPubKey("some pubKey");
        erisAccount.setType(ErisAccountType.PARTICIPANT);

        Account account = new Account();
        account.setErisAccount(erisAccount);
        account.setAmount(BigDecimal.valueOf(10));

        Instance instance = new Instance();
        instance.setId(1L);
        instance.setContract(contract);
        instance.setAddress("some address");
        instance.setName("some name");
        instance.setAccount(account);

        Response response = new Response("",
                Collections.singletonList(true),
                null,
                new TxParams("some", "some"));

        Response responseWithError = new Response("",
                Collections.singletonList(false),
                new Error(0, "Some error"),
                new TxParams("some", "some"));


        when(erisContract.call(eq("close"))).thenReturn(response);
        when(erisContract.call(eq("withdraw"), anyObject(), anyObject(), anyString())).thenReturn(response);
        when(erisContract.call(eq("approveAndCall"), anyString(), anyObject())).thenReturn(response);
        when(erisContract.call(eq("approveAndCall"), eq("wrong address"), anyObject())).thenReturn(responseWithError);
        when(instanceRepository.findByAddress(anyString())).thenReturn(instance);
        when(instanceRepository.findByAddress("null")).thenReturn(null);
        when(erisAccountsService.getByName(anyString())).thenReturn(erisAccount);
        when(erisAccountsService.getByName("null")).thenReturn(null);
        when(contractService.getContract(anyString(), anyString(), anyObject())).thenReturn(erisContract);

        foundationService = new FoundationServiceImpl(contractService, instanceRepository, erisAccountsService);
    }

    @Test
    public void approveTest() throws IOException {
        Transaction result = foundationService.approve(new ApproveDTO("", "", BigInteger.valueOf(1)), new PrincipalImpl("some name"));
        Assert.assertEquals(true, result.getValue() instanceof FoundationTransactionResultDTO);
        Assert.assertEquals(true, ((FoundationTransactionResultDTO) result.getValue()).getTransactionResult());
    }

    @Test(expected = ErisProcessingException.class)
    public void approveTestWithWrongAddress() throws IOException {
        foundationService.approve(new ApproveDTO("", "wrong address", BigInteger.valueOf(1)), new PrincipalImpl("some name"));
    }

    @Test
    public void closeTest() throws IOException {
        Transaction result = foundationService.close("some string", new PrincipalImpl("some name"));
        Assert.assertEquals(true, result.getValue() instanceof FoundationTransactionResultDTO);
        Assert.assertEquals(true, ((FoundationTransactionResultDTO) result.getValue()).getTransactionResult());
    }

    @Test(expected = ErisContractInstanceNotFound.class)
    public void closeTestWithNullInstance() throws IOException {
        foundationService.close("null", new PrincipalImpl("some name"));
    }

    @Test(expected = ErisAccountNotFoundException.class)
    public void closeTestWithNullErisAccount() throws IOException {
        foundationService.close("some string", new PrincipalImpl("null"));
    }

    @Test
    public void withdrawTest() throws IOException {
        Transaction result = foundationService.withdraw("some string", new WithdrawDTO(BigInteger.valueOf(1), BigInteger.valueOf(1), "withdraw"));
        Assert.assertEquals(true, result.getValue() instanceof FoundationTransactionResultDTO);
        Assert.assertEquals(true, ((FoundationTransactionResultDTO) result.getValue()).getTransactionResult());
    }

    @Test(expected = ErisContractInstanceNotFound.class)
    public void withdrawTestWithNullInstance() throws IOException {
        foundationService.withdraw("null", new WithdrawDTO(BigInteger.valueOf(1), BigInteger.valueOf(1), "withdraw"));
    }

    @Test(expected = ErisContractInstanceNotFound.class)
    public void getInfoTestWithNullInstance() throws IOException {
        foundationService.getInfo("null");
    }

}
