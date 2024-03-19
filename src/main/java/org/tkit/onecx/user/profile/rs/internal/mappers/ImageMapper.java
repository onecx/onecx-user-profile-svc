package org.tkit.onecx.user.profile.rs.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.user.profile.domain.models.Image;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.image.rs.internal.model.ImageInfoDTO;

@Mapper(uses = OffsetDateTimeMapper.class)
public interface ImageMapper {

    ImageInfoDTO map(Image image);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageData", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "refType", source = "refType")
    Image create(String userId, String refType, String mimeType, Integer length);
}
