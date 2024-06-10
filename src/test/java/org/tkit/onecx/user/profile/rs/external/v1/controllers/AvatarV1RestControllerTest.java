package org.tkit.onecx.user.profile.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.user.profile.rs.external.v1.model.ImageInfoDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AvatarV1RestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class AvatarV1RestControllerTest extends AbstractTest {

    private static final String MEDIA_TYPE_IMAGE_PNG = "image/png";
    private static final String MEDIA_TYPE_IMAGE_JPG = "image/jpg";

    @Test
    void testAvatarRestControler() throws URISyntaxException, IOException {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // add avatar
        File avatar = new File("src/test/resources/data/avatar_test.jpg");

        // good tenant
        given().basePath("/internal/images/me")
                .when()
                .contentType("image/jpg")
                .body(avatar)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(ImageInfoDTO.class);

        var avatarByteArray = given()
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_JPG)
                .extract().body().asByteArray();

        assertThat(avatarByteArray).isNotNull();
    }

    @Test
    void testAvatarRestControlerErrorStates() {
        // get not existing image
        given()
                .when()
                .pathParam("id", "not-existing")
                .header(APM_HEADER_PARAM, createToken("user2", null))
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // user does not exist for avatar info
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }
}
