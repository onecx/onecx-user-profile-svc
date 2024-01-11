package io.github.onecx.user.profile.domain.models;

import java.io.Serializable;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

/**
 * Address entity
 *
 * @author bkalas
 */
@Getter
@Setter
@Embeddable
public class UserPersonAddress implements Serializable {
    @Column(name = "STREET")
    private String street;

    @Column(name = "STREET_NO")
    private String streetNo;

    @Column(name = "CITY")
    private String city;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @Column(name = "COUNTRY")
    private String country;

}
