package com.softjourn.coin.server.controller

import com.softjourn.coin.server.dto.*
import com.softjourn.coin.server.entity.*
import com.softjourn.coin.server.service.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.web.WebAppConfiguration

import java.security.Principal
import java.time.Instant

import static org.mockito.Matchers.*
import static org.mockito.Mockito.when

@Configuration
@ComponentScan(basePackages = ['com.softjourn.coin.server'])
@EnableAspectJAutoProxy(proxyTargetClass = true)
@WebAppConfiguration
class ControllerTestConfig {

    @Bean
    CoinService coinService() {
        def coinService = Mockito.mock(CoinService.class)

        def account = new Account('account1', 100)
        def account2 = new Account('account2', 100)
        def vm = new Account('VM1', 100)
        def treasury = new Account('Treasury', 100)

        def transaction = createTransaction(account, account)
        def transactionMove = createTransaction(account, account2)
        def transactionMove2 = createTransaction(vm, treasury)
        def rollbackTx = createTransaction(account2, account)

        when(coinService.getAmount(any(String.class))).thenReturn(new BigDecimal('100'))
        when(coinService.buy(any(String.class), any(String.class), any(BigDecimal.class), any(String.class)))
                .thenReturn(transaction)
        when(coinService.fillAccount(any(String.class), any(BigDecimal.class), any(String.class)))
                .thenReturn(transaction)
        when(coinService.move(any(String.class), any(String.class), any(BigDecimal.class), any(String.class)))
                .thenReturn(transactionMove)
        when(coinService.moveToTreasury(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(transactionMove2)
        when(coinService.rollback(anyLong()))
                .thenReturn(rollbackTx)


        coinService
    }

    @Bean
    AccountsService accountsService() {
        def accountsService = Mockito.mock(AccountsService.class)
        def account1 = new Account("user1", 1000)
        account1.fullName = "Bruce Wayne"
        account1.image = "images/default.png"

        def account2 = new Account("user2", 1500)
        account2.fullName = "Josh Long"
        account2.image = "images/default.png"

        def seller = new Account("123456-123456-123456", 0)
        seller.fullName = "VM1"
        seller.accountType = AccountType.MERCHANT

        when(accountsService.getAccount(anyString())).thenReturn(account1)

        when(accountsService.addMerchant(any(MerchantDTO.class), eq(AccountType.MERCHANT))).thenReturn(seller)

        when(accountsService.getAll()).thenReturn([account1, account2])

        when(accountsService.getAll(AccountType.MERCHANT)).thenReturn([seller])

        when(accountsService.delete(anyString())).thenReturn(true)

        accountsService
    }

    @Bean
    ContractService contractService() {
        def contractService = Mockito.mock(ContractService.class)
        def account1 = new Account("user1", 1000)
        account1.fullName = "Bruce Wayne"
        account1.image = "images/default.png"
        def instance = new Instance("SomeAddress")
        instance.setAccount(account1)
        def contract = new Contract(1L, "some name", true, "some code", "some abi", new Type("type"), new ArrayList<Instance>() {
            {
                add(instance)
            }
        })
        def transaction = new Transaction("id")
        transaction.setValue(new ContractCreateResponseDTO(1, "contract", "type", "some address"))
        when(contractService.newContract(any() as NewContractDTO)).thenReturn(transaction)
        when(contractService.getContracts()).thenReturn([contract])
        when(contractService.newInstance(any() as NewContractInstanceDTO)).thenReturn(transaction)
        when(contractService.getInstances(anyLong())).thenReturn([new ContractCreateResponseDTO(1, "contract", "type", "some address")])
        when(contractService.getContractsByAddress(anyString())).thenReturn(contract)
        when(contractService.getTypes()).thenReturn([new Type("some type")])
        when(contractService.getContractsByType(anyString())).thenReturn([contract])
        when(contractService.getContractConstructorInfo(anyLong())).thenReturn(new ArrayList<Map<String, String>>() {
            {
                add(new HashMap<String, String>() {
                    {
                        put("name", "some name")
                        put("type", "some type")
                    }
                })
            }
        })

        contractService
    }

    @Bean
    CrowdsaleService crowdsaleService() {
        def crowdsaleService = Mockito.mock(CrowdsaleService.class)
        def transaction = new Transaction("id")
        transaction.setValue(new CrowdsaleTransactionResultDTO(true))
        when(crowdsaleService.donate(any() as DonateDTO, any() as Principal)).thenReturn(transaction)
        when(crowdsaleService.withdraw(anyString())).thenReturn(transaction)
        when(crowdsaleService.getInfo(anyString())).thenReturn(new CrowdsaleInfoDTO(
                new ArrayList<Map<String, Object>>() {
                    {
                        add(new HashMap<String, Object>() {
                            {
                                put("name", "field name")
                                put("value", "field value")
                            }
                        })
                    }
                },
                new ArrayList<Map<String, Object>>() {
                    {
                        add(new HashMap<String, Object>() {
                            {
                                put("address", "token address")
                                put("amount", 2)
                            }
                        })
                    }
                }
        ))

        crowdsaleService
    }

    @Bean
    TransactionsService transactionsService() {
        def account = new Account('account1', 100)
        def account2 = new Account('account2', 100)
        def vm = new Account('VM1', 100)
        def treasury = new Account('Treasury', 100)

        def transaction = createTransaction(account2, account)
        def transactionMove = createTransaction(account, account2)
        def transactionMove2 = createTransaction(vm, treasury)
        def rollbackTx = createTransaction(account2, vm)

        def pageable = new PageRequest(0, 4)
        List<Transaction> data = [transaction, transactionMove, transactionMove2, rollbackTx]
        Page<Transaction> page = new PageImpl<>(data, pageable, 4)

        def transactionsService = Mockito.mock(TransactionsService.class)
        when(transactionsService.get(anyLong())).thenReturn(transaction)
        when(transactionsService.getFiltered(any(GenericFilter.class), any(Pageable.class))).thenReturn(page)
        when(transactionsService.getFiltered(any(GenericFilter.class), any(Pageable.class))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                GenericFilter filter = invocation.arguments[0] as GenericFilter
                filter.getConditions().forEach({ cond ->
                    if (cond.value.equals("notNumericValue")) {
                        throw new IllegalArgumentException("Can't create instance of class BigDecimal from value notNumericValue.")
                    }
                })
                return page
            }
        })
        when(transactionsService.export(any() as GenericFilter<Transaction>)).thenReturn(new HSSFWorkbook())
        when(transactionsService.getForUser(anyString(), any(Pageable.class), any(TransactionsController.Direction))).thenReturn(page.map({ tx -> new MobileTransactionDTO(tx) }))

        transactionsService
    }

    private static Transaction createTransaction(Account account, Account destinationAccount) {
        def transaction = new Transaction()
        transaction.account = account
        transaction.amount = 10
        transaction.comment = 'Some comment'
        transaction.created = Instant.now()
        transaction.destination = destinationAccount
        transaction.id = 1
        transaction.status = TransactionStatus.SUCCESS
        transaction.transactionStoring = new TransactionStoring()
        transaction
    }

}
