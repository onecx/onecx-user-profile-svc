package org.tkit.onecx.user.profile.rs.external.v1.mappers;

import org.mapstruct.Mapper;
import org.tkit.onecx.user.profile.domain.models.*;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.user.profile.rs.external.v1.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface UserProfileV1Mapper {

    UserProfileDTO map(UserProfile entity);

    UserPersonDTO map(UserPerson entity);

    UserPersonAddressDTO map(UserPersonAddress entity);

    UserPersonPhoneDTO map(UserPersonPhone entity);

    UserProfileAccountSettingsDTO map(UserProfileAccountSettings entity);

}
