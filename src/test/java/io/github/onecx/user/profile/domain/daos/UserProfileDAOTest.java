package io.github.onecx.user.profile.domain.daos;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import io.github.onecx.user.profile.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class UserProfileDAOTest extends AbstractTest {

    @Inject
    UserProfileDAO dao;

    @Test
    void testWithoutEntityGraph() {
        var profile = dao.getUserProfileByUserId("user1", "");
        assertThat(profile).isNotNull();

        var resultList = dao.findBySearchCriteria(null, 0, 10);
        assertThat(resultList).isNotNull();
        assertThat(resultList.getTotalElements()).isEqualTo(2);
    }
}
