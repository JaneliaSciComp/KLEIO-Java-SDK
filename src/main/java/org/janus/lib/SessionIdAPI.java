package org.janus.lib;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.imglib2.type.numeric.integer.UnsignedLongType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class SessionIdAPI {
    private static final String API_LINK = "http://c13u06.int.janelia.org:8000/v1/id";

    public static UnsignedLongType getNextId() {
        final ClientConfig clientConfig = new DefaultClientConfig();
        final Client client = Client.create(clientConfig);
        final WebResource webResource = client.resource(UriBuilder.fromUri(API_LINK).build());

        final String clientResponse = webResource.type(MediaType.APPLICATION_JSON).post(String.class);

        UnsignedLongType newSessionId = SessionId.fromString(clientResponse);
        System.out.println("New SessionID:  " + newSessionId);
        return newSessionId;
    }

    public static void main(String[] args) {
        SessionIdAPI.getNextId();
    }

}
