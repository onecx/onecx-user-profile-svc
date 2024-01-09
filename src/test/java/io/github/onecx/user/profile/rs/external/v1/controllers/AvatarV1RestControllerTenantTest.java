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
import io.github.onecx.user.profile.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AvatarV1RestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
public class AvatarV1RestControllerTenantTest extends AbstractTest {

    @Test
    void testAvatarRestControler() throws URISyntaxException, IOException {
        // add avatar
        File avatar = new File("src/test/resources/data/avatar_test.jpg");

        // wrong tenant
        given()
                .when()
                .contentType("image/jpg")
                .body(avatar)
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .put()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // good tenant
        var imageInfo = given()
                .when()
                .contentType("image/jpg")
                .body(avatar)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .put()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImageInfoDTO.class);

        // wrong tenant get small avatar
        given()
                .when()
                .pathParam("id", imageInfo.getSmallImageUrl().substring(imageInfo.getSmallImageUrl().lastIndexOf("/") + 1))
                .header(APM_HEADER_PARAM, createToken("user1", "org3"))
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // good tenant get small avatar
        var smallAvatarByteArray = given()
                .when()
                .pathParam("id", imageInfo.getSmallImageUrl().substring(imageInfo.getSmallImageUrl().lastIndexOf("/") + 1))
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get("{id}")
                .then()
                .contentType("image/png")
                .statusCode(OK.getStatusCode())
                .extract().asByteArray();

        assertThat(smallAvatarByteArray).isNotNull();

        // get avatar info with wrong tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org3"))
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // get avatar info with good tenant
        var avatarInfo = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImageInfoDTO.class);

        assertThat(avatarInfo).isNotNull();
        assertThat(avatarInfo.getImageUrl()).isNotNull().isEqualTo(imageInfo.getImageUrl());
        assertThat(avatarInfo.getSmallImageUrl()).isNotNull().isEqualTo(imageInfo.getSmallImageUrl());

        // delete with wrong tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode());

        // delete with good tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }
}
