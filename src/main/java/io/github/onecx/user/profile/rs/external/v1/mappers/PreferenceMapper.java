package io.github.onecx.user.profile.rs.external.v1.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.user.profile.rs.external.v1.model.CreateUserPreferenceDTO;
import gen.io.github.onecx.user.profile.rs.external.v1.model.UserPreferenceDTO;
import gen.io.github.onecx.user.profile.rs.external.v1.model.UserPreferencesDTO;
import io.github.onecx.user.profile.domain.models.Preference;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface PreferenceMapper {

    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "id", ignore = true)
    Preference create(CreateUserPreferenceDTO preferencesDTOv1s);

    UserPreferenceDTO map(Preference preferences);

    List<Preference> create(List<UserPreferenceDTO> preferencesDTOv1s);

    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    Preference map(UserPreferenceDTO dto);

    List<UserPreferenceDTO> map(List<Preference> preferences);

    default UserPreferencesDTO find(List<Preference> preferenceList) {
        var preferences = new UserPreferencesDTO();

        preferences.setPreferences(map(preferenceList));

        return preferences;
    }
}
