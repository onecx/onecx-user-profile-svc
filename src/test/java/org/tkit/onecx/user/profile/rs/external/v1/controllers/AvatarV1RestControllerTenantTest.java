package org.tkit.onecx.user.profile.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;

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
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // add avatar
        File avatar = new File("src/test/resources/data/avatar_test.jpg");

        // good tenant
        var internalImageInfo = given().basePath("/internal/images/me")
                .when()
                .contentType("image/jpg")
                .body(avatar)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(ImageInfoDTO.class);

        // get avatar info with wrong tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org3"))
                .get()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

    }
}
