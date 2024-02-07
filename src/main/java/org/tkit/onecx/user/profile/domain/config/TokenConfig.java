package org.tkit.onecx.user.profile.domain.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "onecx.identity.token")
public interface TokenConfig {

    @WithName("header.name")
    @WithDefault("apm-principal-token")
    String headerName();

    @WithName("claim.display.name")
    @WithDefault("name")
    String displayName();

    @WithName("claim.email")
    @WithDefault("email")
    String email();

    @WithName("claim.first.name")
    @WithDefault("given_name")
    String firstName();

    @WithName("claim.last.name")
    @WithDefault("family_name")
    String lastName();
}
