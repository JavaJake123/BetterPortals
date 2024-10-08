package com.lauriethefish.betterportals.bukkit.block.fetch;

import com.lauriethefish.betterportals.api.IntVector;
import com.lauriethefish.betterportals.bukkit.config.RenderConfig;
import com.lauriethefish.betterportals.bukkit.net.IPortalClient;
import com.lauriethefish.betterportals.bukkit.net.requests.GetBlockDataChangesRequest;
import com.lauriethefish.betterportals.bukkit.nms.BlockDataUtil;
import com.lauriethefish.betterportals.bukkit.portal.IPortal;
import com.lauriethefish.betterportals.shared.logging.Logger;
import com.lauriethefish.betterportals.shared.net.RequestException;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fetches the block data for external portals by sending a request to the destination server.
 */
public class ExternalBlockDataFetcher implements IBlockDataFetcher  {
    private final Logger logger;
    private final IPortalClient portalClient;
    private final GetBlockDataChangesRequest request;
    private final String destServerName;

    private final Map<IntVector, BlockData> currentStates = new HashMap<>();
    private volatile boolean hasFirstRequestFinished = false;
    private volatile boolean hasPreviousRequestFinished = true;

    public ExternalBlockDataFetcher(Logger logger, IPortalClient portalClient, RenderConfig renderConfig, IPortal portal) {
        this.logger = logger;
        this.portalClient = portalClient;
        this.destServerName = portal.getDestPos().getServerName();

        this.request = new GetBlockDataChangesRequest();
        request.setYRadius((int) renderConfig.getMaxY());
        request.setXAndZRadius((int) renderConfig.getMaxXZ());
        request.setChangeSetId(UUID.randomUUID());
        request.setWorldName(portal.getDestPos().getWorldName());
        request.setWorldId(portal.getDestPos().getWorldId());
        request.setPosition(new IntVector(portal.getDestPos().getVector()));
        request.setRotateOriginToDest(portal.getTransformations().getRotateToDestination());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update() {
        if(!hasPreviousRequestFinished) {
            logger.fine("Still awaiting block data response");
            return;
        }

        hasPreviousRequestFinished = false;
        portalClient.sendRequestToServer(request, destServerName, (response) -> {
            hasPreviousRequestFinished = true;
            try {
                logger.finer("Received response to get block data request");
                Map<IntVector, Integer> serializedChanges = (Map<IntVector, Integer>) response.getResult();

                serializedChanges.forEach((position, newValue) -> currentStates.put(position, BlockDataUtil.getByCombinedId(newValue)));

                hasFirstRequestFinished = true;
            }   catch(RequestException ex) {
                logger.warning("Failed to fetch block changes for external portal: ");
                ex.printStackTrace();
            }
        });
    }

    @Override
    public boolean isReady() {
        return hasFirstRequestFinished;
    }

    @Override
    public @NotNull BlockData getData(@NotNull IntVector position) {
        return currentStates.get(position);
    }
}
