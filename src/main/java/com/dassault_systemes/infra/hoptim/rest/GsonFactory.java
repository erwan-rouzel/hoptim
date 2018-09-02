package com.dassault_systemes.infra.hoptim.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by ERL1 on 4/26/2016.
 *
 * We may use Gson in different parts of our potentially large server application, so it is a good idea to have some common utils for it.
 */
public class GsonFactory {
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * This method creates a instance of Gson properly configured to be used everywhere on our server
     * @return The json object built
     */
    public static Gson getGson() {
        // A builder pattern, with chained calls to set things up, then create()
        return new GsonBuilder()
                .disableHtmlEscaping()
                .setDateFormat(ISO_DATE_FORMAT)
                .create();
    }

}
