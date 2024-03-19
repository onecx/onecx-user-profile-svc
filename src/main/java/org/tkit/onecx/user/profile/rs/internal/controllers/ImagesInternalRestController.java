package org.tkit.onecx.user.profile.rs.internal.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.user.profile.domain.daos.ImageDAO;
import org.tkit.onecx.user.profile.domain.models.Image;
import org.tkit.onecx.user.profile.rs.internal.mappers.ImageMapper;
import org.tkit.onecx.user.profile.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.user.profile.rs.internal.service.ImageUtilService;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.image.rs.internal.ImagesInternalApi;
import gen.org.tkit.onecx.image.rs.internal.model.RefTypeDTO;
import gen.org.tkit.onecx.user.profile.rs.internal.model.ProblemDetailResponseDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ImagesInternalRestController implements ImagesInternalApi {

    @Inject
    InternalExceptionMapper exceptionMapper;

    @Inject
    ImageUtilService utilService;

    @Inject
    ImageDAO imageDAO;

    @Context
    UriInfo uriInfo;

    @Inject
    ImageMapper imageMapper;

    @Context
    HttpHeaders httpHeaders;

    @ConfigProperty(name = "avatar.small.height", defaultValue = "150")
    Integer smallImgHeight;

    @ConfigProperty(name = "avatar.small.width", defaultValue = "150")
    Integer smallImgWidth;

    @Override
    public Response deleteImage(String userId, RefTypeDTO refType) {
        if (refType == null) {
            imageDAO.deleteQueryByRefId(userId);
        } else {
            imageDAO.deleteQueryByRefIdAndRefType(userId, refType);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response deleteMyImage(RefTypeDTO refType) {
        var userId = ApplicationContext.get().getPrincipal();
        if (refType == null) {
            imageDAO.deleteQueryByRefId(userId);
        } else {
            imageDAO.deleteQueryByRefIdAndRefType(userId, refType);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    @Transactional
    public Response getImage(String userId, RefTypeDTO refType) {
        if (refType == null) {
            refType = RefTypeDTO.NORMAL;
        }
        Image image = imageDAO.findByRefIdAndRefType(userId, refType.toString());
        if (image == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(image.getImageData(), image.getMimeType())
                .header(HttpHeaders.CONTENT_LENGTH, image.getLength()).build();
    }

    @Override
    @Transactional
    public Response getMyImage(RefTypeDTO refType) {
        var userId = ApplicationContext.get().getPrincipal();

        return getImage(userId, refType);
    }

    @Override
    public Response updateImage(String userId, byte[] body, Integer contentLength, RefTypeDTO refType) {
        if (refType == null) {
            refType = RefTypeDTO.NORMAL;
        }
        Image image = imageDAO.findByRefIdAndRefType(userId, refType.toString());
        if (image == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var contentType = httpHeaders.getMediaType();
        contentType = new MediaType(contentType.getType(), contentType.getSubtype());

        image.setLength(contentLength);
        image.setMimeType(contentType.toString());
        image.setImageData(body);

        image = imageDAO.update(image);

        return Response.ok(imageMapper.map(image)).build();
    }

    @Override
    public Response updateMyImage(byte[] body, Integer contentLength, RefTypeDTO refType) {
        var userId = ApplicationContext.get().getPrincipal();

        return updateImage(userId, body, contentLength, refType);
    }

    @Override
    public Response uploadImage(Integer contentLength, String userId, byte[] body, RefTypeDTO refType) {
        if (refType == null) {
            refType = RefTypeDTO.NORMAL;
        }
        var contentType = httpHeaders.getMediaType();
        contentType = new MediaType(contentType.getType(), contentType.getSubtype());
        var image = imageMapper.create(userId, refType.toString(), contentType.toString(), contentLength);
        image.setLength(contentLength);
        image.setImageData(body);
        image = imageDAO.create(image);

        if (refType == RefTypeDTO.NORMAL) {
            try {
                checkAndSaveSmallAvatar(body, userId, contentType.toString());
            } catch (IOException e) {
                // if small avatar could not be created ignore it.
                log.error("Error creating small avatar", e);
            }
        }

        var imageInfoDTO = imageMapper.map(image);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(imageInfoDTO.getId()).build())
                .entity(imageInfoDTO)
                .build();
    }

    void checkAndSaveSmallAvatar(byte[] body, String userId, String mimeType)
            throws IOException {
        var smallImage = imageDAO.findByRefIdAndRefType(userId, RefTypeDTO.SMALL.toString());
        var image = ImageIO.read(new ByteArrayInputStream(body));
        // if the image is bigger create also small avatar
        if (smallImage == null
                && image.getHeight() > smallImgHeight
                && image.getWidth() > smallImgWidth) {
            byte[] smallAvatarBytes = utilService.resizeImage(body, smallImgWidth, smallImgHeight);
            Image smallAvatar = new Image();
            smallAvatar.setRefType(RefTypeDTO.SMALL.toString());
            smallAvatar.setImageData(smallAvatarBytes);
            smallAvatar.setLength(smallAvatarBytes.length);
            smallAvatar.setMimeType(mimeType);
            smallAvatar.setUserId(userId);

            imageDAO.create(smallAvatar);
        }
    }

    @Override
    public Response uploadMyImage(Integer contentLength, byte[] body, RefTypeDTO refType) {
        var userId = ApplicationContext.get().getPrincipal();

        return uploadImage(contentLength, userId, body, refType);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
