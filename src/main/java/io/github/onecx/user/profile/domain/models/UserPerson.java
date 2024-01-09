package io.github.onecx.user.profile.domain.models;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

/**
 * Person data embedable entity
 *
 * @author bkalas
 */
@Embeddable
@Getter
@Setter
public class UserPerson {

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "EMAIL")
    private String email;

    @Embedded
    private UserPersonPhone phone;

    @Embedded
    private UserPersonAddress address;
}
