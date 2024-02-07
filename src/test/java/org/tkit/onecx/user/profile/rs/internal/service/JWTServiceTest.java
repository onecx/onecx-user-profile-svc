package org.tkit.onecx.user.profile.rs.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;

import org.eclipse.microprofile.jwt.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.util.KeyUtils;

@QuarkusTest
class JWTServiceTest extends AbstractTest {

    @Inject
    JWTService service;

    @InjectMock
    HttpHeaders headers;

    @Test
    void testMissingToken() throws NoSuchAlgorithmException {
        // test for null token
        Mockito.when(headers.getHeaderString("apm-principal-token")).thenReturn(null);
        methodExceptionTests(() -> service.createProfileFromToken("user1"), JWTService.ERROR_KEYS.APM_MISSING);
        // test for empty token
        Mockito.when(headers.getHeaderString("apm-principal-token")).thenReturn("");
        methodExceptionTests(() -> service.createProfileFromToken("user1"), JWTService.ERROR_KEYS.APM_MISSING);

        Mockito.when(headers.getHeaderString("apm-principal-token")).thenReturn("wrongTokenFormat");
        methodExceptionTests(() -> service.createProfileFromToken("user1"), JWTService.ERROR_KEYS.TOKEN_SERIALIZATION_FAILURE);

        var claims = createClaims("user1", "org1");
        claims.remove(Claims.given_name.name());
        claims.add(Claims.given_name.name(), 15);
        PrivateKey privateKey = KeyUtils.generateKeyPair(2048).getPrivate();
        var token = Jwt.claims(claims.build()).sign(privateKey);
        Mockito.when(headers.getHeaderString("apm-principal-token")).thenReturn(token);
        var response = service.createProfileFromToken("user1");
        assertThat(response).isNotNull();
        assertThat(response.getPerson().getFirstName()).isNull();
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(ConstraintException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }
}
