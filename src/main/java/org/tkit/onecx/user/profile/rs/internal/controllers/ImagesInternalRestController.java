package org.tkit.onecx.user.profile.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.*;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.user.profile.domain.daos.ImageDAO;
import org.tkit.onecx.user.profile.domain.models.Image;
import org.tkit.onecx.user.profile.rs.internal.mappers.ImageMapper;
import org.tkit.onecx.user.profile.rs.internal.mappers.InternalExceptionMapper;
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
    ImageDAO imageDAO;

    @Context
    UriInfo uriInfo;

    @Inject
    ImageMapper imageMapper;

    @Context
    HttpHeaders httpHeaders;

    @Override
    public Response deleteImage(String userId) {
        imageDAO.deleteQueryByRefId(userId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response deleteMyImage() {
        var userId = ApplicationContext.get().getPrincipal();
        imageDAO.deleteQueryByRefId(userId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    @Transactional
    public Response getImage(String userId, RefTypeDTO refType) {
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
    public Response updateImage(String userId, RefTypeDTO refType, byte[] body, Integer contentLength) {
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
    public Response updateMyImage(RefTypeDTO refType, byte[] body, Integer contentLength) {
        var userId = ApplicationContext.get().getPrincipal();

        return updateImage(userId, refType, body, contentLength);
    }

    @Override
    public Response uploadImage(Integer contentLength, String userId, RefTypeDTO refType, byte[] body) {
        var contentType = httpHeaders.getMediaType();
        contentType = new MediaType(contentType.getType(), contentType.getSubtype());
        var image = imageMapper.create(userId, refType.toString(), contentType.toString(), contentLength);
        image.setLength(contentLength);
        image.setImageData(body);
        image = imageDAO.create(image);

        var imageInfoDTO = imageMapper.map(image);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(imageInfoDTO.getId()).build())
                .entity(imageInfoDTO)
                .build();
    }

    @Override
    public Response uploadMyImage(Integer contentLength, RefTypeDTO refType, byte[] body) {
        var userId = ApplicationContext.get().getPrincipal();

        return uploadImage(contentLength, userId, refType, body);
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
