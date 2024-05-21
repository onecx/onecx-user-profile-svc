package org.tkit.onecx.user.profile.domain.config;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * User profile configuration
 */
@ConfigDocFilename("onecx-user-profile-svc.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.user-profile")
public interface UserProfileConfig {

    /**
     * User profile principal token claims
     */
    @WithName("claims")
    Claims claims();

    /**
     * User profile settings on initial creation
     */
    @WithName("settings")
    Settings settings();

    interface Claims {

        /**
         * User profile display name token claim
         */
        @WithName("display-name")
        @WithDefault("name")
        String displayName();

        /**
         * User profile email token claim
         */
        @WithName("email")
        @WithDefault("email")
        String email();

        /**
         * User profile first name token claim
         */
        @WithName("first-name")
        @WithDefault("given_name")
        String firstName();

        /**
         * User profile last name token claim
         */
        @WithName("last-name")
        @WithDefault("family_name")
        String lastName();

        /**
         * User profile organization token claim
         */
        @WithName("organization-id")
        @WithDefault("orgId")
        String organization();
    }

    interface Settings {

        /**
         * User profile locale
         */
        @WithName("locale")
        @WithDefault("en")
        String locale();

        /**
         * User profile timezone
         */
        @WithName("timezone")
        @WithDefault("Europe/Berlin")
        String timeZone();
    }
}
