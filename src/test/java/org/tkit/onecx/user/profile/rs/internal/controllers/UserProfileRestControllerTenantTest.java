package org.tkit.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.user.profile.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-up:read", "ocx-up:write", "ocx-up:delete", "ocx-up:all" })
class UserProfileRestControllerTenantTest extends AbstractTest {

    @Test
    void deleteUserProfileTest() {
        // delete user profile with wrong tenant
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org1"))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var userPofile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userPofile).isNotNull();
        assertThat(userPofile.getUserId()).isEqualTo("user3");

        // delete user profile with correct tenant
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        userPofile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userPofile.getModificationDate()).isAfterOrEqualTo(OffsetDateTime.now().minusSeconds(1));
    }

    @Test
    void getUserProfileTest() {
        // load existing user profile with wrong tenant - will be created, as the tenant is from organization stored in keycloak
        var userProfile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userProfile.getModificationDate()).isAfterOrEqualTo(OffsetDateTime.now().minusSeconds(1));
    }
}
