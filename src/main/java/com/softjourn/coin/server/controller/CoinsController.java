package com.softjourn.coin.server.controller;


import com.softjourn.coin.server.dto.AmountDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CoinsController {

    private final CoinService coinService;

    @Autowired
    public CoinsController(CoinService coinService) {
        this.coinService = coinService;
    }

    @RequestMapping(value = "/amount", method = RequestMethod.GET)
    public Map<String, BigDecimal> getAmount(Principal principal) {
        Map<String, BigDecimal> responseBody = new HashMap<>();
        responseBody.put("amount", coinService.getAmount(principal.getName()));

        return responseBody;
    }

    @RequestMapping(value = "/buy/{sellerName}", method = RequestMethod.POST)
    public Transaction spentAmount(Principal principal,
                                   @RequestBody AmountDTO amountDto,
                                   @PathVariable String sellerName) {
        return coinService.buy(sellerName, principal.getName(), amountDto.getAmount(), amountDto.getComment());
    }

    @RequestMapping(value = "/move/{account}", method = RequestMethod.POST)
    public Transaction moveAmount(Principal principal,
                              @RequestBody AmountDTO amountDTO,
                              @PathVariable String account) {
        return coinService.move(principal.getName(), account, amountDTO.getAmount(), amountDTO.getComment());
    }

    @RequestMapping(value = "/add/{account}", method = RequestMethod.POST)
    public Transaction addAmount(@RequestBody AmountDTO amount,
                                 @PathVariable String account) {
        return coinService.fillAccount(account, amount.getAmount(), amount.getComment());
    }

    @RequestMapping(value = "/distribute", method = RequestMethod.POST)
    public void distribute(@RequestBody AmountDTO amount) {
        coinService.distribute(amount.getAmount(), "Distribute money for all accounts.");
    }


}
