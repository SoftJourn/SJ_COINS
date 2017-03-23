package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.dto.MobileTransactionDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.AutocompleteService;
import com.softjourn.coin.server.service.GenericFilter;
import com.softjourn.coin.server.service.TransactionsService;
import com.softjourn.coin.server.util.JsonViews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionsController {

    private TransactionsService service;

    private AutocompleteService<Transaction> autocompleteService;

    @Autowired
    public TransactionsController(TransactionsService service, AutocompleteService<Transaction> autocompleteService) {
        this.service = service;
        this.autocompleteService = autocompleteService;
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

    @PreAuthorize("hasRole('BILLING')")
    @RequestMapping(value = "/filter", method = RequestMethod.GET)
    public Map<String, Object> getFilterOptions() {
        return autocompleteService.getAllPaths(Transaction.class);
    }

    @PreAuthorize("hasRole('BILLING')")
    @RequestMapping(value = "/filter/autocomplete", method = RequestMethod.GET)
    public List getAutocompleteOptions(@RequestParam String field) {
        return autocompleteService.getAutocomplete(field);
    }

    public enum Direction {
        IN, OUT, ALL
    }

}
