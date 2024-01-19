package io.github.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.user.profile.rs.internal.model.*;
import io.github.onecx.user.profile.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class UserProfileRestControllerTenantTest extends AbstractTest {

    @Test
    void createUserPreferenceTest() {
        CreateUserPreferenceDTO createUserPreferenceDTO = new CreateUserPreferenceDTO();
        createUserPreferenceDTO.setValue("test");
        createUserPreferenceDTO.setDescription("Test preference");
        createUserPreferenceDTO.setName("TestPreference");
        createUserPreferenceDTO.setApplicationId("TestApp");

        // create preference with wrong tenant
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createUserPreferenceDTO)
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .post("preferences")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("USER_PROFILE_DOES_NOT_EXIST");

        // create preference with existing user profile
        var preferenceDto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createUserPreferenceDTO)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .post("preferences")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(UserPreferenceDTO.class);

        assertThat(preferenceDto).isNotNull();
        assertThat(preferenceDto.getValue()).isEqualTo(createUserPreferenceDTO.getValue());
    }

    @Test
    void deleteUserPreferenceTest() {
        // delete preference for the current logged in user with wrong tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .delete("preferences/{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var result = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get("preferences")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPreferencesDTO.class);
        assertThat(result).isNotNull();
        assertThat(result.getPreferences()).isNotEmpty().hasSize(4);

        // delete preference for the current logged in user with correct tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .delete("preferences/{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        result = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get("preferences")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPreferencesDTO.class);
        assertThat(result).isNotNull();
        assertThat(result.getPreferences()).isNotEmpty().hasSize(3);
    }

    @Test
    void deleteUserProfileTest() {
        // delete user profile with wrong tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org1"))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var userPofile = given()
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
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        userPofile = given()
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
    void getUserPersonTest() {
        // retrieve user person dto wit wrong tenant. NOT_FOUND as result
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org2"))
                .get("person")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // retrieve user person dto
        var result = given()
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
        // get user preference with wrong tenant
        var result = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org3"))
                .get("preferences")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPreferencesDTO.class);
        assertThat(result).isNotNull();
        assertThat(result.getPreferences()).isNullOrEmpty();

        // user 1 has 4 preferences
        result = given()
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
        // load existing user profile with wrong tenant - will be created, as the tenant is from organization stored in keycloak
        var userProfile = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userProfile.getModificationDate()).isAfterOrEqualTo(OffsetDateTime.now().minusSeconds(1));
    }

    @Test
    void getUserSettingsTest() {
        // load existing user settings with wrong tenant - NOT_FOUND as result
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org1"))
                .get("settings")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

    }

    @Test
    void updateUserPersonTest() {
        var userPersonDTO4 = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user4", "org3"))
                .get("person")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPersonDTO.class);
        UpdateUserPersonRequestDTO request = new UpdateUserPersonRequestDTO();
        request.setEmail("new_email@capgemini.com");
        request.setLastName(userPersonDTO4.getLastName());
        request.setFirstName(userPersonDTO4.getFirstName());
        request.setDisplayName(userPersonDTO4.getDisplayName());
        request.setAddress(userPersonDTO4.getAddress());
        request.setPhone(userPersonDTO4.getPhone());

        // update email with wrong tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .put("person")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void updateUserPreferenceTest() {
        // update preference with wrong tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body("changedTestValue")
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .pathParam("id", "11-111")
                .patch("preferences/{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void updateUserSettingsTest() {
        var userSettings = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .get("settings")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileAccountSettingsDTO.class);
        UpdateUserSettingsDTO request = new UpdateUserSettingsDTO();
        request.setColorScheme(userSettings.getColorScheme());
        request.setLocale(userSettings.getLocale());
        request.setHideMyProfile(userSettings.getHideMyProfile());
        request.setTimezone(userSettings.getTimezone());
        request.setMenuMode(MenuModeDTO.SLIMPLUS);

        // update user settings with wrong tenant
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .header(APM_HEADER_PARAM, createToken("user3", "org3"))
                .put("settings")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }
}
