package org.tkit.onecx.user.profile.rs.external.v1.mappers;

import java.util.HashMap;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tkit.onecx.user.profile.domain.models.*;
import org.tkit.onecx.user.profile.rs.internal.mappers.UserProfileMapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.user.profile.rs.external.v1.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface UserProfileV1Mapper {

    UserProfileDTO map(UserProfile entity);

    UserPersonDTO map(UserPerson entity);

    UserPersonAddressDTO map(UserPersonAddress entity);

    UserPersonPhoneDTO map(UserPersonPhone entity);

    @Mapping(target = "removeSettingsItem", ignore = true)
    @Mapping(target = "settings", source = "settings", qualifiedByName = "s2m")
    UserProfileAccountSettingsDTO map(UserProfileAccountSettings entity);

    @Named("s2m")
    default Map<String, String> s2m(String value) {
        ObjectMapper objectMapper = new ObjectMapper();

        if (value == null || value.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            throw new UserProfileMapper.MapperException("Error reading parameter value", e);
        }
    }

}
