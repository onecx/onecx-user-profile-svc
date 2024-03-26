package org.tkit.onecx.user.profile.domain.models;

import java.io.Serializable;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class UserPersonPhone implements Serializable {

    @Column(name = "PHONE_TYPE")
    @Enumerated(EnumType.STRING)
    private PhoneType type;

    @Column(name = "PHONE_NUMBER")
    private String number;

    public enum PhoneType {
        MOBILE,
        LANDLINE
    }

}
