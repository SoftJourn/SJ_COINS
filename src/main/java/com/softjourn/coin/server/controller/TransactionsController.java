package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.entity.Transaction;
import com.softjourn.coin.server.repository.TransactionRepository;
import com.softjourn.coin.server.service.GenericFilter;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionsController {

    TransactionRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    public Page<Transaction> getFiltered(GenericFilter<Transaction> filter) {
        return repository.findAll(filter, filter.getPageable());
    }

}
