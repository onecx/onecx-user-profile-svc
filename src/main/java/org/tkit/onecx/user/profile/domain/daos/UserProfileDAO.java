package org.tkit.onecx.user.profile.domain.daos;

import static org.tkit.onecx.user.profile.domain.models.UserProfile_.PERSON;
import static org.tkit.onecx.user.profile.domain.models.UserProfile_.USER_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.onecx.user.profile.domain.criteria.UserPersonCriteria;
import org.tkit.onecx.user.profile.domain.criteria.UserProfileAbstractCriteria;
import org.tkit.onecx.user.profile.domain.models.UserPerson_;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.domain.models.UserProfile_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;
import org.tkit.quarkus.jpa.utils.QueryCriteriaUtil;

@ApplicationScoped
public class UserProfileDAO extends AbstractDAO<UserProfile> {

    public PageResult<UserProfile> findBySearchCriteria(UserPersonCriteria criteria, int pageNumber, int pageSize) {
        var cb = getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(UserProfile.class);
        var root = cq.from(UserProfile.class);
        cq.select(root).distinct(true);
        var predicates = new ArrayList<>();
        if (criteria.getUserId() != null) {
            predicates.add(createSearchStringPredicate(cb, root.get(UserProfile_.userId), criteria.getUserId()));
        }
        if (criteria.getEmail() != null) {
            predicates.add(createSearchStringPredicate(cb, root.get(PERSON).get(UserPerson_.EMAIL), criteria.getEmail()));
        }
        if (criteria.getFirstName() != null) {
            predicates.add(createSearchStringPredicate(cb, root.get(PERSON).get(UserPerson_.FIRST_NAME),
                    criteria.getFirstName()));
        }
        if (criteria.getLastName() != null) {
            predicates.add(
                    createSearchStringPredicate(cb, root.get(PERSON).get(UserPerson_.LAST_NAME), criteria.getLastName()));
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));

        return createPageQuery(cq, Page.of(pageNumber, pageSize)).getPageResult();
    }

    public PageResult<UserProfile> findProfileAbstractByCriteria(final UserProfileAbstractCriteria criteria) {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(UserProfile.class);
            var root = cq.from(UserProfile.class);
            cq.select(root).distinct(true);
            var predicates = new ArrayList<>();
            if (isCriteriaListValid(criteria.getUserIds())) {
                predicates.add(
                        createInStringListPredicate(cb, root.get(UserProfile_.userId), criteria.getUserIds(), true));
            }
            if (isCriteriaListValid(criteria.getEmailAddresses())) {
                predicates.add(
                        createInStringListPredicate(cb, root.get(PERSON).get(UserPerson_.EMAIL), criteria.getEmailAddresses(),
                                true));
            }
            if (isCriteriaListValid(criteria.getDisplayNames())) {
                predicates.add(
                        createInStringListPredicate(cb, root.get(PERSON).get(UserPerson_.DISPLAY_NAME),
                                criteria.getDisplayNames(), false));
            }
            if (!predicates.isEmpty()) {
                cq.where(cb.or(predicates.toArray(new Predicate[0])));
            }
            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));
            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (RuntimeException e) {
            throw new DAOException(ErrorKeys.SEARCH_PROFILE_ABSTRACT_FAILED, e);
        }

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

    @Transactional
    public UserProfile getUserProfileById(String id, String loadGraphType) {

        var cb = getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(UserProfile.class);
        var root = cq.from(UserProfile.class);
        cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
        var typedQuery = em.createQuery(cq);

        typedQuery.setHint("javax.persistence.loadgraph",
                this.em.getEntityGraph(UserProfile.class.getSimpleName() + loadGraphType));

        return typedQuery.getResultList().stream().findFirst().orElse(null);
    }

    /**
     * Create a search predicate as a case of insensitive search.
     *
     * @param criteriaBuilder - CriteriaBuilder
     * @param column - column Path [root.get(Entity_.attribute)]
     * @param searchString - string to search. if Contains [*,?] like will be used
     * @return LIKE or EQUAL Predicate according to the search string
     */
    public Predicate createSearchStringPredicate(CriteriaBuilder criteriaBuilder, Expression<String> column,
            String searchString) {
        return createSearchStringPredicate(criteriaBuilder, column, searchString, true);
    }

    /**
     * Create a search predicate.
     *
     * @param criteriaBuilder - CriteriaBuilder
     * @param column - column Path [root.get(Entity_.attribute)]
     * @param searchString - string to search. if Contains [*,?] like will be used
     * @param caseInsensitive - true in case of insensitive search (db column and search string are given to lower case)
     * @return LIKE or EQUAL Predicate according to the search string
     */
    public Predicate createSearchStringPredicate(CriteriaBuilder criteriaBuilder, Expression<String> column,
            String searchString, final boolean caseInsensitive) {

        Expression<String> columnDefinition = column;
        if (caseInsensitive) {
            searchString = searchString.toLowerCase();
            columnDefinition = criteriaBuilder.lower(column);
        }

        Predicate searchPredicate = null;
        if (searchString.contains("*") || searchString.contains("?")) {
            searchPredicate = criteriaBuilder.like(columnDefinition, QueryCriteriaUtil.wildcard(searchString, caseInsensitive));
        } else {
            searchPredicate = criteriaBuilder.equal(columnDefinition, searchString);
        }

        return searchPredicate;
    }

    /**
     * Create an IN predicate for a list of string values.
     *
     * @param criteriaBuilder - CriteriaBuilder
     * @param column - column Path [root.get(Entity_.attribute)]
     * @param criteriaList - list of strings to match against
     * @param caseInsensitive - true for case-insensitive search (db column and criteria values are converted to lower case)
     * @return IN Predicate for the given list of values
     */
    public Predicate createInStringListPredicate(CriteriaBuilder criteriaBuilder, Expression<String> column,
            List<String> criteriaList, boolean caseInsensitive) {
        var criteriaValues = criteriaList;
        if (caseInsensitive) {
            criteriaValues = criteriaValues.stream().map(String::toLowerCase).toList();
            return criteriaBuilder.lower(column).in(criteriaValues);
        }
        return column.in(criteriaValues);
    }

    private boolean isCriteriaListValid(final List<String> list) {
        return Objects.nonNull(list) && !list.isEmpty();
    }

    enum ErrorKeys {
        SEARCH_PROFILE_ABSTRACT_FAILED
    }
}
