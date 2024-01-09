package io.github.onecx.user.profile.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * User profile entity
 *
 * @author bkalas
 */
@Entity
@Table(name = "USM_USER_PROFILE", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "USER_ID" })
}, indexes = {
        @Index(columnList = "FIRST_NAME,LAST_NAME,EMAIL, TENANT_ID", name = "user_person_criteria_idx") })
@NamedEntityGraph(name = "UserProfile.loadById", attributeNodes = {
        @NamedAttributeNode(value = "person")
})
@NamedEntityGraph(name = "UserProfile.loadAll", attributeNodes = {
        @NamedAttributeNode(value = "person"),
        @NamedAttributeNode(value = "avatar"),
        @NamedAttributeNode(value = "smallAvatar")
})
@NamedEntityGraph(name = "UserProfile.loadPerson", attributeNodes = {
        @NamedAttributeNode(value = "person")
})
@Getter
@Setter
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

    @Column(name = "ORGANIZATION")
    private String organization;

    @Embedded
    private UserPerson person;

    @Embedded
    private UserProfileAccountSettings accountSettings;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "AVATAR_GUID")
    private Image avatar;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "SMALL_AVATAR_GUID")
    private Image smallAvatar;

}
