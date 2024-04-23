package org.tkit.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.domain.service.ImageUtilService;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.image.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.image.rs.internal.model.RefTypeDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ImagesInternalRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class ImagesInternalRestControllerErrorTest extends AbstractTest {

    @InjectMock
    ImageUtilService utilService;

    private static final String MEDIA_TYPE_IMAGE_JPG = "image/jpg";

    private static final File SMALL = new File(
            Objects.requireNonNull(ImagesInternalRestControllerErrorTest.class.getResource("/data/avatar_small.jpg"))
                    .getFile());
    private static final File FILE = new File(
            Objects.requireNonNull(ImagesInternalRestControllerErrorTest.class.getResource("/data/avatar_test.jpg")).getFile());

    @Test
    void uploadImage() throws IOException {
        doThrow(new IOException("test")).when(utilService).resizeImage(any(), any(), any());

        var res = given()
                .pathParam("userId", "user2")
                .queryParam("refType", RefTypeDTO.NORMAL.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        assertThat(res).isNotNull();
    }

}
