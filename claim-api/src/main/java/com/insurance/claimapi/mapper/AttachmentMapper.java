package com.insurance.claimapi.mapper;

import com.insurance.claimapi.dto.AttachmentDto;
import com.insurance.claimapi.entity.ClaimAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    AttachmentMapper INSTANCE = Mappers.getMapper(AttachmentMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "claim", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    AttachmentDto toDto(ClaimAttachment attachment);

    List<AttachmentDto> toDtoList(List<ClaimAttachment> attachments);
}
