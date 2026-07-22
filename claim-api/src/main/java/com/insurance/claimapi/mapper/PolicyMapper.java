package com.insurance.claimapi.mapper;

import com.insurance.claimapi.dto.PolicyDto;
import com.insurance.claimapi.entity.Policy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

    PolicyMapper INSTANCE = Mappers.getMapper(PolicyMapper.class);

    @Mapping(target = "active", expression = "java(policy.isActive())")
    PolicyDto toDto(Policy policy);
}
