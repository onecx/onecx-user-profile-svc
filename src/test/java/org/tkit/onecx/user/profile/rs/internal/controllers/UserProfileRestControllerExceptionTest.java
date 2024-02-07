package org.tkit.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.user.profile.domain.daos.UserProfileDAO;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.user.profile.rs.internal.model.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class UserProfileRestControllerExceptionTest extends AbstractTest {

    @InjectMock
    UserProfileDAO userProfileDAO;

    @Test
    void testDAOExceptionThrow() {
        Mockito.when(userProfileDAO.getUserProfileByUserId("user1", UserProfile.ENTITY_GRAPH_LOAD_PERSON))
                .thenThrow(new DAOException(TestEnumError.TEST_ERROR, null));
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .get()
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());
    }

    enum TestEnumError {
        TEST_ERROR
    }

}
