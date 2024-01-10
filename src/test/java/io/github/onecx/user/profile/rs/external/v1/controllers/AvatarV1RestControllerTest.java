package io.github.onecx.user.profile.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.user.profile.rs.external.v1.model.ImageInfoDTO;
import gen.io.github.onecx.user.profile.rs.internal.model.ProblemDetailResponseDTO;
import io.github.onecx.user.profile.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AvatarV1RestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class AvatarV1RestControllerTest extends AbstractTest {

    @Test
    void testAvatarRestControler() throws URISyntaxException, IOException {
        // test empty jpg image
        File emptyAvatar = new File("src/test/resources/data/avatar_empty.jpg");
        var error = given()
                .when()
                .contentType("image/jpg")
                .body(emptyAvatar)
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .put()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("UPLOAD_ERROR");

        var avatarInfo = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImageInfoDTO.class);

        assertThat(avatarInfo).isNotNull();
        assertThat(avatarInfo.getImageUrl()).isNotNull();
        assertThat(avatarInfo.getSmallImageUrl()).isNotNull();

        // add avatar
        File avatar = new File("src/test/resources/data/avatar_test.jpg");

        var imageInfo = given()
                .when()
                .contentType("image/jpg")
                .body(avatar)
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .put()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImageInfoDTO.class);

        assertThat(imageInfo).isNotNull();
        assertThat(imageInfo.getSmallImageUrl()).isNotNull();
        assertThat(imageInfo.getImageUrl()).isNotNull();

        var smallAvatarByteArray = given()
                .when()
                .pathParam("id", imageInfo.getSmallImageUrl().substring(imageInfo.getSmallImageUrl().lastIndexOf("/") + 1))
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .get("{id}")
                .then()
                .contentType("image/png")
                .statusCode(OK.getStatusCode())
                .extract().asByteArray();

        assertThat(smallAvatarByteArray).isNotNull();

        var avatarByteArray = given()
                .when()
                .pathParam("id", imageInfo.getImageUrl().substring(imageInfo.getImageUrl().lastIndexOf("/") + 1))
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .get("{id}")
                .then()
                .contentType("image/jpeg")
                .statusCode(OK.getStatusCode())
                .extract().asByteArray();

        assertThat(avatarByteArray).isNotNull();

        avatarInfo = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImageInfoDTO.class);

        assertThat(avatarInfo).isNotNull();
        assertThat(avatarInfo.getImageUrl()).isNotNull().isEqualTo(imageInfo.getImageUrl());
        assertThat(avatarInfo.getSmallImageUrl()).isNotNull().isEqualTo(imageInfo.getSmallImageUrl());

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void testAvatarRestControlerErrorStates() {
        // delete from not existing profile
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        // get not existing image
        given()
                .when()
                .pathParam("id", "not-existing")
                .header(APM_HEADER_PARAM, createToken("user1", null))
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
