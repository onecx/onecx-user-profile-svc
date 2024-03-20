package org.tkit.onecx.user.profile.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.*;

import org.tkit.onecx.user.profile.domain.daos.ImageDAO;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.user.profile.rs.external.v1.AvatarV1Api;
import gen.org.tkit.onecx.user.profile.rs.external.v1.model.RefTypeDTO;
import lombok.extern.slf4j.Slf4j;

@LogService
@Slf4j
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class AvatarV1RestController implements AvatarV1Api {

    @Inject
    ImageDAO imageDAO;

    @Override
    @Transactional
    public Response getMyImage(RefTypeDTO refType) {
        var userId = ApplicationContext.get().getPrincipal();

        var image = imageDAO.findByRefIdAndRefType(userId, refType.toString());
        if (image == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(image.getImageData(), image.getMimeType())
                .header(HttpHeaders.CONTENT_LENGTH, image.getLength()).build();
    }
}
