package org.tkit.onecx.user.profile.domain.daos;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.domain.criteria.UserPersonCriteria;
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
}
