package org.tkit.onecx.user.profile.rs.external.v1.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.user.profile.domain.models.Image;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.user.profile.rs.external.v1.model.ImageInfoDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AvatarV1Mapper {

    @Mapping(target = "userUploaded", ignore = true)
    @Mapping(target = "smallImageUrl", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    ImageInfoDTO map(Image avatar);
}
