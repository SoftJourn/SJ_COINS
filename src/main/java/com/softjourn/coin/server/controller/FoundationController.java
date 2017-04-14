package com.softjourn.coin.server.controller;

import com.softjourn.coin.server.dto.FoundationInfoDTO;
import com.softjourn.coin.server.dto.FoundationTransactionResultDTO;
import com.softjourn.coin.server.dto.ApproveDTO;
import com.softjourn.coin.server.dto.WithdrawDTO;
import com.softjourn.coin.server.service.FoundationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/v1/foundation/")
public class FoundationController {

    private FoundationService foundationService;

    @Autowired
    public FoundationController(FoundationService foundationService) {
        this.foundationService = foundationService;
    }

    /**
     * This method should be used to do donates and exchanges
     *
     * @param dto
     * @param principal
     * @return FoundationTransactionResultDTO
     * @throws IOException
     */
    @PreAuthorize("authenticated")
    @RequestMapping(value = "/approve", method = RequestMethod.POST)
    public FoundationTransactionResultDTO approve(@RequestBody ApproveDTO dto, Principal principal) throws IOException {
        return (FoundationTransactionResultDTO) foundationService.approve(dto, principal).getValue();
    }

    /**
     * This method should be used to close crowdsale campaign
     *
     * @param address
     * @param principal
     * @return FoundationTransactionResultDTO
     * @throws IOException
     */
    @PreAuthorize("authenticated")
    @RequestMapping(value = "/close/{address}", method = RequestMethod.POST)
    public FoundationTransactionResultDTO close(@PathVariable String address, Principal principal) throws IOException {
        return (FoundationTransactionResultDTO) foundationService.close(address, principal).getValue();
    }

    /**
     * Method should be used to withdraw coins
     *
     * @param address
     * @param dto
     * @return FoundationTransactionResultDTO
     * @throws IOException
     */
    @PreAuthorize("authenticated")
    @RequestMapping(value = "/withdraw/{address}", method = RequestMethod.POST)
    public FoundationTransactionResultDTO withdraw(@PathVariable String address, @RequestBody WithdrawDTO dto) throws IOException {
        return (FoundationTransactionResultDTO) foundationService.withdraw(address, dto).getValue();
    }

    /**
     * Method should be used to get current values of foundation contract fields
     *
     * @param address
     * @return FoundationInfoDTO
     * @throws IOException
     */
    @PreAuthorize("authenticated")
    @RequestMapping(value = "/{address}", method = RequestMethod.GET)
    public FoundationInfoDTO getInfo(@PathVariable String address) throws IOException {
        return foundationService.getInfo(address);
    }

}
