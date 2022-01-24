package com.softjourn.coin.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.softjourn.coin.server.dto.MobileTransactionDTO;
import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.service.AutocompleteService;
import com.softjourn.coin.server.service.GenericFilter;
import com.softjourn.coin.server.service.TransactionsService;
import com.softjourn.coin.server.util.JsonViews;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/transactions")
public class TransactionsController {

  private final TransactionsService service;
  private final AutocompleteService<Transaction> autocompleteService;

  @PostMapping
  @JsonView(JsonViews.REGULAR.class)
  @PreAuthorize("hasRole('BILLING')")
  public Page<Transaction> getFiltered(@RequestBody GenericFilter<Transaction> filter) {
    return service.getFiltered(filter, filter.getPageable().toPageable());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('BILLING')")
  public Transaction get(@PathVariable Long id) {
    return service.get(id);
  }

  @GetMapping("/my")
  @PreAuthorize("isAuthenticated()")
  public Page<MobileTransactionDTO> getForUser(
      Principal principal, Pageable pageable,
      @RequestParam(required = false, defaultValue = "ALL") Direction direction
  ) {
    return service.getForUser(principal.getName(), pageable, direction);
  }

  @GetMapping("/filter")
  @PreAuthorize("hasRole('BILLING')")
  public Map<String, Object> getFilterOptions() {
    return autocompleteService.getAllPaths(Transaction.class);
  }

  @GetMapping("/filter/autocomplete")
  @PreAuthorize("hasRole('BILLING')")
  public List getAutocompleteOptions(@RequestParam String field) {
    return autocompleteService.getAutocomplete(field);
  }

  @PostMapping("/export")
  @PreAuthorize("hasRole('BILLING')")
  public String export(
      @RequestBody GenericFilter<Transaction> filter
  ) throws IOException, ReflectiveOperationException {
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
