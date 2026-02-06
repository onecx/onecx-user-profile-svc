package org.tkit.onecx.user.profile.rs.internal.mappers;

import java.util.HashMap;

import org.mapstruct.*;
import org.tkit.onecx.user.profile.domain.criteria.UserPersonCriteria;
import org.tkit.onecx.user.profile.domain.models.*;
import org.tkit.onecx.user.profile.domain.models.enums.ColorScheme;
import org.tkit.onecx.user.profile.domain.models.enums.MenuMode;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gen.org.tkit.onecx.user.profile.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface UserProfileMapper {

    default UserProfile create(CreateUserProfileRequestDTO dto) {
        var userProfile = new UserProfile();

        userProfile.setOrganization(dto.getOrganization());
        userProfile.setUserId(dto.getUserId());
        userProfile.setIdentityProvider(dto.getIdentityProvider());
        userProfile.setIdentityProviderId(dto.getIdentityProviderId());

        userProfile.setAccountSettings(new UserProfileAccountSettings());

        userProfile.setPerson(create(dto.getPerson()));

        return userProfile;
    }

    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "address", ignore = true)
    UserPerson create(CreateUserPersonDTO dto);

    @Named("mapProfile")
    default UserProfileDTO mapProfile(UserProfile entity) {
        UserProfileDTO dto = map(entity);
        if (dto.getPerson() == null) {
            dto.setPerson(new UserPersonDTO());
        }
        if (dto.getAccountSettings() == null) {
            dto.setAccountSettings(new UserProfileAccountSettingsDTO());
        }
        if (dto.getSettings() == null) {
            ObjectNode settings = new ObjectMapper().createObjectNode();
            settings.put("menuMode", MenuMode.STATIC.toString());
            settings.put("colorScheme", ColorScheme.AUTO.toString());
            dto.setSettings(settings);
        }
        return dto;
    }

    @Mapping(target = "settings", source = "settings", qualifiedByName = "s2o")
    UserProfileDTO map(UserProfile entity);

    default UserPersonDTO mapUserPerson(UserProfile entity) {
        var dto = map(entity.getPerson());
        if (dto == null) {
            dto = new UserPersonDTO();
        }
        return dto;
    }

    UserPersonDTO map(UserPerson entity);

    UserPersonAddressDTO map(UserPersonAddress entity);

    UserPersonPhoneDTO map(UserPersonPhone entity);

    default UserProfileAccountSettingsDTO mapAccountSettings(UserProfile entity) {
        var dto = map(entity.getAccountSettings());
        if (dto == null) {
            dto = new UserProfileAccountSettingsDTO();
        }
        return dto;
    }

    UserProfileAccountSettingsDTO map(UserProfileAccountSettings entity);

    UserPersonCriteria map(UserPersonCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserProfilePageResultDTO mapPageResult(PageResult<UserProfile> page);

    @Named("s2o")
    default Object s2o(String value) {
        ObjectMapper objectMapper = new ObjectMapper();

        if (value == null || value.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            throw new MapperException("Error reading settings values", e);
        }
    }

    @Named("o2s")
    default String o2s(Object value) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new MapperException("Error reading settings values", e);
        }
    }

    @Mapping(source = "accountSettings", target = "settings", qualifiedByName = "o2s")
    UserProfile mapSettingsToString(UserProfile userProfile);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "issuer", ignore = true)
    @Mapping(target = "identityProviderId", ignore = true)
    @Mapping(target = "identityProvider", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "settings", source = "settings", qualifiedByName = "o2s")
    @Mapping(target = "accountSettings", source = "settings", qualifiedByName = "mirrorSettingsToLegacySettings")
    void updateProfile(@MappingTarget UserProfile userProfile, UpdateUserProfileRequestDTO updateUserProfileRequestDTO);

    @Named("mirrorSettingsToLegacySettings")
    default UserProfileAccountSettings mirrorSettings(Object settings) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (settings == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(settings);
            return objectMapper.readValue(json, UserProfileAccountSettings.class);
        } catch (Exception e) {
            throw new MapperException("Error reading settings values", e);
        }
    }

    class MapperException extends RuntimeException {

        public MapperException(String msg, Throwable t) {
            super(msg, t);
        }

    }
}
