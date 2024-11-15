package org.tkit.onecx.user.profile.domain.service;

import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.tkit.onecx.user.profile.domain.config.UserProfileConfig;
import org.tkit.onecx.user.profile.domain.models.UserPerson;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.domain.models.UserProfileAccountSettings;
import org.tkit.onecx.user.profile.domain.models.enums.MenuMode;
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
        userProfile.getAccountSettings()
                .setTimezone(getClaimOrConfig(token, config.claims().timeZone(), config.settings().timeZone()));
        userProfile.getAccountSettings()
                .setLocale(getClaimOrConfig(token, config.claims().locale(), config.settings().locale()));
        userProfile.getAccountSettings()
                .setMenuMode(getClaimOrConfigMenuMode(token, config.claims().menuMode(), config.settings().menuMode()));
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

    String getClaimOrConfig(JsonWebToken token, Optional<String> claim, String config) {
        if (claim.isEmpty()) {
            return config;
        }
        var temp = claim(token, claim.get());
        if (temp != null && !temp.isBlank()) {
            return temp;
        }
        return config;
    }

    MenuMode getClaimOrConfigMenuMode(JsonWebToken token, Optional<String> claim, String config) {
        String tmp = getClaimOrConfig(token, claim, config);
        try {
            return MenuMode.valueOf(tmp);
        } catch (Exception ex) {
            log.error("Wrong value of the menu mode for the user. Menu mode {}. Returning {} instead.", tmp, MenuMode.STATIC,
                    ex);
            return MenuMode.STATIC;
        }
    }

}
