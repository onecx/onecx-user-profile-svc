package org.tkit.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.user.profile.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-up:read", "ocx-up:write", "ocx-up:delete", "ocx-up:all" })
class UserProfileRestControllerTest extends AbstractTest {

    @Test
    void deleteUserProfileTest() {
        // delete not existing user profile
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", null))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing-user", null))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void getUserProfileWrongTokenTest() {
        // not existing user profile, should be created
        var up = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing", null, "given_name", 123))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(up.getUserId()).isEqualTo("not-existing");
        assertThat(up.getPerson().getFirstName()).isNull();
        assertThat(up.getPerson().getEmail()).isEqualTo("not-existing@testOrg.de");
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
        assertThat(userPofile.getSettings()).isNotNull();
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
        assertThat(userPofile.getUserId()).isEqualTo("user3");
        assertThat(userPofile.getOrganization()).isEqualTo("org2");
        assertThat(userPofile.getPerson().getDisplayName()).isEqualTo("User Three");

        // load second user profile
        userPofile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user4", "org3"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userPofile).isNotNull();
        assertThat(userPofile.getUserId()).isEqualTo("user4");
        assertThat(userPofile.getOrganization()).isEqualTo("Company3");
        assertThat(userPofile.getPerson().getDisplayName()).isEqualTo("User Four");
    }

    @Test
    void updateUserProfileTest() throws JsonProcessingException {

        UpdateUserProfileRequestDTO updateDTO = new UpdateUserProfileRequestDTO();

        ObjectMapper objectMapper = new ObjectMapper();
        Object settingsObject = objectMapper.readValue("{\"locale\":\"testLocale\"}", Object.class);
        updateDTO.setSettings(settingsObject);

        updateDTO.setModificationCount(0);
        UserPersonDTO personDTO = new UserPersonDTO();
        personDTO.setEmail("updated@email.de");
        updateDTO.setPerson(personDTO);

        //try to update without body
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .put()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        var updatedProfile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .body(updateDTO)
                .put()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(updatedProfile.getPerson().getEmail()).isEqualTo(updateDTO.getPerson().getEmail());
        assertThat(updatedProfile.getSettings()).isNotNull();
        assertThat(updatedProfile.getAccountSettings().getLocale()).isEqualTo("testLocale");

        //second time with same modificationCount => optlock exception
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .body(updateDTO)
                .put()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateUserProfileNotFoundTest() throws JsonProcessingException {

        UpdateUserProfileRequestDTO updateDTO = new UpdateUserProfileRequestDTO();

        ObjectMapper objectMapper = new ObjectMapper();
        Object settingsObject = objectMapper.readValue("{\"locale\":\"testLocale\"}", Object.class);
        updateDTO.setSettings(settingsObject);

        updateDTO.setModificationCount(0);
        UserPersonDTO personDTO = new UserPersonDTO();
        personDTO.setEmail("updated@email.de");
        updateDTO.setPerson(personDTO);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user123", "org123"))
                .body(updateDTO)
                .put()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

}
