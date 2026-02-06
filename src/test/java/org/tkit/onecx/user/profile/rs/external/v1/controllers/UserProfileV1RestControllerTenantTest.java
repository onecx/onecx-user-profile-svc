package org.tkit.onecx.user.profile.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.user.profile.rs.external.v1.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileV1RestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-up:read" })
class UserProfileV1RestControllerTenantTest extends AbstractTest {

    @Test
    void getUserPersonTest() {
        // retrieve user person dto wit wrong tenant. NOT_FOUND as result
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org2"))
                .get("/me/person")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // retrieve user person dto
        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .get("/me/person")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPersonDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getDisplayName()).isEqualTo("User Two");
    }

    @Test
    void getUserPreferenceTest() {
        // get user preference with wrong tenant
        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org3"))
                .get("/me/preferences")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPreferencesDTO.class);
        assertThat(result).isNotNull();
        assertThat(result.getPreferences()).isNullOrEmpty();

        // user 1 has 4 preferences
        result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get("/me/preferences")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPreferencesDTO.class);
        assertThat(result).isNotNull();
        assertThat(result.getPreferences()).isNotEmpty().hasSize(4);
    }

    @Test
    void getUserProfileTest() {
        // load existing user profile with wrong tenant - will be created, as the tenant is from organization stored in keycloak
        var userProfile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org1"))
                .get("/me")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userProfile).isNotNull();
        assertThat(userProfile.getTenantId()).isNotNull();

    }

    @Test
    void getUserSettingsTest() {
        // load existing user settings with wrong tenant - NOT_FOUND as result
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org1"))
                .get("/me/settings")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

    }

}
