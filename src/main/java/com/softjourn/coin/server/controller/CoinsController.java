package com.softjourn.coin.server.controller;


import com.softjourn.coin.server.dto.AmountDTO;
import com.softjourn.coin.server.dto.CashDTO;
import com.softjourn.coin.server.dto.CheckDTO;
import com.softjourn.coin.server.dto.ResultDTO;
import com.softjourn.coin.server.entity.AccountType;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.CoinService;
import com.softjourn.coin.server.service.FillAccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1")
public class CoinsController {

    private final CoinService coinService;
    private final FillAccountsService fillAccountsService;

    @Autowired
    public CoinsController(CoinService coinService, FillAccountsService fillAccountsService) {
        this.coinService = coinService;
        this.fillAccountsService = fillAccountsService;
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/amount", method = RequestMethod.GET)
    public Map<String, BigDecimal> getAmount(Principal principal) {
        Map<String, BigDecimal> responseBody = new HashMap<>();
        responseBody.put("amount", coinService.getAmount(principal.getName()));

        return responseBody;
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/buy/{merchantLdapId}", method = RequestMethod.POST)
    public Transaction spentAmount(Principal principal,
                                   @RequestBody AmountDTO amountDto,
                                   @PathVariable String merchantLdapId) {
        return coinService.buy(merchantLdapId, principal.getName(), amountDto.getAmount(), amountDto.getComment());
    }

    @PreAuthorize("#oauth2.hasScope('rollback')")
    @RequestMapping(value = "/rollback/{txId}", method = RequestMethod.POST)
    public Transaction rollback(@PathVariable Long txId, Principal principal) {
        System.out.println(principal);
        return coinService.rollback(txId);
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/move/{account}", method = RequestMethod.POST)
    public Transaction moveAmount(Principal principal,
                                  @RequestBody AmountDTO amountDTO,
                                  @PathVariable String account) {
        return coinService.move(principal.getName(), account, amountDTO.getAmount(), amountDTO.getComment());
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/withdraw", method = RequestMethod.POST)
    public byte[] withdrawAmount(Principal principal,
                                 @RequestBody AmountDTO amountDTO,
                                 @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
                                 HttpServletResponse response) {
        boolean produceImage = MediaType.IMAGE_PNG_VALUE.equals(accept);
        response.setHeader(HttpHeaders.CONTENT_TYPE, produceImage ? MediaType.IMAGE_PNG_VALUE : MediaType.APPLICATION_JSON_UTF8_VALUE);
        Transaction<byte[]> transaction = coinService.withdraw(principal.getName(), amountDTO.getAmount(), amountDTO.getComment(), produceImage);
        return transaction.getValue();
    }

    @PreAuthorize("authenticated")
    @RequestMapping(value = "/deposit", method = RequestMethod.POST)
    public Transaction deposit(Principal principal, @RequestBody CashDTO cashDTO) {
        return coinService.deposit(cashDTO, principal.getName(), "Deposite cash.", new BigDecimal(cashDTO.getAmount()));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/move/{account}/treasury", method = RequestMethod.POST)
    public Transaction moveAmountToTreasury(@PathVariable String account, @RequestBody AmountDTO amountDTO) {
        return coinService.moveToTreasury(account, amountDTO.getAmount(), amountDTO.getComment());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/add/{account}", method = RequestMethod.POST)
    public Transaction addAmount(@RequestBody AmountDTO amount,
                                 @PathVariable String account) {
        return coinService.fillAccount(account, amount.getAmount(), amount.getComment());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/add/", method = RequestMethod.POST)
    public ResultDTO addAmounts(@RequestParam MultipartFile file) throws IOException {
        return this.fillAccountsService.fillAccounts(file);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/check/{checkHash}", method = RequestMethod.GET)
    public CheckDTO checkProgress(@PathVariable String checkHash) {
        return this.fillAccountsService.checkProcessing(checkHash);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/template", method = RequestMethod.GET)
    public ResponseEntity<Void> getTemplate(HttpServletResponse response) throws IOException {
        String contentDisposition = "attachment; filename=\"template.csv\"";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
        response.setContentType("application/csv");
        response.setCharacterEncoding("UTF-8");
        this.fillAccountsService.getAccountDTOTemplate(response.getWriter());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/distribute", method = RequestMethod.POST)
    public void distribute(@RequestBody AmountDTO amount) {
        coinService.distribute(amount.getAmount(), "Distribute money for all accounts.");
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/amount/treasury", method = RequestMethod.GET)
    public Map<String, BigDecimal> getTreasuryAmount() {
        HashMap<String, BigDecimal> responseBody = new HashMap<>();
        responseBody.put("amount", coinService.getTreasuryAmount());
        return responseBody;
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BILLING')")
    @RequestMapping(value = "/amount/{accountType}", method = RequestMethod.GET)
    public Map<String, BigDecimal> getAmountByAccountType(@PathVariable String accountType) {
        HashMap<String, BigDecimal> responseBody = new HashMap<>();
        responseBody.put("amount", coinService.getAmountByAccountType(AccountType.valueOf(accountType.toUpperCase())));

        return responseBody;
    }
}
