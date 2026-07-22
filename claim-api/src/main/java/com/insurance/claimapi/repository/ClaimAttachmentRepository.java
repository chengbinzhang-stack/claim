package com.insurance.claimapi.repository;

import com.insurance.claimapi.entity.ClaimAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimAttachmentRepository extends JpaRepository<ClaimAttachment, Long> {
    List<ClaimAttachment> findByClaimId(Long claimId);
}
