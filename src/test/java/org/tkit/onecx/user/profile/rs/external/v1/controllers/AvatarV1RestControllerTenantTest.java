package org.tkit.onecx.user.profile.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.user.profile.rs.external.v1.model.ImageInfoDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AvatarV1RestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class AvatarV1RestControllerTenantTest extends AbstractTest {

    @Test
    void testAvatarRestControler() throws URISyntaxException, IOException {
        var testInfoDto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImageInfoDTO.class);

        assertThat(testInfoDto.getUserUploaded()).isNull();

        // add avatar
        File avatar = new File("src/test/resources/data/avatar_test.jpg");

        // good tenant
        var internalImageInfo = given().basePath("/internal/userProfile/me/avatar")
                .when()
                .contentType("image/jpg")
                .body(avatar)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .put()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(gen.org.tkit.onecx.user.profile.rs.internal.model.ImageInfoDTO.class);

        // wrong tenant get small avatar
        given()
                .when()
                .pathParam("id",
                        internalImageInfo.getSmallImageUrl()
                                .substring(internalImageInfo.getSmallImageUrl().lastIndexOf("/") + 1))
                .header(APM_HEADER_PARAM, createToken("user1", "org3"))
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // good tenant get small avatar
        var smallAvatarByteArray = given()
                .when()
                .pathParam("id",
                        internalImageInfo.getSmallImageUrl()
                                .substring(internalImageInfo.getSmallImageUrl().lastIndexOf("/") + 1))
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
        assertThat(avatarInfo.getImageUrl().substring(avatarInfo.getImageUrl().indexOf("userProfile"))).isNotNull()
                .isEqualTo(internalImageInfo.getImageUrl().substring(internalImageInfo.getImageUrl().indexOf("userProfile")));
        assertThat(avatarInfo.getSmallImageUrl().substring(avatarInfo.getSmallImageUrl().indexOf("userProfile"))).isNotNull()
                .isEqualTo(internalImageInfo.getSmallImageUrl()
                        .substring(internalImageInfo.getSmallImageUrl().indexOf("userProfile")));
    }
}
