package io.github.onecx.user.profile.rs.internal.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

import io.github.onecx.user.profile.domain.config.TokenConfig;
import io.github.onecx.user.profile.domain.models.UserPerson;
import io.github.onecx.user.profile.domain.models.UserProfile;
import io.github.onecx.user.profile.domain.models.UserProfileAccountSettings;
import io.smallrye.jwt.auth.principal.JWTParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
public class JWTService {

    @Inject
    JWTParser parser;

    @Context
    HttpHeaders headers;

    @Inject
    TokenConfig config;

    public UserProfile createProfileFromToken(String userId) {
        var apmPrincipalToken = headers.getHeaderString(config.headerName());
        if (apmPrincipalToken == null || apmPrincipalToken.isBlank()) {
            log.error("Missing APM principal token: " + config.headerName());
            throw new ConstraintException("APM principal token cannot be null", ERROR_KEYS.APM_MISSING, null);
        }

        try {
            return createProfileFromToken(userId, apmPrincipalToken);
        } catch (Exception e) {
            throw new ConstraintException("Failed to create profile from token", ERROR_KEYS.TOKEN_SERIALIZATION_FAILURE, e);
        }
    }

    private UserProfile createProfileFromToken(String userId, String token) throws JoseException, InvalidJwtException {
        var jws = (JsonWebSignature) JsonWebStructure.fromCompactSerialization(token);
        var jwtClaims = JwtClaims.parse(jws.getUnverifiedPayload());

        UserProfile userProfile = new UserProfile();
        userProfile.setPerson(new UserPerson());
        userProfile.setIdentityProvider("keycloak");
        userProfile.setUserId(userId);
        userProfile.getPerson().setDisplayName(getClaim(jwtClaims, config.displayName()));
        userProfile.getPerson().setFirstName(getClaim(jwtClaims, config.firstName()));
        userProfile.getPerson().setLastName(getClaim(jwtClaims, config.lastName()));
        userProfile.getPerson().setEmail(getClaim(jwtClaims, config.email()));
        userProfile.setAccountSettings(new UserProfileAccountSettings());
        return userProfile;
    }

    private String getClaim(JwtClaims jwtClaims, String name) {
        try {
            return jwtClaims.getClaimValue(name, String.class);
        } catch (MalformedClaimException e) {
            log.error("Get claim " + name + " failed. Returning null instead.", e);
            return null;
        }
    }

    enum ERROR_KEYS {
        APM_MISSING,
        TOKEN_SERIALIZATION_FAILURE
    }

}
