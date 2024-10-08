package com.lauriethefish.betterportals.proxy.net;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lauriethefish.betterportals.proxy.IProxy;
import com.lauriethefish.betterportals.shared.logging.Logger;
import com.lauriethefish.betterportals.shared.net.IRequestHandler;
import com.lauriethefish.betterportals.shared.net.RequestException;
import com.lauriethefish.betterportals.shared.net.Response;
import com.lauriethefish.betterportals.shared.net.ServerNotFoundException;
import com.lauriethefish.betterportals.shared.net.requests.RelayRequest;
import com.lauriethefish.betterportals.shared.net.requests.Request;
import com.lauriethefish.betterportals.shared.net.requests.TeleportRequest;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

@Singleton
public class ProxyRequestHandler implements IRequestHandler {
    private final IPortalServer portalServer;
    private final Logger logger;
    private final IProxy proxy;

    @Inject
    public ProxyRequestHandler(IPortalServer portalServer, Logger logger, IProxy proxy) {
        this.portalServer = portalServer;
        this.logger = logger;
        this.proxy = proxy;
    }

    @Override
    public void handleRequest(@NotNull Request request, @NotNull Consumer<Response> onFinish) {
        logger.finer("Processing request of type: %s", request.getClass().getName());

        try {
            if(request instanceof RelayRequest) {
                handleRelayRequest((RelayRequest) request, onFinish);
            }   else if(request instanceof TeleportRequest) {
                handleTeleportRequest((TeleportRequest) request, onFinish);
            }   else    {
                throw new IllegalStateException("Unknown request type " + request.getClass().getName());
            }
        }   catch(RequestException ex) {
            Response response = new Response();
            response.setError(ex);
            onFinish.accept(response);
        }   catch(Exception ex) {
            Response response = new Response();
            response.setError(new RequestException(ex, "Internal error occurred on the proxy while processing request"));
        }
    }

    /**
     * Finds the server with name <code>serverName</code>
     * @param serverName The server to find
     * @return The handler for the server with name <code>serverName</code>.
     * @throws ServerNotFoundException If the server isn't registered
     */
    private IClientHandler checkExists(String serverName) throws ServerNotFoundException {
        IClientHandler clientHandler = portalServer.getServer(serverName);
        if(clientHandler == null) {
            throw new ServerNotFoundException(serverName);
        }

        return clientHandler;
    }

    private void handleRelayRequest(RelayRequest request, Consumer<Response> onFinish) throws RequestException  {
        IClientHandler clientHandler = checkExists(request.getDestination());
        clientHandler.sendRequest(request, onFinish);
    }

    private void handleTeleportRequest(TeleportRequest request, Consumer<Response> onFinish) throws RequestException {
        IClientHandler clientHandler = checkExists(request.getDestServer());

        // Tell the other server that the player needs to be teleported to the destination when they join
        logger.fine("Requesting teleport on join for player %s", request.getPlayerId());
        clientHandler.sendRequest(request, (response) -> {
            try {
                response.checkForErrors();
                logger.fine("No errors while setting to teleport on join, moving server!");

                UUID playerId = request.getPlayerId();

                if(!proxy.playerExists(playerId)) {
                    throw new RequestException(String.format("No player with UUID %s exists", playerId));
                }

                proxy.changePlayerServer(playerId, clientHandler.getServerName());
            }   catch(RequestException ex) {
                onFinish.accept(response);
            }
        });

        onFinish.accept(new Response());
    }
}
