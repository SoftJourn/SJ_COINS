package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.dto.MobileTransactionDTO;
import com.softjourn.coin.server.dto.PageRequestImpl;
import com.softjourn.coin.server.dto.SingleReplenichmentResponseDTO;
import com.softjourn.coin.server.dto.SingleReplenishmentRequestDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.entity.TransactionType;
import com.softjourn.coin.server.service.AutocompleteService;
import com.softjourn.coin.server.service.GenericFilter;
import com.softjourn.coin.server.service.TransactionsService;
import com.softjourn.coin.server.util.JsonViews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.softjourn.coin.server.entity.TransactionType.SINGLE_REPLENISHMENT;
import static com.softjourn.coin.server.util.Util.dataToCSV;

@RestController
@RequestMapping("/v1/transactions")
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

    @PreAuthorize("hasRole('BILLING')")
    @RequestMapping(value = "/single_replenishment", method = RequestMethod.POST)
    public Page<Transaction> getSingleReplenishment(@Valid @RequestBody SingleReplenishmentRequestDTO singleReplenishment) {
        return service.getTransactionsByTypeAndTime(SINGLE_REPLENISHMENT, singleReplenishment.getStart(),
                singleReplenishment.getDue(), singleReplenishment.getPageable().toPageable());
    }

    @PreAuthorize("hasRole('BILLING')")
    @RequestMapping(value = "/single_replenishment/report", method = RequestMethod.POST)
    public ResponseEntity<Void> getSingleReplenishmentReport(@Valid @RequestBody SingleReplenishmentRequestDTO singleReplenishment,
                                                             HttpServletResponse response) throws IOException {
        Page<Transaction> transactions = service.getTransactionsByTypeAndTime(SINGLE_REPLENISHMENT, singleReplenishment.getStart(),
                singleReplenishment.getDue(), new PageRequestImpl(Integer.MAX_VALUE, 0, null).toPageable());

        String contentDisposition = String.format("attachment; filename=\"single_replenishment%s.csv\"", LocalDateTime.now().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
        response.setContentType("application/csv");
        response.setCharacterEncoding("UTF-8");

        dataToCSV(response.getWriter(), transactions.getContent().stream()
                .map(transaction -> new SingleReplenichmentResponseDTO(transaction.getDestination().getFullName(),
                        transaction.getAmount(),
                        transaction.getStatus(),
                        transaction.getCreated())).collect(Collectors.toList()), SingleReplenichmentResponseDTO.class);

        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    public enum Direction {
        IN, OUT, ALL
    }

}
