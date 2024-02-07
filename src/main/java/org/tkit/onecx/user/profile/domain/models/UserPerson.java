package org.tkit.onecx.user.profile.domain.models;

import java.io.Serializable;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class UserPerson implements Serializable {

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
