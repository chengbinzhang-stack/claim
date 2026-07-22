package com.insurance.claimapi.repository;

import com.insurance.claimapi.entity.Claim;
import com.insurance.claimapi.entity.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long>, JpaSpecificationExecutor<Claim> {

    @Query("SELECT c FROM Claim c JOIN FETCH c.policy JOIN FETCH c.submittedBy WHERE c.id = :id")
    Optional<Claim> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT c FROM Claim c JOIN FETCH c.policy JOIN FETCH c.submittedBy WHERE c.claimNumber = :claimNumber")
    Optional<Claim> findByClaimNumberWithDetails(@Param("claimNumber") String claimNumber);

    Page<Claim> findBySubmittedById(Long userId, Pageable pageable);

    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);

    @Query("SELECT c FROM Claim c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:claimNumber IS NULL OR c.claimNumber LIKE %:claimNumber%) AND " +
           "(:fromDate IS NULL OR c.incidentDate >= :fromDate) AND " +
           "(:toDate IS NULL OR c.incidentDate <= :toDate)")
    Page<Claim> findByFilters(
            @Param("status") ClaimStatus status,
            @Param("claimNumber") String claimNumber,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    List<Claim> findBySubmittedByIdAndStatus(Long userId, ClaimStatus status);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status = :status")
    long countByStatus(@Param("status") ClaimStatus status);

    @Query("SELECT c.status, COUNT(c) FROM Claim c GROUP BY c.status")
    List<Object[]> countByStatusGrouped();
}
