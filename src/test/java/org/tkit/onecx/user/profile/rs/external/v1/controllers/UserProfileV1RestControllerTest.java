package org.tkit.onecx.user.profile.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
class UserProfileV1RestControllerTest extends AbstractTest {

    @Test
    void getUserPersonTest() {
        // not found for not existing user profile
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing-user", null))
                .get("person")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // retrieve user person dto
        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .get("person")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPersonDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getDisplayName()).isEqualTo("User Two");
    }

    @Test
    void getUserPreferenceTest() {
        // user 2 no preferences
        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .get("preferences")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPreferencesDTO.class);
        assertThat(result).isNotNull();
        assertThat(result.getPreferences()).isEmpty();

        // user 1 has 4 preferences
        result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get("preferences")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPreferencesDTO.class);
        assertThat(result).isNotNull();
        assertThat(result.getPreferences()).isNotEmpty().hasSize(4);
    }

    @Test
    void getUserProfileTest() {
        // not existing user profile, should be created
        var userPofile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userPofile.getUserId()).isEqualTo("not-existing");
        assertThat(userPofile.getOrganization()).isEqualTo("org1");
        assertThat(userPofile.getPerson().getEmail()).isEqualTo("not-existing@testOrg.de");
        assertThat(userPofile.getAccountSettings().getMenuMode()).isEqualTo(MenuModeDTO.HORIZONTAL);

        // load existing user profile
        userPofile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userPofile).isNotNull();
        assertThat(userPofile.getOrganization()).isEqualTo("org2");
        assertThat(userPofile.getUserId()).isEqualTo("user3");
        assertThat(userPofile.getPerson().getDisplayName()).isEqualTo("User Three");
        assertThat(userPofile.getAccountSettings().getMenuMode()).isEqualTo(MenuModeDTO.SLIM);
        assertThat(userPofile.getIssuer()).isNotNull();
    }

    private static Stream<Arguments> claimTimezone() {
        return Stream.of(
                arguments("timezone", "Europe/Berlin"),
                arguments("timezone", " "),
                arguments("timezone", null));
    }

    @ParameterizedTest
    @MethodSource("claimTimezone")
    void getUserProfileClaimTimeZoneAndLocaleTest(String claimName, String value) {
        // not existing user profile, should be created
        var userPofile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing", "1", claimName, value))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userPofile.getUserId()).isEqualTo("not-existing");
        assertThat(userPofile.getOrganization()).isEqualTo("1");
        assertThat(userPofile.getPerson().getEmail()).isEqualTo("not-existing@testOrg.de");
    }

    @Test
    void getUserSettingsTest() {
        // not existing user settings
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .get("settings")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // load existing user settings
        var userSettings = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .get("settings")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileAccountSettingsDTO.class);

        assertThat(userSettings).isNotNull();
        assertThat(userSettings.getMenuMode()).isEqualTo(MenuModeDTO.SLIM);
        assertThat(userSettings.getColorScheme()).isEqualTo(ColorSchemeDTO.LIGHT);
    }

}
