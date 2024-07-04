package org.tkit.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.onecx.user.profile.rs.internal.mappers.InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.io.File;
import java.util.Objects;
import java.util.Random;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.image.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.image.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.image.rs.internal.model.RefTypeDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ImagesInternalRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-up:read", "ocx-up:write", "ocx-up:delete", "ocx-up:all" })
class ImagesInternalRestControllerTest extends AbstractTest {

    private static final String MEDIA_TYPE_IMAGE_PNG = "image/png";
    private static final String MEDIA_TYPE_IMAGE_JPG = "image/jpg";

    private static final File PORTRAIT = new File(
            Objects.requireNonNull(ImagesInternalRestControllerTest.class.getResource("/data/avatar_portrait.jpg")).getFile());
    private static final File SMALL = new File(
            Objects.requireNonNull(ImagesInternalRestControllerTest.class.getResource("/data/avatar_small.jpg")).getFile());
    private static final File FILE = new File(
            Objects.requireNonNull(ImagesInternalRestControllerTest.class.getResource("/data/avatar_test.jpg")).getFile());

    @Test
    void uploadImage() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "user4")
                .queryParam("refType", RefTypeDTO.MEDIUM.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .body(SMALL)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "user2")
                .queryParam("refType", RefTypeDTO.MEDIUM.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .body(PORTRAIT)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "user2")
                .queryParam("refType", RefTypeDTO.SMALL.toString())
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
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "user3")
                .queryParam("refType", RefTypeDTO.SMALL.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user3", "org1"))
                .body(SMALL)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "user3")
                .queryParam("refType", RefTypeDTO.MEDIUM.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user3", "org1"))
                .body(SMALL)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

    }

    @Test
    void uploadImageEmptyBody() {

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "user1")
                .queryParam("refType", RefTypeDTO.SMALL.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo(CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isEqualTo("uploadImage.contentLength: must be greater than or equal to 1");
    }

    @Test
    void uploadImage_shouldReturnBadRequest_whenImageIs() {

        var userId = "productNameUpload";
        var refType = RefTypeDTO.MEDIUM;

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode());

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(exception.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'image_constraints'  Detail: Key (user_id, ref_type, tenant_id)=(productNameUpload, medium, tenant-100) already exists.]");
    }

    @Test
    void getImageJpgTest() {

        var userId = "nameJpg";
        var refType = RefTypeDTO.MEDIUM;

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode());

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("userId", userId)
                .get("{userId}")
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_JPG)
                .extract().body().asByteArray();

        assertThat(data).isNotNull().isNotEmpty();

        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("userId", userId)
                .queryParam("refType", RefTypeDTO.MEDIUM)
                .get("{userId}")
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_JPG)
                .extract().body().asByteArray();

        assertThat(data).isNotNull().isNotEmpty();
    }

    @Test
    void getMyImageJpgTest() {

        var refType = RefTypeDTO.MEDIUM;

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .queryParam("refType", refType)
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("me")
                .then()
                .statusCode(CREATED.getStatusCode());

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .get("me")
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_JPG)
                .extract().body().asByteArray();

        assertThat(data).isNotNull().isNotEmpty();

        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .queryParam("refType", RefTypeDTO.MEDIUM)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .get("me")
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_JPG)
                .extract().body().asByteArray();

        assertThat(data).isNotNull().isNotEmpty();
    }

    @Test
    void getImageTest_shouldReturnNotFound_whenImagesDoesNotExist() {

        var userId = "productNameGetTest";
        var refType = RefTypeDTO.MEDIUM;

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("userId", userId + "_not_exists")
                .queryParam("refType", refType)
                .get("{userId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void updateImage() {

        var userId = "user1";
        var refType = RefTypeDTO.MEDIUM;

        var res = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .put("{userId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        Assertions.assertNotNull(res);

        res = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", RefTypeDTO.MEDIUM)
                .when()
                .body(SMALL)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .put("{userId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        Assertions.assertNotNull(res);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "does-not-exists")
                .queryParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .put("{userId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void updateMyImage() {

        var userId = "user1";
        var refType = RefTypeDTO.MEDIUM;

        var res = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .queryParam("refType", refType)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .put("me")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);
        Assertions.assertNotNull(res);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .queryParam("refType", RefTypeDTO.SMALL)
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
    void updateImage_returnNotFound_whenEntryNotExists() {

        var userId = "productNameUpdateFailed";
        var refType = RefTypeDTO.MEDIUM;

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "wrongRefId")
                .queryParam("refType", "wrongRefType")
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .put("{userId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        Assertions.assertNotNull(exception);
    }

    @Test
    void testMaxUploadSize() {

        var userId = "productMaxUpload";

        byte[] body = new byte[110001];
        new Random().nextBytes(body);

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", RefTypeDTO.MEDIUM)
                .when()
                .body(body)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo(CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isEqualTo(
                "uploadImage.contentLength: must be less than or equal to 110000");

    }

    @Test
    void deleteImage() {
        var userId = "user1";
        var refType = RefTypeDTO.MEDIUM;

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", RefTypeDTO.SMALL)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .delete("{userId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .delete("{userId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("userId", userId)
                .queryParam("refType", refType)
                .get("{userId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteMyImage() {
        var userId = "user1";
        var refType = RefTypeDTO.MEDIUM;

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .queryParam("refType", RefTypeDTO.SMALL)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .when()
                .delete("me")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .delete("me")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .queryParam("refType", refType)
                .get("me")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }
}
