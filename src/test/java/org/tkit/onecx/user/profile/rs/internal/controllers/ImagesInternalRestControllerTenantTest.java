package org.tkit.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Objects;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.image.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.image.rs.internal.model.RefTypeDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ImagesInternalRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class ImagesInternalRestControllerTenantTest extends AbstractTest {

    private static final String MEDIA_TYPE_IMAGE_JPG = "image/jpg";

    private static final File PORTRAIT = new File(
            Objects.requireNonNull(ImagesInternalRestControllerTenantTest.class.getResource("/data/avatar_portrait.jpg"))
                    .getFile());
    private static final File SMALL = new File(
            Objects.requireNonNull(ImagesInternalRestControllerTenantTest.class.getResource("/data/avatar_small.jpg"))
                    .getFile());
    private static final File FILE = new File(
            Objects.requireNonNull(ImagesInternalRestControllerTenantTest.class.getResource("/data/avatar_test.jpg"))
                    .getFile());

    @Test
    void uploadImage() {
        given()
                .pathParam("userId", "user4")
                .queryParam("refType", RefTypeDTO.NORMAL.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .body(SMALL)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        given()
                .pathParam("userId", "user4")
                .queryParam("refType", RefTypeDTO.NORMAL.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org2"))
                .body(SMALL)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

    }

    @Test
    void getImageJpgTest() {

        var userId = "user2";
        var refType = RefTypeDTO.NORMAL;

        given()
                .pathParam("userId", userId)
                .queryParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode());

        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org2"))
                .pathParam("userId", userId)
                .get("{userId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        var data = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .pathParam("userId", userId)
                .queryParam("refType", RefTypeDTO.SMALL)
                .get("{userId}")
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_JPG)
                .extract().body().asByteArray();

        assertThat(data).isNotNull().isNotEmpty();
    }

    @Test
    void getMyImageJpgTest() {

        var refType = RefTypeDTO.NORMAL;

        given()
                .queryParam("refType", refType)
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("me")
                .then()
                .statusCode(CREATED.getStatusCode());

        var data = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .get("me")
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_JPG)
                .extract().body().asByteArray();

        assertThat(data).isNotNull().isNotEmpty();

        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org2"))
                .get("me")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void updateImage() {

        var userId = "user1";
        var refType = RefTypeDTO.NORMAL;

        given()
                .pathParam("userId", userId)
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .put("{userId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        var res = given()
                .pathParam("userId", userId)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .put("{userId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        Assertions.assertNotNull(res);
    }

    @Test
    void updateMyImage() {

        var userId = "user1";
        var refType = RefTypeDTO.NORMAL;

        given()
                .when()
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .put("me")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        var res = given()
                .when()
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .body(SMALL)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .put("me")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);
        Assertions.assertNotNull(res);
    }

    @Test
    void deleteImage() {
        var userId = "user1";
        var refType = RefTypeDTO.NORMAL;

        given()
                .pathParam("userId", userId)
                .queryParam("refType", RefTypeDTO.SMALL)
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .delete("{userId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .pathParam("userId", userId)
                .queryParam("refType", RefTypeDTO.SMALL)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .delete("{userId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void deleteMyImage() {

        var refType = RefTypeDTO.NORMAL;

        given()
                .queryParam("refType", RefTypeDTO.SMALL)
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .when()
                .delete("me")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .queryParam("refType", RefTypeDTO.SMALL)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .when()
                .delete("me")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }
}
