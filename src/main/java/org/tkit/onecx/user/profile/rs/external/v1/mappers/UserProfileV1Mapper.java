package org.tkit.onecx.user.profile.rs.external.v1.mappers;

import java.util.HashMap;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tkit.onecx.user.profile.domain.models.*;
import org.tkit.onecx.user.profile.rs.internal.mappers.UserProfileMapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.user.profile.rs.external.v1.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface UserProfileV1Mapper {

    @Mapping(target = "settings", source = "settings", qualifiedByName = "s2o")
    UserProfileDTO map(UserProfile entity);

    UserPersonDTO map(UserPerson entity);

    UserPersonAddressDTO map(UserPersonAddress entity);

    UserPersonPhoneDTO map(UserPersonPhone entity);

    UserProfileAccountSettingsDTO map(UserProfileAccountSettings entity);

    @Named("s2o")
    default Object s2o(String value) {
        ObjectMapper objectMapper = new ObjectMapper();

        if (value == null || value.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            throw new UserProfileMapper.MapperException("Error reading parameter value", e);
        }
    }

}
