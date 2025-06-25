package org.tkit.onecx.user.profile.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "USER_PROFILE", uniqueConstraints = {
        @UniqueConstraint(name = "UP_CONSTRAINTS", columnNames = { "USER_ID", "TENANT_ID" })
}, indexes = {
        @Index(columnList = "FIRST_NAME,LAST_NAME,EMAIL, TENANT_ID", name = "user_person_criteria_idx") })
@NamedEntityGraph(name = "UserProfile.loadById", attributeNodes = {
        @NamedAttributeNode(value = "person")
})
@NamedEntityGraph(name = "UserProfile.loadAll", attributeNodes = {
        @NamedAttributeNode(value = "person"),
})
@NamedEntityGraph(name = "UserProfile.loadPerson", attributeNodes = {
        @NamedAttributeNode(value = "person")
})
@Getter
@Setter
@SuppressWarnings("java:S2160")
public class UserProfile extends TraceableEntity {

    /**
     * The entity graph name for the load with person fetch
     */
    public static final String ENTITY_GRAPH_LOAD_PERSON = ".loadPerson";
    public static final String ENTITY_GRAPH_LOAD_ALL = ".loadAll";

    @Column(name = "USER_ID")
    private String userId;

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "IDENTITY_PROVIDER")
    private String identityProvider;

    @Column(name = "IDENTITY_PROVIDER_ID")
    private String identityProviderId;

    @Column(name = "ISSUER")
    private String issuer;

    @Column(name = "ORGANIZATION")
    private String organization;

    @Embedded
    private UserPerson person;

    @Embedded
    private UserProfileAccountSettings accountSettings;

    @Column(name = "SETTINGS", columnDefinition = "varchar(5000)")
    private String settings;

}
