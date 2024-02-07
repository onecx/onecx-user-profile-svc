package org.tkit.onecx.user.profile.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.user.profile.domain.daos.UserProfileDAO;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileAdminRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class UserProfileAdminRestControllerExceptionTest extends AbstractTest {

    @InjectMock
    UserProfileDAO userProfileDAO;

    @Test
    void testDAOExceptionThrow() {
        Mockito.when(userProfileDAO.getUserProfileByUserId("user1", UserProfile.ENTITY_GRAPH_LOAD_PERSON))
                .thenThrow(new DAOException(TestEnumError.TEST_ERROR, null));
        given()
                .when()
                .pathParam("id", "user1")
                .get("{id}")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());
    }

    enum TestEnumError {
        TEST_ERROR
    }

}
