package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.dto.MobileTransactionDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.AutocompleteService;
import com.softjourn.coin.server.service.GenericFilter;
import com.softjourn.coin.server.service.ReportService;
import com.softjourn.coin.server.service.TransactionsService;
import com.softjourn.coin.server.util.JsonViews;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionsController {

    private TransactionsService service;

    private AutocompleteService<Transaction> autocompleteService;

    private ReportService reportService;

    @Autowired
    public TransactionsController(TransactionsService service, AutocompleteService<Transaction> autocompleteService, ReportService reportService) {
        this.service = service;
        this.autocompleteService = autocompleteService;
        this.reportService = reportService;
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

    @PreAuthorize("hasRole('BILLING')")
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public String export(@RequestBody GenericFilter<Transaction> filter) throws IOException, ReflectiveOperationException {
        Workbook workbook = service.export(filter);

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            bytes = bos.toByteArray();
        }

        return Base64.getEncoder().encodeToString(bytes);
    }

    public enum Direction {
        IN, OUT, ALL
    }

}
