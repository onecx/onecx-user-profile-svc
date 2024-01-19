package io.github.onecx.user.profile.rs.internal.controllers;

import static io.github.onecx.user.profile.domain.models.UserProfile.ENTITY_GRAPH_LOAD_ALL;
import static io.github.onecx.user.profile.domain.models.UserProfile.ENTITY_GRAPH_LOAD_PERSON;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.user.profile.rs.internal.UserProfileAdminApi;
import gen.io.github.onecx.user.profile.rs.internal.model.CreateUserProfileRequestDTO;
import gen.io.github.onecx.user.profile.rs.internal.model.ProblemDetailResponseDTO;
import gen.io.github.onecx.user.profile.rs.internal.model.UpdateUserPersonRequestDTO;
import gen.io.github.onecx.user.profile.rs.internal.model.UserPersonCriteriaDTO;
import io.github.onecx.user.profile.domain.daos.PreferenceDAO;
import io.github.onecx.user.profile.domain.daos.UserProfileDAO;
import io.github.onecx.user.profile.rs.internal.mappers.InternalExceptionMapper;
import io.github.onecx.user.profile.rs.internal.mappers.UserProfileMapper;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class UserProfileAdminRestController implements UserProfileAdminApi {

    @Inject
    UserProfileDAO userProfileDAO;

    @Inject
    PreferenceDAO preferenceDAO;

    @Inject
    UserProfileMapper userProfileMapper;

    @Inject
    InternalExceptionMapper exceptionMapper;

    @Context
    UriInfo uriInfo;

    @Override
    public Response createUserProfile(CreateUserProfileRequestDTO createUserProfileRequestDTO) {
        var userProfile = userProfileMapper.create(createUserProfileRequestDTO);
        userProfile = userProfileDAO.create(userProfile);

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(userProfile.getId()).build())
                .entity(userProfileMapper.map(userProfile))
                .build();
    }

    @Override
    @Transactional
    public Response deleteUserProfile(String id) {
        var userProfile = userProfileDAO.getUserProfileByUserId(id, ENTITY_GRAPH_LOAD_PERSON);
        if (userProfile != null) {
            preferenceDAO.delete(preferenceDAO.getAllPreferencesByUserId(id));
            userProfileDAO.delete(userProfile);
        }

        return Response.noContent().build();
    }

    @Override
    public Response getUserProfile(String id) {
        var userProfile = userProfileDAO.getUserProfileByUserId(id, ENTITY_GRAPH_LOAD_PERSON);
        if (userProfile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(userProfileMapper.map(userProfile)).build();
    }

    @Override
    public Response searchUserProfile(UserPersonCriteriaDTO userPersonCriteriaDTO) {
        var criteria = userProfileMapper.map(userPersonCriteriaDTO);

        var result = userProfileDAO.findBySearchCriteria(criteria, userPersonCriteriaDTO.getPageNumber(),
                userPersonCriteriaDTO.getPageSize());

        return Response.ok(userProfileMapper.mapPageResult(result)).build();
    }

    @Override
    public Response updateUserProfile(String id, UpdateUserPersonRequestDTO updateUserPersonRequestDTO) {
        var userProfile = userProfileDAO.getUserProfileByUserId(id, ENTITY_GRAPH_LOAD_ALL);

        if (userProfile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        userProfileMapper.updateUserPerson(userProfile, updateUserPersonRequestDTO);
        userProfileDAO.update(userProfile);

        return Response.noContent().build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> optimisticLock(DAOException ex) {
        if (ex.getCause() instanceof OptimisticLockException oex) {
            return exceptionMapper.optimisticLock(oex);
        }
        throw ex;
    }
}
