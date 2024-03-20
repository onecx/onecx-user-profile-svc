package org.tkit.onecx.user.profile.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "IMAGE", uniqueConstraints = {
        @UniqueConstraint(name = "IMAGE_CONSTRAINTS", columnNames = { "USER_ID", "REF_TYPE", "TENANT_ID" })
})
@SuppressWarnings("java:S2160")
public class Image extends TraceableEntity {

    @Column(name = "MIME_TYPE")
    private String mimeType;

    @Column(name = "REF_TYPE")
    private String refType;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "DATA_LENGTH")
    private Integer length;

    @Column(name = "DATA")
    private byte[] imageData;

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

}
