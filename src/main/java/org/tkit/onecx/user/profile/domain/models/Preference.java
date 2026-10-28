package org.tkit.onecx.user.profile.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PREFERENCE", indexes = { @Index(columnList = "USER_ID, TENANT_ID", name = "preferences_user_id_idx") })
@Getter
@Setter
@SuppressWarnings("java:S2160")
public class Preference extends TraceableEntity {

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")),
            @JoinColumnOrFormula(formula = @JoinFormula(value = "TENANT_ID", referencedColumnName = "TENANT_ID"))
    })
    private UserProfile userProfile;

    @Column(name = "APPLICATION_ID")
    private String applicationId;

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    private String name;
    private String description;
    private String value;

}
