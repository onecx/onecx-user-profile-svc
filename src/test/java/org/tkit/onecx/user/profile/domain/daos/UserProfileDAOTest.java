package org.tkit.onecx.user.profile.domain.daos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.domain.criteria.UserPersonCriteria;
import org.tkit.onecx.user.profile.domain.criteria.UserProfileAbstractCriteria;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.domain.models.UserProfile_;
import org.tkit.onecx.user.profile.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class UserProfileDAOTest extends AbstractTest {

    @Inject
    UserProfileDAO dao;

    @Inject
    EntityManager em;

    @Test
    void testWithoutEntityGraph() {
        var profile = dao.getUserProfileByUserId("user1", "");
        assertThat(profile).isNotNull();

        UserPersonCriteria criteria = new UserPersonCriteria();
        var resultList = dao.findBySearchCriteria(criteria, 0, 10);
        assertThat(resultList).isNotNull();
        assertThat(resultList.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testCaseSensitive() {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(UserProfile.class);
        var root = cq.from(UserProfile.class);
        var predicate = dao.createSearchStringPredicate(cb, root.get(UserProfile_.userId), "dummyUser", false);
        assertThat(predicate).isNotNull();
    }

    @Test
    void testCreateInStringListPredicateCaseInsensitive() {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(UserProfile.class);
        var root = cq.from(UserProfile.class);

        // Test case insensitive (default) - should match regardless of case
        List<String> criteriaList = List.of("USER1", "USER2");
        var predicate = dao.createInStringListPredicate(cb, root.get(UserProfile_.userId), criteriaList);

        assertThat(predicate).isNotNull();

        // Execute the query to verify it works case-insensitively
        cq.where(predicate);
        var result = em.createQuery(cq).getResultList();

        // Should find user1 and user2 despite uppercase search criteria
        assertThat(result).isNotEmpty().hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void testCreateInStringListPredicateCaseSensitive() {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(UserProfile.class);
        var root = cq.from(UserProfile.class);

        // Test case sensitive - exact match required
        List<String> criteriaList = List.of("USER1", "USER2");
        var predicate = dao.createInStringListPredicate(cb, root.get(UserProfile_.userId), criteriaList, false);

        assertThat(predicate).isNotNull();

        // Execute the query to verify it works case-sensitively
        cq.where(predicate);
        var result = em.createQuery(cq).getResultList();

        // Should find no users because usernames are lowercase
        assertThat(result).isEmpty();
    }

    @Test
    void testFindProfileAbstractByCriteriaWithUserIds() {
        var criteria = UserProfileAbstractCriteria.builder()
                .userIds(List.of("user1"))
                .build();

        var result = dao.findProfileAbstractByCriteria(criteria, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void testFindProfileAbstractByCriteriaWithEmailAddresses() {
        var criteria = UserProfileAbstractCriteria.builder()
                .emailAddresses(List.of("user1@testOrg.de"))
                .build();

        var result = dao.findProfileAbstractByCriteria(criteria, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void testFindProfileAbstractByCriteriaWithDisplayNames() {
        var criteria = UserProfileAbstractCriteria.builder()
                .displayNames(List.of("User One"))
                .build();

        var result = dao.findProfileAbstractByCriteria(criteria, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void testFindProfileAbstractByCriteriaWithEmptyLists() {
        var criteria = UserProfileAbstractCriteria.builder()
                .userIds(new ArrayList<>())
                .emailAddresses(new ArrayList<>())
                .displayNames(new ArrayList<>())
                .build();

        var result = dao.findProfileAbstractByCriteria(criteria, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    @Test
    void testFindProfileAbstractByCriteriaWithNullLists() {
        var criteria = UserProfileAbstractCriteria.builder()
                .userIds(null)
                .emailAddresses(null)
                .displayNames(null)
                .build();

        var result = dao.findProfileAbstractByCriteria(criteria, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    @Test
    void testFindProfileAbstractByCriteriaWithMultipleCriteria() {
        var criteria = UserProfileAbstractCriteria.builder()
                .userIds(List.of("user1"))
                .emailAddresses(List.of("user2@testOrg.de"))
                .build();

        var result = dao.findProfileAbstractByCriteria(criteria, 0, 10);

        assertThat(result).isNotNull();
        // Should use OR logic, so both user1 and user with email user2@testOrg.de should be returned
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }
}
