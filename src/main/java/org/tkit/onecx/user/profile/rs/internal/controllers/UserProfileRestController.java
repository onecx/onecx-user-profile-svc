package org.tkit.onecx.user.profile.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.user.profile.domain.daos.PreferenceDAO;
import org.tkit.onecx.user.profile.domain.daos.UserProfileDAO;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.domain.service.UserProfileService;
import org.tkit.onecx.user.profile.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.user.profile.rs.internal.mappers.UserProfileMapper;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.user.profile.rs.internal.UserProfileApi;
import gen.org.tkit.onecx.user.profile.rs.internal.model.*;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class UserProfileRestController implements UserProfileApi {

    @Inject
    UserProfileDAO userProfileDAO;

    @Inject
    PreferenceDAO preferenceDAO;

    @Inject
    UserProfileMapper userProfileMapper;

    @Inject
    UserProfileService jwtService;

    @Inject
    InternalExceptionMapper exceptionMapper;

    @Override
    @Transactional
    public Response deleteMyUserProfile() {
        var userId = ApplicationContext.get().getPrincipal();

        var userProfile = userProfileDAO.getUserProfileByUserId(userId, UserProfile.ENTITY_GRAPH_LOAD_ALL);

        if (userProfile != null) {
            var preferences = preferenceDAO.getAllPreferencesByUserId(userId);
            preferenceDAO.delete(preferences);
            userProfileDAO.delete(userProfile);

        }

        return Response.noContent().build();
    }

    @Override
    public Response getMyUserProfile() {
        var userId = ApplicationContext.get().getPrincipal();

        var userProfile = userProfileDAO.getUserProfileByUserId(userId, UserProfile.ENTITY_GRAPH_LOAD_PERSON);

        if (userProfile == null) {
            // create user profile if it does not exist
            var createUserProfile = jwtService.createProfileFromToken();
            userProfile = userProfileDAO.create(createUserProfile);
        }
        //temporary mirroring of account settings - sunset strategy of deprecating old account settings
        if (userProfile.getSettings() == null) {
            userProfile = jwtService.mirrorSettings(userProfile);
            userProfileDAO.update(userProfile);
        }
        return Response.ok(userProfileMapper.mapProfile(userProfile)).build();
    }

    @Override
    public Response updateMyUserProfile(UpdateUserProfileRequestDTO updateUserProfileRequestDTO) {
        var userId = ApplicationContext.get().getPrincipal();
        var userProfile = userProfileDAO.getUserProfileByUserId(userId, UserProfile.ENTITY_GRAPH_LOAD_ALL);
        if (userProfile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        userProfileMapper.updateProfile(userProfile, updateUserProfileRequestDTO);
        var updatedProfile = userProfileDAO.update(userProfile);

        return Response.status(Response.Status.OK).entity(userProfileMapper.map(updatedProfile)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> daoException(OptimisticLockException ex) {
        return exceptionMapper.optimisticLock(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraintViolation(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
