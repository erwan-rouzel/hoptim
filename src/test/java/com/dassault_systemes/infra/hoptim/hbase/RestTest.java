package com.dassault_systemes.infra.hoptim.hbase;

import com.dassault_systemes.infra.hoptim.opentsdb.MetricLocation;
import com.dassault_systemes.infra.hoptim.smartbalancing.AlgorithmConfig;
import com.dassault_systemes.infra.hoptim.taskmanager.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.deploy.net.URLEncoder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.*;
import junit.framework.TestCase;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ERL1 on 5/25/2016.
 */
public class RestTest extends TestCase {

    public RestTest(String testName)
    {
        super(testName);
    }

    public void testHello() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/hello");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);
            assertTrue(response.getEntity(String.class).equals("Hello!"));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    private RegionView getOneRegion() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/regions");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
            assertTrue(response.getStatus() == 200);

            ArrayList<RegionView> tables = new Gson().fromJson(response.getEntity(String.class), new TypeToken<ArrayList<RegionView>>(){}.getType());
            return tables.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private TableView getOneTable() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/tables");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            ArrayList<TableView> tables = new Gson().fromJson(response.getEntity(String.class), new TypeToken<ArrayList<TableView>>(){}.getType());
            return tables.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public RegionView getOneTsdbRegion() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/regions/tsdb/views");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            ArrayList<RegionView> regions = new Gson().fromJson(response.getEntity(String.class), new TypeToken<ArrayList<RegionView>>(){}.getType());
            return regions.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void testGetMetrics() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/metrics/p");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);
            assertTrue(response.getEntity(String.class).length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetMetricsForRegion() {
        try {
            RegionView oneRegion = getOneRegion();

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/metrics/" + oneRegion.encodedName + "/*");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            ArrayList<String> metricsForRegion = new Gson().fromJson(response.getEntity(String.class), new TypeToken<ArrayList<String>>(){}.getType());
            assertTrue(response.getStatus() == 200);
            //assertTrue(metricsForRegion.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetMetric() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/metric/paas.supervision.alert");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);
            assertTrue(response.getEntity(String.class).length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetMetricRegionServers() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/metric/paas.supervision.alert/region_servers");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            MetricLocation location = new Gson().fromJson(response.getEntity(String.class), new TypeToken<MetricLocation>(){}.getType());
            assertTrue(location.regionServers.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetRegionServers() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/region_servers");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            HashMap<String, ArrayList<RegionView>> regionServers = new Gson().fromJson(
                    response.getEntity(String.class),
                    new TypeToken<HashMap<String, ArrayList<RegionView>>>(){}.getType());
            assertTrue(regionServers.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetRegions() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/regions");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            ArrayList<RegionView> regions = new Gson().fromJson(response.getEntity(String.class), new TypeToken<ArrayList<RegionView>>(){}.getType());
            assertTrue(regions.size() > 0);
            assertTrue(regions.get(0).encodedName.length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetRegionsInfos() {
        try {
            RegionView oneRegion = getOneRegion();

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/regions/infos/" + oneRegion.encodedName);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            RegionView retrievedRegion = new Gson().fromJson(response.getEntity(String.class), new TypeToken<RegionView>(){}.getType());
            assertNotNull(retrievedRegion);
            assertTrue(oneRegion.regionName.equals(retrievedRegion.regionName));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetTables() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/tables");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            ArrayList<TableView> tables = new Gson().fromJson(response.getEntity(String.class), new TypeToken<ArrayList<TableView>>(){}.getType());
            assertNotNull(tables);
            assertTrue(tables.size() > 0);
            assertTrue(tables.get(0).tableName.length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetTablesInfos() {
        try {
            TableView oneTable = getOneTable();

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/tables/infos/" + oneTable.tableName);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            TableView table = new Gson().fromJson(response.getEntity(String.class), new TypeToken<TableView>(){}.getType());
            assertNotNull(table);
            assertTrue(table.tableName.equals(oneTable.tableName));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetRegionServersSmartBalancingPlan_SizeBalancingAlgorithm() {
        try {
            TableView oneTable = getOneTable();

            Client client = Client.create();
            String json = "{}";
            String jsonEncoded = URLEncoder.encode(json, "UTF-8");
            WebResource webResource = client.resource("http://localhost:7777/api/region_servers/smart_balancing/plan/com.dassault_systemes.infra.hoptim.smartbalancing.SizeBalancingAlgorithm/" + jsonEncoded);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            String responseAsString = response.getEntity(String.class);
            assertTrue(responseAsString.length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetRegionServersSmartBalancingPlan_OptaPlannerBalancingAlgorithm() {
        try {
            TableView oneTable = getOneTable();

            Client client = Client.create();
            String json = "{\"timeout\": 5, \"size_weight\": 50, \"read_weight\": 50, \"write_weight\": 50}";
            String jsonEncoded = URLEncoder.encode(json, "UTF-8");
            WebResource webResource = client.resource("http://localhost:7777/api/region_servers/smart_balancing/plan/com.dassault_systemes.infra.hoptim.smartbalancing.OptaPlannerBalancingAlgorithm/" + jsonEncoded);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            String responseAsString = response.getEntity(String.class);
            assertTrue(responseAsString.length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetRegionServersSmartBalancingPlan_OptaPlannerBalancingAlgorithm_Delayed_WithRepeat() {
        try {
            TableView oneTable = getOneTable();

            Client client = Client.create();
            String json = "{\"timeout\": 5, \"size_weight\": 50, \"read_weight\": 50, \"write_weight\": 50}";
            String jsonEncoded = URLEncoder.encode(json, "UTF-8");
            WebResource webResource = client.resource("http://localhost:7777/api/region_servers/smart_balancing/execute_delayed_repeat/com.dassault_systemes.infra.hoptim.smartbalancing.OptaPlannerBalancingAlgorithm/" + jsonEncoded);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            String responseAsString = response.getEntity(String.class);
            assertTrue(responseAsString.length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetRegionServersSmartBalancingPlan_SplitBalancingAlgorithm() {
        try {
            TableView oneTable = getOneTable();

            Client client = Client.create();
            String json = "{}";
            String jsonEncoded = URLEncoder.encode(json, "UTF-8");
            WebResource webResource = client.resource("http://localhost:7777/api/region_servers/smart_balancing/plan/com.dassault_systemes.infra.hoptim.smartbalancing.OpenTSDBVipSplitAlgorithm/" + jsonEncoded);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            String responseAsString = response.getEntity(String.class);
            assertTrue(responseAsString.length() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetActiveTasks() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/tasks/active");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            ArrayList<Task> tasks = new Gson().fromJson(response.getEntity(String.class), new TypeToken<ArrayList<Task>>(){}.getType());
            assertNotNull(tasks);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetTsdbRegionsViews() {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/regions/tsdb/views");
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            ArrayList<RegionView> regions = new Gson().fromJson(response.getEntity(String.class), new TypeToken<ArrayList<RegionView>>(){}.getType());
            assertNotNull(regions);
            assertTrue(regions.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetTsdbRegionsInfos() {
        try {
            RegionView oneRegion = getOneTsdbRegion();

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:7777/api/regions/tsdb/infos/" + oneRegion.encodedName);
            ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

            assertTrue(response.getStatus() == 200);

            RegionView retrievedRegion = new Gson().fromJson(response.getEntity(String.class), new TypeToken<RegionView>(){}.getType());
            assertNotNull(retrievedRegion);
            assertTrue(retrievedRegion.regionName.equals(oneRegion.regionName));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
