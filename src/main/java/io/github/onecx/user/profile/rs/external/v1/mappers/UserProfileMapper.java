package io.github.onecx.user.profile.rs.external.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.user.profile.rs.external.v1.model.*;
import io.github.onecx.user.profile.domain.models.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface UserProfileMapper {

    @Mapping(target = "version", source = "modificationCount")
    UserProfileDTO map(UserProfile entity);

    UserPersonDTO map(UserPerson entity);

    UserPersonAddressDTO map(UserPersonAddress entity);

    UserPersonPhoneDTO map(UserPersonPhone entity);

    UserProfileAccountSettingsDTO map(UserProfileAccountSettings entity);

    void update(@MappingTarget UserPerson model, UpdateUserPersonDTO dto);

    void update(@MappingTarget UserProfileAccountSettings model, UpdateUserSettingsDTO dto);

}
