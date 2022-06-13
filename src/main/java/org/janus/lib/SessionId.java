package org.janus.lib;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.imglib2.type.numeric.integer.UnsignedLongType;

public class SessionId {
    UnsignedLongType id;

    public static UnsignedLongType fromString(String response) {
        Gson g = new Gson();
        JsonObject s = g.fromJson(response, JsonObject.class);
        return new UnsignedLongType(s.get("id").getAsBigInteger());
    }

    public UnsignedLongType getId() {
        return id;
    }

    @Override
    public String toString() {
        return "SessionId{" +
                "id=" + id +
                '}';
    }
}
