package org.tkit.onecx.user.profile.rs.internal.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.tkit.onecx.user.profile.domain.daos.ImageDAO;
import org.tkit.onecx.user.profile.domain.daos.UserProfileDAO;
import org.tkit.onecx.user.profile.domain.models.Image;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.rs.internal.mappers.AvatarMapper;
import org.tkit.onecx.user.profile.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.user.profile.rs.internal.service.ImageUtilService;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.user.profile.rs.internal.AvatarApi;
import lombok.extern.slf4j.Slf4j;

@LogService
@Slf4j
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class AvatarRestController implements AvatarApi {

    @Inject
    UserProfileDAO userProfileDAO;

    @Inject
    ImageDAO imageDAO;

    @Inject
    InternalExceptionMapper exceptionMapper;

    @Inject
    AvatarMapper avatarV1Mapper;

    @Context
    UriInfo uriInfo;

    @ConfigProperty(name = "avatar.small.height", defaultValue = "150")
    Integer smallImgHeight;

    @ConfigProperty(name = "avatar.small.width", defaultValue = "150")
    Integer smallImgWidth;

    @Override
    @Transactional
    public Response deleteUserProfileAvatar() {
        var userProfile = userProfileDAO.getUserProfileByUserId(ApplicationContext.get().getPrincipal(),
                UserProfile.ENTITY_GRAPH_LOAD_ALL);

        if (userProfile != null) {
            userProfile.setAvatar(null);
            userProfile.setSmallAvatar(null);
            userProfileDAO.update(userProfile);
        }

        return Response.noContent().build();
    }

    @Override
    @Transactional
    public Response getUserProfileAvatar(String id) {
        var avatarEntity = imageDAO.findById(id);

        if (avatarEntity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var imageByteInputStream = new ByteArrayInputStream(avatarEntity.getImageByte());

        return Response.ok(imageByteInputStream).header("Content-Type", avatarEntity.getMimeType()).build();
    }

    @Override
    public Response getUserProfileAvatarInfo() {
        var userProfileEntity = userProfileDAO.getUserProfileByUserId(ApplicationContext.get().getPrincipal(),
                UserProfile.ENTITY_GRAPH_LOAD_ALL);

        if (userProfileEntity == null || userProfileEntity.getAvatar() == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var avatarEntity = userProfileEntity.getAvatar();
        var imageInfoDTO = avatarV1Mapper.map(avatarEntity);

        if (userProfileEntity.getUserId().equals(avatarEntity.getModificationUser())) {
            imageInfoDTO.setUserUploaded(true);
        }

        imageInfoDTO.setImageUrl(uriInfo.getPath()
                .concat("/")
                .concat(avatarEntity.getId()));

        imageInfoDTO.setSmallImageUrl((uriInfo.getPath()
                .concat("/")
                .concat(userProfileEntity.getSmallAvatar().getId())));

        return Response.ok(imageInfoDTO).build();
    }

    @Override
    public Response uploadUserProfileAvatar(File body) {
        var userProfile = userProfileDAO.getUserProfileByUserId(ApplicationContext.get().getPrincipal(),
                UserProfile.ENTITY_GRAPH_LOAD_ALL);

        if (userProfile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            byte[] avatarBytes = Files.readAllBytes(body.toPath());
            InputStream avatarIs = new ByteArrayInputStream(avatarBytes);
            var avatarContentType = URLConnection.guessContentTypeFromStream(avatarIs);

            var smallAvatarBytes = this.convertToSmallImage(avatarBytes);

            var avatar = updateUserAvatar(userProfile, avatarBytes, smallAvatarBytes, avatarContentType);

            userProfile = userProfileDAO.getUserProfileByUserId(ApplicationContext.get().getPrincipal(),
                    UserProfile.ENTITY_GRAPH_LOAD_ALL);

            var imageInfo = avatarV1Mapper.map(userProfile.getAvatar());
            imageInfo.setUserUploaded(true);
            imageInfo.setImageUrl(uriInfo.getPath()
                    .concat("/")
                    .concat(avatar.getId()));

            imageInfo.setSmallImageUrl(uriInfo.getPath()
                    .concat("/")
                    .concat(userProfile.getSmallAvatar().getId()));

            return Response.ok(imageInfo).build();
        } catch (Exception ioe) {
            var e = exceptionMapper.exception("UPLOAD_ERROR", ioe.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
        }
    }

    private Image updateUserAvatar(UserProfile userProfile, byte[] avatarBytes, byte[] smallAvatarBytes, String mimeType)
            throws IOException {
        var avatar = new Image();
        userProfile.setAvatar(avatar);
        avatar.setImageByte(avatarBytes);
        avatar.setMimeType(mimeType);
        this.setUpAvatarDimensions(avatar);

        var smallAvatar = new Image();
        userProfile.setSmallAvatar(smallAvatar);
        smallAvatar.setImageByte(smallAvatarBytes);
        smallAvatar.setMimeType("image/png");
        this.setUpAvatarDimensions(smallAvatar);
        userProfile.setSmallAvatar(smallAvatar);

        userProfileDAO.update(userProfile);
        return avatar;
    }

    private byte[] convertToSmallImage(byte[] imgBytesArray) throws IOException {
        return ImageUtilService.resizeImage(imgBytesArray, smallImgWidth, smallImgHeight);
    }

    private void setUpAvatarDimensions(Image avatar) throws IOException {
        var image = ImageIO.read(new ByteArrayInputStream(avatar.getImageByte()));
        avatar.setHeight(image.getHeight());
        avatar.setWidth(image.getWidth());
    }

}
