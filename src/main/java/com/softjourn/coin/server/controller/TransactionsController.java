package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.dto.MobileTransactionDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.GenericFilter;
import com.softjourn.coin.server.service.TransactionsService;
import com.softjourn.coin.server.util.JsonViews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionsController {

    private TransactionsService service;

    @Autowired
    public TransactionsController(TransactionsService service) {
        this.service = service;
    }

    @JsonView(JsonViews.REGULAR.class)
    @PreAuthorize("hasRole('BILLING')")
    @RequestMapping(method = RequestMethod.POST)
    public Page<Transaction> getFiltered(@RequestBody GenericFilter<Transaction> filter) {
        return service.getFiltered(filter, filter.getPageable().toPageable());
    }

    @PreAuthorize("hasRole('BILLING')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Transaction get(@PathVariable Long id) {
        return service.get(id);
    }

    @PreAuthorize("isAuthenticated() ")
    @RequestMapping(value = "/my", method = RequestMethod.GET)
    public Page<MobileTransactionDTO> getForUser(Principal principal, Pageable pageable, @RequestParam(required = false, defaultValue = "ALL") Direction direction) {
        return service.getForUser(principal.getName(), pageable, direction);
    }

    public enum Direction {
        IN, OUT, ALL
    }

}
