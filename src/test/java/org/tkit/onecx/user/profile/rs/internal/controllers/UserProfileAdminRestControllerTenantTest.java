package org.tkit.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.user.profile.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileAdminRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-up:read", "ocx-up:write", "ocx-up:delete", "ocx-up:all" })
class UserProfileAdminRestControllerTenantTest extends AbstractTest {

    @Test
    void createUserProfileTest() {
        // create user profile with content
        CreateUserProfileRequestDTO request = new CreateUserProfileRequestDTO();
        request.setUserId("testOrgUser");
        request.setOrganization("testOrg");
        request.setIdentityProvider("database");
        request.setIdentityProviderId("db");
        var person = new CreateUserPersonDTO();
        request.setPerson(person);
        person.setDisplayName("testOrg super user");
        person.setEmail("test@testOrg.com");
        person.setFirstName("Superuser");
        person.setLastName("SuperLastName");

        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user4", "org3"))
                .body(request)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(request.getUserId());
        assertThat(result.getModificationDate()).isNotNull();
        assertThat(result.getPerson().getPhone()).isNull();

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .pathParam("id", "testOrg")
                .header(APM_HEADER_PARAM, createToken("user4", "org2"))
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
        var id = result.getId();

        result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .pathParam("id", id)
                .header(APM_HEADER_PARAM, createToken("user4", "org3"))
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(request.getUserId());
        assertThat(result.getModificationDate()).isNotNull();
        assertThat(result.getPerson().getPhone()).isNull();

    }

    @Test
    void deleteUserProfileTest() {
        // delete existing profile with wrong tenant
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org3"))
                .pathParam("id", "11-111")
                .delete("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        // load deleted profile still found as deleted with wrong tenant
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .pathParam("id", "11-111")
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode());

        // delete existing profile with correct tenant
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .pathParam("id", "11-111")
                .delete("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        // load deleted profile returns not found
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .pathParam("id", "11-111")
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void getUserProfileTest() {
        // get with different tenant
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org2"))
                .pathParam("id", "11-111")
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // get existing profile with correct tenant
        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .pathParam("id", "11-111")
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user1");
        assertThat(result.getAccountSettings().getColorScheme().name())
                .isEqualTo(ColorSchemeDTO.AUTO.name());
        assertThat(result.getPerson().getAddress().getStreet()).isEqualTo("userstreet1");
    }

    @Test
    void searchUserProfileTest() {
        // search with criteria
        // org1
        UserPersonCriteriaDTO criteriaDTO = new UserPersonCriteriaDTO();
        criteriaDTO.setEmail("*testOrg.de");
        var result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .body(criteriaDTO)
                .contentType(APPLICATION_JSON)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfilePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        var resultList = result.getStream();
        assertThat(resultList.get(0).getPerson().getLastName()).isEqualTo("One");

        // org2
        result = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org2"))
                .body(criteriaDTO)
                .contentType(APPLICATION_JSON)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfilePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        resultList = result.getStream();
        assertThat(resultList.get(0).getPerson().getLastName()).isEqualTo("Three");

    }

    @Test
    void updateUserProfileTest() {
        var userProfileDTO = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .pathParam("id", "11-111")
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        UpdateUserProfileRequestDTO requestDTO = new UpdateUserProfileRequestDTO();
        UserPersonDTO userPersonDTO = new UserPersonDTO();
        userPersonDTO.setEmail(userProfileDTO.getPerson().getEmail());
        userPersonDTO.setDisplayName(userProfileDTO.getPerson().getDisplayName());
        userPersonDTO.setFirstName(userProfileDTO.getPerson().getFirstName());
        userPersonDTO.setLastName(userProfileDTO.getPerson().getLastName());
        userPersonDTO.setAddress(userProfileDTO.getPerson().getAddress());
        userPersonDTO.setPhone(userProfileDTO.getPerson().getPhone());
        userPersonDTO.getPhone().setNumber("123456789");
        userPersonDTO.getPhone().setType(PhoneTypeDTO.LANDLINE);
        requestDTO.setPerson(userPersonDTO);
        requestDTO.setModificationCount(userProfileDTO.getModificationCount());

        // update existing profile with wrong tenant
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org2"))
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("id", "11-111")
                .put("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // update existing profile with correct tenant
        var updatedProfile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("id", "11-111")
                .put("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);
        Assertions.assertNotNull(updatedProfile);

        userProfileDTO = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .header(APM_HEADER_PARAM, createToken("user4", "org1"))
                .pathParam("id", "11-111")
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userProfileDTO.getPerson().getPhone().getType()).isEqualTo(requestDTO.getPerson().getPhone().getType());
        assertThat(userProfileDTO.getPerson().getPhone().getNumber()).isEqualTo(requestDTO.getPerson().getPhone().getNumber());
    }
}
