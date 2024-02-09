package org.tkit.onecx.user.profile.rs.external.v1.controllers;

import java.io.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.*;

import org.tkit.onecx.user.profile.domain.daos.ImageDAO;
import org.tkit.onecx.user.profile.domain.daos.UserProfileDAO;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.rs.external.v1.mappers.AvatarV1Mapper;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.user.profile.rs.external.v1.AvatarV1Api;
import lombok.extern.slf4j.Slf4j;

@LogService
@Slf4j
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class AvatarV1RestController implements AvatarV1Api {

    @Inject
    UserProfileDAO userProfileDAO;

    @Inject
    ImageDAO imageDAO;

    @Inject
    AvatarV1Mapper avatarV1Mapper;

    @Context
    UriInfo uriInfo;

    @Override
    @Transactional
    public Response getUserAvatar(String id) {
        var avatar = imageDAO.findById(id);

        if (avatar == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(avatar.getImageData(), avatar.getMimeType())
                .header(HttpHeaders.CONTENT_LENGTH, avatar.getLength()).build();
    }

    @Override
    public Response getUserAvatarInfo() {
        var userProfile = userProfileDAO.getUserProfileByUserId(ApplicationContext.get().getPrincipal(),
                UserProfile.ENTITY_GRAPH_LOAD_ALL);

        if (userProfile == null || userProfile.getAvatar() == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var avatar = userProfile.getAvatar();
        var imageInfo = avatarV1Mapper.map(avatar);

        if (userProfile.getUserId().equals(avatar.getModificationUser())) {
            imageInfo.setUserUploaded(true);
        }

        imageInfo.setImageUrl(uriInfo.getPath()
                .concat("/")
                .concat(avatar.getId()));

        imageInfo.setSmallImageUrl((uriInfo.getPath()
                .concat("/")
                .concat(userProfile.getSmallAvatar().getId())));

        return Response.ok(imageInfo).build();
    }

}
