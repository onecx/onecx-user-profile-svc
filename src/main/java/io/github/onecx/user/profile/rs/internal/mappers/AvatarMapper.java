package io.github.onecx.user.profile.rs.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.user.profile.rs.internal.model.ImageInfoDTO;
import io.github.onecx.user.profile.domain.models.Image;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AvatarMapper {

    @Mapping(target = "userUploaded", ignore = true)
    @Mapping(target = "smallImageUrl", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    ImageInfoDTO map(Image avatar);
}
