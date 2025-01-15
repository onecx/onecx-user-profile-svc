package org.tkit.onecx.user.profile.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.user.profile.domain.config.UserProfileConfig;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.user.profile.rs.external.v1.model.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
@TestHTTPEndpoint(UserProfileV1RestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-up:read" })
class UserProfileV1RestControllerMenuSettingsTest extends AbstractTest {

    @InjectMock
    UserProfileConfig userProfileConfig;

    @Inject
    Config config;

    @BeforeEach
    void beforeEach() {
        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(UserProfileConfig.class);

        var m = Mockito.mock(UserProfileConfig.Settings.class);
        Mockito.when(m.locale()).thenReturn(tmp.settings().locale());
        Mockito.when(m.timeZone()).thenReturn(tmp.settings().timeZone());
        Mockito.when(m.menuMode()).thenReturn("WRONG_ENUM_KEY");

        Mockito.when(userProfileConfig.claims()).thenReturn(tmp.claims());
        Mockito.when(userProfileConfig.settings()).thenReturn(m);

    }

    @Test
    void getUserProfileTest() {
        // not existing user profile, should be created
        var userProfile = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("not-existing", null))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(UserProfileDTO.class);

        assertThat(userProfile.getUserId()).isEqualTo("not-existing");
        assertThat(userProfile.getOrganization()).isEqualTo("org1");
        assertThat(userProfile.getPerson().getEmail()).isEqualTo("not-existing@testOrg.de");
        assertThat(userProfile.getAccountSettings().getMenuMode()).isEqualTo(MenuModeDTO.STATIC);
    }

}
