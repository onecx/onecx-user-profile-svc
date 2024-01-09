package io.github.onecx.user.profile.domain.daos;

import static io.github.onecx.user.profile.domain.models.UserPerson_.*;
import static io.github.onecx.user.profile.domain.models.UserProfile_.PERSON;
import static io.github.onecx.user.profile.domain.models.UserProfile_.USER_ID;

import java.util.ArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;

import io.github.onecx.user.profile.domain.criteria.UserPersonCriteria;
import io.github.onecx.user.profile.domain.models.UserPerson_;
import io.github.onecx.user.profile.domain.models.UserProfile;
import io.github.onecx.user.profile.domain.models.UserProfile_;

@ApplicationScoped
public class UserProfileDAO extends AbstractDAO<UserProfile> {

    public PageResult<UserProfile> findBySearchCriteria(UserPersonCriteria criteria, int pageNumber, int pageSize) {
        var cb = getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(UserProfile.class);
        var root = cq.from(UserProfile.class);
        cq.select(root).distinct(true);
        var predicates = new ArrayList<>();
        if (criteria != null) {
            if (criteria.getUserId() != null) {
                var userIdCriterion = criteria.getUserId().toLowerCase();
                var userIdLowercase = cb.lower(root.get(UserProfile_.userId));
                if (userIdCriterion.contains("*")) {
                    var pattern = preparePatternForLikeQueryWithWildcards(userIdCriterion);
                    predicates.add(cb.like(userIdLowercase, pattern));
                } else {
                    predicates.add(cb.equal(userIdLowercase, userIdCriterion));
                }
            }
            if (criteria.getEmail() != null) {
                var emailCriterion = criteria.getEmail().toLowerCase();
                var emailLowercase = cb.lower(root.get(PERSON).get(UserPerson_.EMAIL));
                if (emailCriterion.contains("*")) {
                    var pattern = preparePatternForLikeQueryWithWildcards(emailCriterion);
                    predicates.add(cb.like(emailLowercase, pattern));
                } else {
                    predicates.add(cb.equal(emailLowercase, emailCriterion));
                }
            }
            if (criteria.getFirstName() != null) {
                var firstNameCriterion = criteria.getFirstName().toLowerCase();
                var firstNameLowercase = cb.lower(root.get(PERSON).get(FIRST_NAME));
                if (firstNameCriterion.contains("*")) {
                    var pattern = preparePatternForLikeQueryWithWildcards(firstNameCriterion);
                    predicates.add(cb.like(firstNameLowercase, pattern));
                } else {
                    predicates.add(cb.equal(firstNameLowercase, firstNameCriterion));
                }
            }
            if (criteria.getLastName() != null) {
                var lastNameCriterion = criteria.getLastName().toLowerCase();
                var lastNameLowercase = cb.lower(root.get(PERSON).get(LAST_NAME));
                if (lastNameCriterion.contains("*")) {
                    var pattern = preparePatternForLikeQueryWithWildcards(lastNameCriterion);
                    predicates.add(cb.like(lastNameLowercase, pattern));
                } else {
                    predicates.add(cb.equal(lastNameLowercase, lastNameCriterion));
                }
            }
        }
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return createPageQuery(cq, Page.of(pageNumber, pageSize)).getPageResult();
    }

    @Transactional
    public UserProfile getUserProfileByUserId(String userId, String loadGraphType) {

        var cb = getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(UserProfile.class);
        var root = cq.from(UserProfile.class);
        cq.where(cb.equal(root.get(USER_ID), userId));
        var typedQuery = em.createQuery(cq);

        if (!loadGraphType.isEmpty()) {
            typedQuery.setHint("javax.persistence.loadgraph",
                    this.em.getEntityGraph(UserProfile.class.getSimpleName() + loadGraphType));
        }

        return typedQuery.getResultList().stream().findFirst().orElse(null);
    }

    private static String preparePatternForLikeQueryWithWildcards(String criterionValue) {

        return criterionValue
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("*", "%");
    }
}
