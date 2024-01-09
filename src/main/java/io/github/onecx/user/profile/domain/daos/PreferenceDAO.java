package io.github.onecx.user.profile.domain.daos;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.JoinType;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

import io.github.onecx.user.profile.domain.models.Preference;
import io.github.onecx.user.profile.domain.models.Preference_;
import io.github.onecx.user.profile.domain.models.UserProfile_;

@ApplicationScoped
public class PreferenceDAO extends AbstractDAO<Preference> {

    @Override
    public Preference findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Preference.class);
            var root = cq.from(Preference.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public List<Preference> getAllPreferencesByUserId(String userId) {

        var cb = this.getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(Preference.class);

        var root = cq.from(Preference.class);
        root.fetch(Preference_.userProfile, JoinType.LEFT);
        cq.where(cb.equal(root.get(Preference_.userProfile).get(UserProfile_.USER_ID), userId));
        var typedQuery = em.createQuery(cq);

        return typedQuery.getResultList();
    }

    enum ErrorKeys {

        FIND_ENTITY_BY_ID_FAILED,
    }
}
