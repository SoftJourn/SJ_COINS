package com.softjourn.coin.server.service;

import com.softjourn.coin.server.dto.CertificateDTO;
import com.softjourn.coin.server.entity.FabricAccount;

public interface CertificateService {

    CertificateDTO generate(String ldap);

    FabricAccount newFabricAccount(String ldap);

}
