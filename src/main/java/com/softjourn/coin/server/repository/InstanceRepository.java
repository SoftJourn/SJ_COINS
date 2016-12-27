package com.softjourn.coin.server.repository;

import com.softjourn.coin.server.entity.Instance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanceRepository extends JpaRepository<Instance, Long> {

    Instance findByAddress(String address);

    List<Instance> findByContractId(Long id);

}
