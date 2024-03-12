package org.tkit.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.user.profile.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class UserProfileRestControllerTest extends AbstractTest {

    @Test
    void createUserPreferenceTest() {
        // create without body
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .post("preferences")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(error.getDetail()).isEqualTo("createUserProfilePreference.createUserPreferenceDTO: must not be null");

        CreateUserPreferenceDTO createUserPreferenceDTO = new CreateUserPreferenceDTO();
        createUserPreferenceDTO.setValue("test");
        createUserPreferenceDTO.setDescription("Test preference");
        createUserPreferenceDTO.setName("TestPreference");
        createUserPreferenceDTO.setApplicationId("TestApp");

        // create with body but not existing user profile
        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createUserPreferenceDTO)
                .header(APM_HEADER_PARAM, createToken("does-not-exist", null))
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
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .post("preferences")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(UserPreferenceDTO.class);

        assertThat(preferenceDto).isNotNull();
        assertThat(preferenceDto.getValue()).isEqualTo(createUserPreferenceDTO.getValue());
    }

    @Test
    void deleteUserPreferenceTest() {
        // delete not existing preference
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "not-existing")
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .delete("preferences/{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        // delete preference for preference of another user
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("user2", null))
                .delete("preferences/{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("PREFERENCE_NOT_FROM_ACTUAL_USER");

        // delete preference for the current logged in user
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .delete("preferences/{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void deleteUserProfileTest() {
        // delete not existing user profile
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
                .header(APM_HEADER_PARAM, createToken("not-existing-user", null))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void getUserPersonTest() {
        // not found for not existing user profile
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing-user", null))
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
        // user 2 no preferences
        var result = given()
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
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userPofile.getUserId()).isEqualTo("not-existing");
        assertThat(userPofile.getPerson().getEmail()).isEqualTo("not-existing@cap.de");

        // load existing user profile
        userPofile = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userPofile).isNotNull();
        assertThat(userPofile.getUserId()).isEqualTo("user3");
        assertThat(userPofile.getPerson().getDisplayName()).isEqualTo("User Three");
        assertThat(userPofile.getPerson().getModificationCount()).isNotNull();
        assertThat(userPofile.getAccountSettings().getModificationCount()).isNotNull();
    }

    @Test
    void getUserSettingsTest() {
        // not existing user settings
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .get("settings")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // load existing user settings
        var userSettings = given()
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

    @Test
    void updateUserPersonTest() {
        // update without body
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .put("person")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS.name());

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
        request.setModificationCount(userPersonDTO4.getModificationCount());

        // Update for not existing profile
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .put("person")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // update email
        var result = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .header(APM_HEADER_PARAM, createToken("user4", "org3"))
                .put("person")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPersonDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(request.getEmail());
        assertThat(result.getFirstName()).isNotNull().isNotEmpty();
        assertThat(result.getPhone()).isNotNull();

        // update 2nd time OPTIMISTIC LOCK EXCEPTION
        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .header(APM_HEADER_PARAM, createToken("user4", "org3"))
                .put("person")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.OPTIMISTIC_LOCK.name());
    }

    @Test
    void updateUserPreferenceTest() {
        // update without body
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .pathParam("id", "11-111")
                .patch("preferences/{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("PREFERENCE_WITH_EMPTY_VALUE");

        // not existing preference
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body("changedTestValue")
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .pathParam("id", "not-existing")
                .patch("preferences/{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // not existing user profile
        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body("changedTestValue")
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .pathParam("id", "11-111")
                .patch("preferences/{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("PREFERENCE_NOT_FROM_ACTUAL_USER");

        // update preference
        var result = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body("changedTestValue")
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .pathParam("id", "11-111")
                .patch("preferences/{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserPreferenceDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("changedTestValue");
    }

    @Test
    void updateUserSettingsTest() {

        // dont send body
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", null))
                .put("settings")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS.name());

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
        request.setMenuMode(userSettings.getMenuMode());
        request.setModificationCount(userSettings.getModificationCount());

        // not existing user profile
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .put("settings")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        request.setMenuMode(MenuModeDTO.SLIMPLUS);
        // not existing user profile
        var result = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .put("settings")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileAccountSettingsDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getMenuMode()).isEqualTo(request.getMenuMode());

        // update 2nd time OPTIMISTIC LOCK EXCEPTION
        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .put("settings")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.OPTIMISTIC_LOCK.name());
    }

}
