package org.tkit.onecx.user.profile.domain.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.tkit.onecx.user.profile.domain.config.UserProfileConfig;
import org.tkit.onecx.user.profile.domain.models.UserPerson;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.domain.models.UserProfileAccountSettings;
import org.tkit.quarkus.context.ApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
public class UserProfileService {

    @Inject
    UserProfileConfig config;

    public UserProfile createProfileFromToken() {
        var userId = ApplicationContext.get().getPrincipal();
        var token = ApplicationContext.get().getPrincipalToken();

        UserProfile userProfile = new UserProfile();
        userProfile.setPerson(new UserPerson());
        userProfile.setIdentityProvider("keycloak");
        userProfile.setUserId(userId);
        userProfile.setOrganization(claim(token, config.claims().organization()));
        userProfile.getPerson().setDisplayName(claim(token, config.claims().displayName()));
        userProfile.getPerson().setFirstName(claim(token, config.claims().firstName()));
        userProfile.getPerson().setLastName(claim(token, config.claims().lastName()));
        userProfile.getPerson().setEmail(claim(token, config.claims().email()));
        userProfile.setAccountSettings(new UserProfileAccountSettings());
        return userProfile;
    }

    String claim(JsonWebToken token, String name) {
        try {
            return token.getClaim(name);
        } catch (Exception ex) {
            log.error("Get claim {} failed. Returning null instead.", name, ex);
            return null;
        }
    }

}
