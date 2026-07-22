package com.insurance.policyservice.repository;

import com.insurance.policyservice.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {
}
