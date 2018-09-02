package com.dassault_systemes.infra.hoptim.rest;

import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.simple.container.SimpleServerFactory;

/**
 * Created by ERL1 on 4/26/2016.
 *
 * This is very simple and basic HTTP server. This needs to be started for the RESTful API to be contacted.
 *
 */
public class SimpleServer {
    public static void main( String[] args ) throws Exception {
        java.io.Closeable server = null;
        ResourceConfig resourceConfig = new ClassNamesResourceConfig(RestApi.class);
        resourceConfig.getContainerResponseFilters().add(new GZIPContentEncodingFilter());

        /* Uncomment these lines to active HTTP traces in the server log */
        resourceConfig.getProperties().put(ResourceConfig.FEATURE_TRACE, true);
        resourceConfig.getProperties().put(ResourceConfig.FEATURE_TRACE_PER_REQUEST, true);
        resourceConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, com.sun.jersey.api.container.filter.LoggingFilter.class);
        resourceConfig.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, com.sun.jersey.api.container.filter.LoggingFilter.class);

        // Creates a server and listens on the address below.
        // Scans classpath for JAX-RS resources
        String restApiPort = SettingsFacade.getInstance().hbase.getRestApiPort();
        server = SimpleServerFactory.create("http://localhost:" + restApiPort, resourceConfig);
        System.out.println("The HTTP server is now running on port " + restApiPort + "...");
    }
}
