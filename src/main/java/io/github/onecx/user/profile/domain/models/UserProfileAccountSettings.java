package io.github.onecx.user.profile.domain.models;

import jakarta.persistence.*;

import io.github.onecx.user.profile.domain.models.enums.ColorScheme;
import io.github.onecx.user.profile.domain.models.enums.MenuMode;
import lombok.Getter;
import lombok.Setter;

/**
 * User profile account settings entity
 */
@Embeddable
@Getter
@Setter
public class UserProfileAccountSettings {

    @Column(name = "HIDE_MY_PROFILE")
    private Boolean hideMyProfile;

    @Column(name = "LOCALE")
    private String locale;
    @Column(name = "TIMEZONE")
    private String timezone;

    @Column(name = "MENU_MODE")
    @Enumerated(EnumType.STRING)
    private MenuMode menuMode = MenuMode.STATIC;

    @Column(name = "COLOR_SCHEME")
    @Enumerated(EnumType.STRING)
    private ColorScheme colorScheme = ColorScheme.AUTO;

}
