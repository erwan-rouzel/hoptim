package com.dassault_systemes.infra.hoptim.rest;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.opentsdb.OpenTSDBFacade;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import com.dassault_systemes.infra.hoptim.smartbalancing.AlgorithmConfig;
import com.dassault_systemes.infra.hoptim.smartbalancing.SmartBalancingFacade;
import com.dassault_systemes.infra.hoptim.taskmanager.TaskManagerFacade;
import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.codehaus.jettison.json.JSONException;
import org.glassfish.jersey.internal.inject.Custom;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ERL1 on 4/26/2016.
 *
 * This is a RESTful API to retrieve simplified informations from HBase / OpenTSDB.
 *
 */
@Path("/api")
public class RestApi {
    private static Configuration conf = null;
    private static SmartBalancingFacade smartBalancingFacade;
    private static HBaseFacade hbaseFacade;
    private static OpenTSDBFacade tsdbFacade;
    private static TaskManagerFacade taskManagerFacade;
    private static SettingsFacade settingsFacade;
    private static Gson jsonFactory;
    private final static ArrayList<String> EMPTY_JSON_RESPONSE = new ArrayList<String>();

    public RestApi() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            hbaseFacade = HBaseFacade.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            tsdbFacade = OpenTSDBFacade.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            smartBalancingFacade = SmartBalancingFacade.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            taskManagerFacade = TaskManagerFacade.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            settingsFacade = SettingsFacade.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        jsonFactory = GsonFactory.getGson();
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    private Response getResponse(Object data, int status) {
        String dataAsJson;

        if(! data.getClass().equals(String.class)) {
            dataAsJson = jsonFactory.toJson(data);
        } else {
            dataAsJson = (String)data;
        }

        CustomLogger.debug(this, "Json extract: " + dataAsJson.substring(0, Math.min(dataAsJson.length() - 1, 60)));

        return Response.status(status)
                .entity(dataAsJson)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
                .header("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, Origin, User-Agent, DNT, Cache-Control, X-Mx-ReqToken, Keep-Alive, X-Requested-With, If-Modified-Since")
                .header("Accept-Ranges", "bytes")
                .header("Date", getServerTime())
                .build();
    }

    @GET
    @Path("/hello")
    @Produces("application/json")
    public Response getHello() {
        try {
            return getResponse("Hello!", 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(e, 500);
        }
    }

    @GET
    @Path("/metrics/{metric_prefix}")
    @Produces("application/json")
    public Response getMetrics(@PathParam("metric_prefix") String metricPrefix){
        try {
            return getResponse(tsdbFacade.getAllMetrics(metricPrefix), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/metrics/{region_encoded_name}/{metric_prefix}")
    @Produces("application/json")
    public Response getMetricsForRegion(@PathParam("region_encoded_name") String regionEncodedName, @PathParam("metric_prefix") String metricPrefix){
        try {
            return getResponse(tsdbFacade.getMetricsForRegion(regionEncodedName, metricPrefix), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/metric/{metric_name}")
    @Produces("application/json")
    public Response getMetric(@PathParam("metric_name") String metricName) {
        try {
            return getResponse(tsdbFacade.getMetric(metricName), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/metric/{metric_name}/region_servers")
    @Produces("application/json")
    public Response getMetricRegionServers(@PathParam("metric_name") String metricName) {
        try {
            return getResponse(tsdbFacade.getRegionServerForMetric(metricName), 200);
        } catch (IOException e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/region_servers")
    @Produces("application/json")
    public Response getRegionServers() {
        try {
            return getResponse(hbaseFacade.getRegionsDistributionPerRegionServer(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/regions")
    @Produces("application/json")
    public Response getRegions() {
        try {
            return getResponse(hbaseFacade.getRegions(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/regions/infos/{encoded_name}")
    @Produces("application/json")
    public Response getRegionsInfos(@PathParam("encoded_name") String encodedName) {
        try {
            return getResponse(hbaseFacade.getRegion(encodedName), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/region/split/{encoded_name}")
    @Produces("application/json")
    public Response getRegionSplit(@PathParam("encoded_name") String encodedName) {
        try {
            return getResponse(hbaseFacade.splitRegion(encodedName, null), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/tables")
    @Produces("application/json")
    public Response getTables() {
        try {
            return getResponse(hbaseFacade.getTables(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/tables/infos/{table_name}")
    @Produces("application/json")
    public Response getTablesInfos(@PathParam("table_name") String tableName) {
        try {
            return getResponse(hbaseFacade.getTable(tableName), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/region_servers/smart_balancing/plan/{algorithm_class_name}/{config_json_encoded}")
    @Produces("application/json")
    public Response getRegionServersSmartBalancingPlan(@PathParam("algorithm_class_name") String algorithmClassName, @PathParam("config_json_encoded") String configJsonEncoded) {
        try {
            String jsonConfig = URLDecoder.decode(configJsonEncoded, "UTF-8");
            CustomLogger.debug(this, "jsonConfig=" + jsonConfig);
            return getResponse(smartBalancingFacade.getSmartBalancingExecutionPlan(algorithmClassName, jsonConfig), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/region_servers/smart_balancing/execute_delayed_repeat/{algorithm_class_name}/{config_json_encoded}")
    @Produces("application/json")
    public Response getRegionServersSmartBalancingExecuteDelayedWithRepeat(@PathParam("algorithm_class_name") String algorithmClassName, @PathParam("config_json_encoded") String configJsonEncoded) {
        try {
            String jsonConfig = URLDecoder.decode(configJsonEncoded, "UTF-8");
            CustomLogger.debug(this, "jsonConfig=" + jsonConfig);
            return getResponse(smartBalancingFacade.getRegionServersSmartBalancingExecuteDelayedWithRepeat(algorithmClassName, jsonConfig), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/region_servers/smart_balancing/execute_delayed/{algorithm_class_name}/{config_json_encoded}")
    @Produces("application/json")
    public Response getRegionServersSmartBalancingExecuteDelayed(@PathParam("algorithm_class_name") String algorithmClassName, @PathParam("config_json_encoded") String configJsonEncoded) {
        try {
            String jsonConfig = URLDecoder.decode(configJsonEncoded, "UTF-8");
            CustomLogger.debug(this, "jsonConfig=" + jsonConfig);
            return getResponse(smartBalancingFacade.getRegionServersSmartBalancingExecuteDelayed(algorithmClassName, jsonConfig), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/region_servers/smart_balancing/execute/{algorithm_class_name}/{config_json_encoded}")
    @Produces("application/json")
    public Response getRegionServersSmartBalancingExecute(@PathParam("algorithm_class_name") String algorithmClassName, @PathParam("config_json_encoded") String configJsonEncoded) {
        try {
            String jsonConfig = URLDecoder.decode(configJsonEncoded, "UTF-8");
            CustomLogger.debug(this, "jsonConfig=" + jsonConfig);
            return getResponse(smartBalancingFacade.getRegionServersSmartBalancingExecute(algorithmClassName, jsonConfig), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/tasks/active")
    @Produces("application/json")
    public Response getActiveTasks() {
        try {
            return getResponse(taskManagerFacade.getActiveTasks(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/regions/tsdb/views")
    @Produces("application/json")
    public Response getTsdbRegionsViews() {
        try {
            return getResponse(tsdbFacade.getTsdbRegionsViews(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/regions/tsdb/infos/{encoded_name}")
    @Produces("application/json")
    public Response getTsdbRegionsInfos(@PathParam("encoded_name") String encodedName) {
        try {
            return getResponse(tsdbFacade.getTsdbRegionsInfos(encodedName), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/regions/tsdb/presplit")
    @Produces("application/json")
    public Response getTsdbPresplitRecommandation() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            return getResponse(tsdbFacade.getPresplitRecommandation(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/regions/tsdb/presplit/file")
    @Produces("application/json")
    public Response getTsdbPresplitFile() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            return getResponse(tsdbFacade.getPresplitFile(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/dump/update")
    @Produces("application/json")
    public Response getUpdateDump() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            return getResponse(hbaseFacade.updateDump(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/dump/timestamp")
    @Produces("application/json")
    public Response getLatestDumpTimestamp() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            return getResponse(hbaseFacade.latestDumpTimestamp(), 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }

    @GET
    @Path("/info")
    @Produces("application/json")
    public Response getInfo() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            ToolInfo info = new ToolInfo();
            info.name = settingsFacade.hbase.getName();
            info.version = settingsFacade.hbase.getVersion();
            return getResponse(info, 200);
        } catch (Exception e) {
            e.printStackTrace();
            return getResponse(EMPTY_JSON_RESPONSE, 500);
        }
    }
}
