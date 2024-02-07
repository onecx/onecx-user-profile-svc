package org.tkit.onecx.user.profile.test;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static io.restassured.RestAssured.config;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;

import java.security.PrivateKey;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.Claims;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.restassured.config.RestAssuredConfig;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.util.KeyUtils;

@SuppressWarnings("java:S2187")
public class AbstractTest {

    protected static final String APM_HEADER_PARAM = "apm-principal-token";
    protected static final String CLAIMS_ORG_ID = ConfigProvider.getConfig()
            .getValue("%test.tkit.rs.context.tenant-id.mock.claim-org-id", String.class);;

    static {
        config = RestAssuredConfig.config().objectMapperConfig(
                objectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> {
                            var objectMapper = new ObjectMapper();
                            objectMapper.registerModule(new JavaTimeModule());
                            objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
                            return objectMapper;
                        }));
    }

    protected static String createToken(String userId, String orgId) {
        try {
            String userName = userId != null ? userId : "test-user";
            String organizationId = orgId != null ? orgId : "org1";
            JsonObjectBuilder claims = createClaims(userName, organizationId);

            PrivateKey privateKey = KeyUtils.generateKeyPair(2048).getPrivate();
            return Jwt.claims(claims.build()).sign(privateKey);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected static JsonObjectBuilder createClaims(String userName, String orgId) {
        JsonObjectBuilder claims = Json.createObjectBuilder();
        claims.add(Claims.preferred_username.name(), userName);
        claims.add(Claims.sub.name(), userName);
        claims.add(CLAIMS_ORG_ID, orgId);
        claims.add("name", "Given Family " + userName);
        claims.add(Claims.given_name.name(), "Given " + userName);
        claims.add(Claims.family_name.name(), "Family " + userName);
        claims.add(Claims.email.name(), userName + "@cap.de");

        return claims;
    }
}
