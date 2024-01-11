package io.github.onecx.user.profile.rs.external.v1.controllers;

import java.io.*;
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
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.user.profile.rs.external.v1.AvatarV1Api;
import io.github.onecx.user.profile.domain.daos.ImageDAO;
import io.github.onecx.user.profile.domain.daos.UserProfileDAO;
import io.github.onecx.user.profile.domain.models.Image;
import io.github.onecx.user.profile.domain.models.UserProfile;
import io.github.onecx.user.profile.rs.external.v1.mappers.AvatarV1Mapper;
import io.github.onecx.user.profile.rs.external.v1.mappers.V1ExceptionMapper;
import io.github.onecx.user.profile.rs.external.v1.service.ImageUtilService;
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
    V1ExceptionMapper exceptionMapper;

    @Inject
    AvatarV1Mapper avatarV1Mapper;

    @Context
    UriInfo uriInfo;

    @ConfigProperty(name = "avatar.small.height", defaultValue = "150")
    Integer smallImgHeight;

    @ConfigProperty(name = "avatar.small.width", defaultValue = "150")
    Integer smallImgWidth;

    @Override
    @Transactional
    public Response deleteUserAvatar() {
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
    public Response getUserAvatar(String id) {
        var avatar = imageDAO.findById(id);

        if (avatar == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var imageByteInputStream = new ByteArrayInputStream(avatar.getImageByte());

        return Response.ok(imageByteInputStream).header("Content-Type", avatar.getMimeType()).build();
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

    @Override
    public Response uploadAvatar(File body) {
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
