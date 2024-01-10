package io.github.onecx.user.profile.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Table
@Entity
@Getter
@Setter
public class Image extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "HEIGHT")
    private Integer height;

    @Column(name = "WIDTH")
    private Integer width;

    @Column(name = "MIME_TYPE")
    private String mimeType;

    @Lob
    @Column(name = "IMAGE")
    private byte[] imageByte;

}
