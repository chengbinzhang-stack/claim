package com.insurance.claimapi.mapper;

import com.insurance.claimapi.dto.ClaimDto;
import com.insurance.claimapi.dto.ClaimRequest;
import com.insurance.claimapi.entity.Claim;
import com.insurance.claimapi.entity.ClaimAttachment;
import com.insurance.claimapi.entity.Policy;
import com.insurance.claimapi.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClaimMapper {

    @Mapping(target = "policyNumber", source = "policy.policyNumber")
    @Mapping(target = "policyType", source = "policy.policyType")
    @Mapping(target = "submittedByName", source = "submittedBy.fullName")
    @Mapping(target = "submittedByEmail", source = "submittedBy.email")
    @Mapping(target = "reviewedByName", source = "reviewedBy.fullName")
    @Mapping(target = "attachments", source = "attachments")
    @Mapping(target = "version", source = "version")
    ClaimDto toDto(Claim claim);

    List<ClaimDto> toDtoList(List<Claim> claims);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "claimNumber", ignore = true)
    @Mapping(target = "policy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedBy", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewNotes", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Claim toEntity(ClaimRequest request);

    default Claim toEntityWithRelations(ClaimRequest request, Policy policy, User submittedBy) {
        Claim claim = toEntity(request);
        claim.setPolicy(policy);
        claim.setSubmittedBy(submittedBy);
        return claim;
    }

    default List<ClaimAttachment> mapAttachments(List<com.insurance.claimapi.dto.AttachmentDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(dto -> ClaimAttachment.builder()
                .fileName(dto.getFileName())
                .filePath(dto.getFilePath())
                .fileType(dto.getFileType())
                .fileSize(dto.getFileSize())
                .build())
                .collect(Collectors.toList());
    }
}
