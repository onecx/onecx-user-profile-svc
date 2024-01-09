package io.github.onecx.user.profile.domain.models;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class UserPersonPhone {

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private PhoneType type;

    @Column(name = "NUMBER")
    private String number;

    public enum PhoneType {
        MOBILE,
        LANDLINE
    }

}
