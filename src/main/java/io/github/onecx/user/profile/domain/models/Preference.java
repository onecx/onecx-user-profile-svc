package io.github.onecx.user.profile.domain.models;

import jakarta.persistence.*;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * User preference value
 *
 * @author bkalas
 */
@Entity
@Table(name = "USM_PREFERENCE", indexes = { @Index(columnList = "USER_ID", name = "preferences_user_id_idx") })
@Getter
@Setter
public class Preference extends TraceableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    private UserProfile userProfile;

    @Column(name = "APPLICATION_ID")
    private String applicationId;

    @Column(name = "TENANT_ID")
    private String tenantId;

    private String name;
    private String description;
    private String value;

}
