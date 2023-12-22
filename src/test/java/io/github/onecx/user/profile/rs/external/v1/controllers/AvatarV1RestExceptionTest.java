package io.github.onecx.user.profile.rs.external.v1.controllers;

import static jakarta.ws.rs.core.Response.Status.*;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import io.github.onecx.user.profile.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AvatarV1RestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
public class AvatarV1RestExceptionTest extends AbstractTest {

    @Test
    void testAvatarRestControler() throws URISyntaxException, IOException {

        //        Mockito.when(ImageIO.read((InputStream) any())).thenThrow(IOException.class);
        //        File avatar = new File("src/test/resources/data/avatar_test.jpg");
        //
        //        var imageInfo = given()
        //                .when()
        //                .contentType("image/jpg")
        //                .body(avatar)
        //                .header(APM_HEADER_PARAM, createToken("user1", null))
        //                .put()
        //                .then()
        //                .statusCode(OK.getStatusCode())
        //                .extract().as(ImageInfoDTO.class);
        //
        //        assertThat(imageInfo).isNotNull();
        //        assertThat(imageInfo.getSmallImageUrl()).isNotNull();
        //        assertThat(imageInfo.getImageUrl()).isNotNull();
        //
        //        var avatarInfo = given()
        //                .when()
        //                .contentType(APPLICATION_JSON)
        //                .header(APM_HEADER_PARAM, createToken("user1", null))
        //                .get()
        //                .then()
        //                .statusCode(OK.getStatusCode())
        //                .extract().as(ImageInfoDTO.class);
        //
        //        assertThat(avatarInfo).isNotNull();
        //        assertThat(avatarInfo.getImageUrl()).isNotNull().isEqualTo(imageInfo.getImageUrl());
        //        assertThat(avatarInfo.getSmallImageUrl()).isNotNull().isEqualTo(imageInfo.getSmallImageUrl());
        //
        //        Mock filesMock = new Mock(Files.class);
        //        Mockito.when(Files.readAllBytes(any())).thenThrow(IOException.class);
        //        var error = given()
        //                .when()
        //                .contentType("image/jpg")
        //                .body(avatar)
        //                .header(APM_HEADER_PARAM, createToken("user1", null))
        //                .put()
        //                .then()
        //                .statusCode(BAD_REQUEST.getStatusCode())
        //                .extract().as(ProblemDetailResponseDTO.class);
        //
        //        assertThat(error).isNotNull();
        //        assertThat(error.getErrorCode()).isEqualTo("IO_EXCEPTION");
    }
}
