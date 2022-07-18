package org.janelia.scicomp.lib;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.imglib2.type.numeric.integer.UnsignedLongType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.concurrent.ThreadLocalRandom;

public class SessionId {
        private final static boolean TESTING = true;
    private static UnsignedLongType currentId;

    private static final String API_LINK = "http://c13u06.int.janelia.org:8000/v1/id";

    private static UnsignedLongType getNextId() {
        if(TESTING)
            return new UnsignedLongType(ThreadLocalRandom.current().nextInt(10, 1000 ));
        final ClientConfig clientConfig = new DefaultClientConfig();
        final Client client = Client.create(clientConfig);
        final WebResource webResource = client.resource(UriBuilder.fromUri(API_LINK).build());

        final String clientResponse = webResource.type(MediaType.APPLICATION_JSON).post(String.class);

        UnsignedLongType newSessionId = SessionId.fromString(clientResponse);
        System.out.println("New Session ID:  " + newSessionId);
        return newSessionId;
    }

    private static UnsignedLongType fromString(String response) {
        Gson g = new Gson();
        JsonObject s = g.fromJson(response, JsonObject.class);
        return new UnsignedLongType(s.get("id").getAsBigInteger());
    }

    public static UnsignedLongType get() {
        if (currentId == null) {
            currentId = getNextId();
        }
        return currentId;
    }

    public static UnsignedLongType incrementAndGet(){
        currentId = getNextId();
        return currentId;
    }

    @Override
    public String toString() {
        return "SessionId{" +
                "id=" + currentId +
                '}';
    }

    public static void main(String[] args) {
        System.out.println(get());
    }
}
