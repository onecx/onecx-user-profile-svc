package org.tkit.onecx.user.profile.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.user.profile.domain.daos.PreferenceDAO;
import org.tkit.onecx.user.profile.domain.daos.UserProfileDAO;
import org.tkit.onecx.user.profile.domain.models.UserProfile;
import org.tkit.onecx.user.profile.domain.service.UserProfileService;
import org.tkit.onecx.user.profile.rs.external.v1.mappers.PreferenceV1Mapper;
import org.tkit.onecx.user.profile.rs.external.v1.mappers.UserProfileV1Mapper;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.user.profile.rs.external.v1.UserProfileV1Api;

@LogService
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class UserProfileV1RestController implements UserProfileV1Api {

    @Inject
    PreferenceDAO preferenceDAO;

    @Inject
    UserProfileDAO userProfileDAO;

    @Inject
    PreferenceV1Mapper preferenceMapper;

    @Inject
    UserProfileV1Mapper userProfileV1Mapper;

    @Inject
    UserProfileService userProfileService;

    @Override
    public Response getUserPerson() {
        var userId = ApplicationContext.get().getPrincipal();

        var userProfile = userProfileDAO.getUserProfileByUserId(userId, UserProfile.ENTITY_GRAPH_LOAD_PERSON);

        if (userProfile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var userPerson = userProfileV1Mapper.map(userProfile.getPerson());

        return Response.ok(userPerson).build();
    }

    @Override
    public Response getUserPreference() {
        var userId = ApplicationContext.get().getPrincipal();

        var preferences = preferenceDAO.getAllPreferencesByUserId(userId);

        return Response.ok(preferenceMapper.findV1(preferences)).build();
    }

    @Override
    public Response getUserProfile() {
        var userId = ApplicationContext.get().getPrincipal();
        var userProfile = userProfileDAO.getUserProfileByUserId(userId, UserProfile.ENTITY_GRAPH_LOAD_PERSON);

        if (userProfile == null) {
            // create user profile if it does not exist
            var createUserProfile = userProfileService.createProfileFromToken();
            userProfile = userProfileDAO.create(createUserProfile);
        }
        if (userProfile.getIssuer() == null) {
            System.out.println("MYISSUER: " + ApplicationContext.get().getPrincipalToken().getIssuer());
            userProfile.setIssuer(ApplicationContext.get().getPrincipalToken().getIssuer());
            userProfile = userProfileDAO.update(userProfile);
        }

        return Response.ok(userProfileV1Mapper.map(userProfile)).build();
    }

    @Override
    public Response getUserSettings() {
        var userId = ApplicationContext.get().getPrincipal();

        var userProfile = userProfileDAO.getUserProfileByUserId(userId, UserProfile.ENTITY_GRAPH_LOAD_PERSON);

        if (userProfile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(userProfileV1Mapper.map(userProfile.getAccountSettings())).build();
    }

}
