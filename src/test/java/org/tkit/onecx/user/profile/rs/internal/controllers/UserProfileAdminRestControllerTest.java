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
@TestHTTPEndpoint(UserProfileAdminRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class UserProfileAdminRestControllerTest extends AbstractTest {

    @Test
    void createUserProfileTest() {
        // test not sending the body
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(error.getDetail()).isEqualTo("createUserProfileData.createUserProfileRequestDTO: must not be null");

        // create user profile with content
        CreateUserProfileRequestDTO request = new CreateUserProfileRequestDTO();
        request.setUserId("cap");
        request.setOrganization("capgemini");
        request.setIdentityProvider("database");
        request.setIdentityProviderId("db");
        var person = new CreateUserPersonDTO();
        request.setPerson(person);
        person.setDisplayName("Capgemini super user");
        person.setEmail("cap@capgemini.com");
        person.setFirstName("Superuser");
        person.setLastName("Capgeminius");

        var result = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .body(request)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(request.getUserId());
        assertThat(result.getModificationDate()).isNotNull();
        assertThat(result.getPerson().getPhone()).isNull();

        result = given()
                .when()
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .pathParam("id", "cap")
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(request.getUserId());
        assertThat(result.getModificationDate()).isNotNull();
        assertThat(result.getPerson().getPhone()).isNull();

        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user3", "org2"))
                .body(request)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(error.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'up_constraints'  Detail: Key (user_id, tenant_id)=(cap, tenant-200) already exists.]");
    }

    @Test
    void deleteUserProfileTest() {
        // delete not existing profile
        given()
                .when()
                .pathParam("id", "not-existing")
                .delete("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        // delete existing profile
        given()
                .when()
                .pathParam("id", "user1")
                .delete("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        // load deleted profile
        given()
                .when()
                .pathParam("id", "user1")
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void getUserProfileTest() {
        // get existing profile
        var result = given()
                .when()
                .pathParam("id", "user1")
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
        // search without criteria
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .post("search")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(error.getDetail()).isEqualTo("searchUserProfileData.userPersonCriteriaDTO: must not be null");

        // search with criteria
        UserPersonCriteriaDTO criteriaDTO = new UserPersonCriteriaDTO();
        criteriaDTO.setEmail("*cap.de");
        var result = given()
                .when()
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

        // search with empty criteria
        criteriaDTO = new UserPersonCriteriaDTO();
        result = given()
                .when()
                .body(criteriaDTO)
                .contentType(APPLICATION_JSON)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfilePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        resultList = result.getStream();
        assertThat(resultList.get(0).getPerson().getLastName()).isEqualTo("One");

        // search with all criteria filled
        criteriaDTO = new UserPersonCriteriaDTO();
        criteriaDTO.setEmail("email");
        criteriaDTO.setUserId("userId");
        criteriaDTO.setFirstName("firstName");
        criteriaDTO.setLastName("lastName");
        result = given()
                .when()
                .body(criteriaDTO)
                .contentType(APPLICATION_JSON)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfilePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isZero();

        // search with all criteria filled with * or ?
        criteriaDTO = new UserPersonCriteriaDTO();
        criteriaDTO.setEmail("*cap.d?");
        criteriaDTO.setUserId("user?");
        criteriaDTO.setFirstName("User*");
        criteriaDTO.setLastName("*o*");
        result = given()
                .when()
                .body(criteriaDTO)
                .contentType(APPLICATION_JSON)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfilePageResultDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void updateUserProfileTest() {
        // no content for update
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "not-existing")
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(error.getDetail()).isEqualTo("updateUserProfileData.updateUserPersonRequestDTO: must not be null");

        var userProfileDTO = given()
                .when()
                .pathParam("id", "user1")
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        UpdateUserPersonRequestDTO requestDTO = new UpdateUserPersonRequestDTO();
        requestDTO.setEmail(userProfileDTO.getPerson().getEmail());
        requestDTO.setDisplayName(userProfileDTO.getPerson().getDisplayName());
        requestDTO.setFirstName(userProfileDTO.getPerson().getFirstName());
        requestDTO.setLastName(userProfileDTO.getPerson().getLastName());
        requestDTO.setAddress(userProfileDTO.getPerson().getAddress());
        requestDTO.setPhone(userProfileDTO.getPerson().getPhone());
        requestDTO.getPhone().setNumber("123456789");
        requestDTO.getPhone().setType(PhoneTypeDTO.LANDLINE);
        requestDTO.setModificationCount(userProfileDTO.getModificationCount());

        // update not existing profile
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("id", "not-existing")
                .put("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // update existing profile
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("id", "user1")
                .put("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        // update 2nd time - existing profile - get optimistic lock exception
        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("id", "user1")
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.OPTIMISTIC_LOCK.name());

        userProfileDTO = given()
                .when()
                .pathParam("id", "user1")
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userProfileDTO.getPerson().getPhone().getType()).isEqualTo(requestDTO.getPhone().getType());
        assertThat(userProfileDTO.getPerson().getPhone().getNumber()).isEqualTo(requestDTO.getPhone().getNumber());
    }
}
