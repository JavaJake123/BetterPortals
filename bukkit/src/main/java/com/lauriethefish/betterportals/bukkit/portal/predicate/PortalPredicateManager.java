package com.lauriethefish.betterportals.bukkit.portal.predicate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lauriethefish.betterportals.api.PortalPredicate;
import com.lauriethefish.betterportals.bukkit.player.IPlayerDataManager;
import com.lauriethefish.betterportals.bukkit.portal.IPortal;
import com.lauriethefish.betterportals.shared.logging.Logger;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class PortalPredicateManager implements IPortalPredicateManager  {
    private final Logger logger;

    // The predicates that filter out the most portals should go first for efficiency
    private final List<PortalPredicate> activationPredicates = new ArrayList<>();
    private final List<PortalPredicate> viewPredicates = new ArrayList<>();
    private final List<PortalPredicate> teleportationPredicates = new ArrayList<>();

    // Add the default predicates for activation distance and view permissions
    @Inject
    public PortalPredicateManager(Logger logger,  IPlayerDataManager playerDataManager, ActivationDistance activationDistance, CrossServerDestinationChecker crossServerDestinationChecker) {
        this.logger = logger;

        addActivationPredicate(activationDistance);
        addActivationPredicate(crossServerDestinationChecker);
        addViewPredicate(new PermissionsChecker("betterportals.see"));
        addViewPredicate(new PlayerPreferenceChecker(playerDataManager, "seeThroughPortal"));
        addTeleportPredicate(new PermissionsChecker("betterportals.use"));
    }

    @Override
    public void addActivationPredicate(PortalPredicate predicate) {
        logger.fine("Portal activation predicate added of type %s", predicate.getClass().getName());
        activationPredicates.add(predicate);
    }

    @Override
    public boolean removeActivationPredicate(PortalPredicate predicate) {
        logger.fine("Portal activation predicate removed of type %s", predicate.getClass().getName());
        return activationPredicates.remove(predicate);
    }

    @Override
    public void addViewPredicate(PortalPredicate predicate) {
        logger.fine("Portal view predicate added of type %s", predicate.getClass().getName());
        viewPredicates.add(predicate);
    }

    @Override
    public boolean removeViewPredicate(PortalPredicate predicate) {
        logger.fine("Portal view predicate removed of type %s", predicate.getClass().getName());
        return viewPredicates.remove(predicate);
    }

    @Override
    public void addTeleportPredicate(PortalPredicate predicate) {
        logger.fine("Portal teleportation predicate added of type %s", predicate.getClass().getName());
        teleportationPredicates.add(predicate);
    }

    @Override
    public boolean removeTeleportPredicate(PortalPredicate predicate) {
        logger.fine("Portal teleportation predicate removed of type %s", predicate.getClass().getName());
        return teleportationPredicates.remove(predicate);
    }

    @Override
    public boolean isActivatable(IPortal portal, Player player) {
        for(PortalPredicate predicate : activationPredicates) {
            if(!predicate.test(portal, player)) {return false;}
        }
        return true;
    }

    @Override
    public boolean isViewable(IPortal portal, Player player) {
        for(PortalPredicate predicate : viewPredicates) {
            if(!predicate.test(portal, player)) {return false;}
        }
        return true;
    }

    @Override
    public boolean canTeleport(IPortal portal, Player player) {
        for(PortalPredicate predicate : teleportationPredicates) {
            if(!predicate.test(portal, player)) {return false;}
        }
        return true;
    }
}
