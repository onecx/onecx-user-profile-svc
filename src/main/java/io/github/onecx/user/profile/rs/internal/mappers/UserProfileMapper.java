package io.github.onecx.user.profile.rs.internal.mappers;

import java.util.List;
import java.util.stream.Stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.user.profile.rs.internal.model.*;
import io.github.onecx.user.profile.domain.criteria.UserPersonCriteria;
import io.github.onecx.user.profile.domain.models.*;

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

    UserProfileDTO map(UserProfile entity);

    default UserPersonDTO mapUserPerson(UserProfile entity) {
        var dto = map(entity.getPerson());
        dto.setModificationCount(entity.getModificationCount());

        return dto;
    }

    @Mapping(target = "modificationCount", ignore = true)
    UserPersonDTO map(UserPerson entity);

    UserPersonAddressDTO map(UserPersonAddress entity);

    UserPersonPhoneDTO map(UserPersonPhone entity);

    default UserProfileAccountSettingsDTO mapAccountSettings(UserProfile entity) {
        var dto = map(entity.getAccountSettings());
        dto.setModificationCount(entity.getModificationCount());
        return dto;
    }

    @Mapping(target = "modificationCount", ignore = true)
    UserProfileAccountSettingsDTO map(UserProfileAccountSettings entity);

    default void updateUserPerson(UserProfile model, UpdateUserPersonRequestDTO dto) {
        model.setModificationCount(dto.getModificationCount());
        update(model.getPerson(), dto);
    }

    void update(@MappingTarget UserPerson model, UpdateUserPersonRequestDTO dto);

    UserPersonCriteria map(UserPersonCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    @Mapping(target = "stream", qualifiedByName = "mapStream")
    UserProfilePageResultDTO mapPageResult(PageResult<UserProfile> page);

    @Named("mapStream")
    List<UserProfileDTO> mapStream(Stream<UserProfile> stream);

    default void updateUserSettings(UserProfile model, UpdateUserSettingsDTO dto) {
        model.setModificationCount(dto.getModificationCount());
        update(model.getAccountSettings(), dto);
    }

    void update(@MappingTarget UserProfileAccountSettings model, UpdateUserSettingsDTO dto);

}
