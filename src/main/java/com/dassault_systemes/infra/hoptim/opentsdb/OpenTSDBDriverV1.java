package com.dassault_systemes.infra.hoptim.opentsdb;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import com.dassault_systemes.infra.hoptim.util.BytesHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ERL1 on 6/21/2016.
 */
public class OpenTSDBDriverV1 implements IOpenTSDBDriver {
    private static OpenTSDBDriverV1 instance;
    private HTable tsdbTable;
    private HTable tsdbUidTable;
    private Configuration conf;
    private HBaseAdmin hbaseAdmin;
    private HBaseFacade hBaseFacade;
    private SettingsFacade settingsFacade;

    /**
     * Private constructor. This just prevent from instanciating this class without going through the getInstance method.
     *
     * @throws IOException
     */
    private OpenTSDBDriverV1() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        CustomLogger.debug(this, "Init OpenTSDBFacade: START");
        this.conf = HBaseConfiguration.create();
        if(! SettingsFacade.getInstance().hbase.getZookeeperQuorum().equals("auto")) {
            conf.set("hbase.zookeeper.quorum", SettingsFacade.getInstance().hbase.getZookeeperQuorum());
        }
        CustomLogger.debug(this, "Init HBaseAdmin: START");
        hbaseAdmin = new HBaseAdmin(conf);
        CustomLogger.debug(this, "Init HBaseAdmin: DONE");
        hBaseFacade = HBaseFacade.getInstance();
        settingsFacade = SettingsFacade.getInstance();
        tsdbTable = new HTable(conf, Bytes.toBytes(settingsFacade.opentsdb.getTsdbTable()));
        tsdbUidTable = new HTable(conf, Bytes.toBytes(settingsFacade.opentsdb.getTsdbUidTable()));
        CustomLogger.debug(this, "Init OpenTSDBFacade: DONE");
    }

    /**
     * Gets an instance of this class if it exists, otherwise create this first instance and then return it.
     * (following the Singleton Design Pattern).
     *
     * @return The single instance of this class
     * @throws IOException
     */
    public static OpenTSDBDriverV1 getInstance() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        if(instance == null) {
            instance = new OpenTSDBDriverV1();
        }

        return instance;
    }


    /**
     * Retrieves the list of regions accross which is spanned the TSDB table.
     *
     * @return List of regions
     * @throws IOException
     */
    private List<HRegionInfo> getTsdbRegions() throws IOException {
        return hbaseAdmin.getTableRegions(tsdbTable.getName());
    }

    /**
     * Retrieves a list of region views accross which is spanned the TSDB table.
     *
     * @return List of region views
     * @throws IOException
     */
    public List<RegionView> getTsdbRegionsViews() throws IOException {
        List<HRegionInfo> listRegions = hbaseAdmin.getTableRegions(tsdbTable.getName());
        List<RegionView> listRegionViews = new ArrayList<RegionView>();

        for(HRegionInfo region: listRegions) {
            RegionView regionView = new RegionView();

            regionView.id = region.getRegionId();
            regionView.encodedName = region.getEncodedName();
            regionView.tableName = region.getTable().getNameAsString();
            regionView.regionName = region.getRegionNameAsString();

            OpenTSDBKey tsdbStartKey;
            OpenTSDBKey tsdbEndKey;

            tsdbStartKey = new OpenTSDBKey(region.getStartKey());
            tsdbEndKey = new OpenTSDBKey(region.getEndKey());

            regionView.startKey = tsdbStartKey;
            regionView.endKey = tsdbEndKey;
            regionView.startMetricName = getMetricValue(tsdbStartKey.getMetricAsBytes());
            regionView.endMetricName = getMetricValue(tsdbEndKey.getMetricAsBytes());

            listRegionViews.add(regionView);
        }

        return listRegionViews;
    }

    public RegionView getTsdbRegionsInfos(String encodedName) throws IOException {
        List<RegionView> tsdbRegionViews = getTsdbRegionsViews();

        for(RegionView regionView: tsdbRegionViews) {
            if(regionView.encodedName.equals(encodedName)) {
                regionView.startMetricName = getMetricValue(regionView.startKey.getMetricAsBytes());
                regionView.endMetricName = getMetricValue(regionView.endKey.getMetricAsBytes());
                regionView.metrics = getMetricsForRegion(encodedName, "*");
                return regionView;
            }
        }

        return null;
    }

    public RegionView getRegionForMetric(String metricName) throws IOException {
        return null;
    }

    /**
     * Retrieves where a metric is stored (on which region server).
     *
     * @param metricName Name of the metric
     * @return The location of the metric
     * @throws IOException
     */
    public MetricLocation getRegionServerForMetric(String metricName) throws IOException {
        byte[] keyForMetric = this.getFirstKeyForMetric(metricName).getRawKeyAsBytes();
        List<HRegionInfo> listRegions = this.getTsdbRegions();
        MetricLocation metricLocation = new MetricLocation();

        for(HRegionInfo region: listRegions) {
            if(     ( (region.getStartKey().length == 0) || (Bytes.compareTo(region.getStartKey(), keyForMetric) < 0) ) &&
                    ( (region.getEndKey().length == 0) || (Bytes.compareTo(keyForMetric, region.getEndKey()) < 0) ) ) {
                metricLocation.regionServers.add(hBaseFacade.getRegionServerForRegion(region));
            }
        }

        return metricLocation;
    }

    /**
     * Retrieves all informations concerning a metric given its name.
     *
     * @param metricName Name of the metric
     * @return The information on the metric
     * @throws IOException
     */
    public Metric getMetric(String metricName) throws IOException, JSONException {
        Metric metric = new Metric();
        metric.name = metricName;
        metric.regionServers = getRegionServerForMetric(metricName).regionServers;
        ArrayList<OpenTSDBTagVal> tagValList = getTagsListForMetric(metricName);
        metric.tags = tagValList;

        return metric;
    }

    public Metric getMetricById(int id) throws IOException {
        //TODO
        return null;
    }

    public Metric getMetricByKey(byte[] key) throws IOException {
        //TODO
        return null;
    }

    public ArrayList<String> getMetricsForRange(OpenTSDBKey startKey, OpenTSDBKey endKey) throws IOException {
        //TODO
        return null;
    }

    /**
     * Gets the user friendly name of a metric (as a String) from its raw representation (as bytes).
     *
     * @param metric Bytes array representing the metric
     * @return The name of the metric
     * @throws IOException
     */
    public String getMetricValue(byte[] metric) throws IOException {
        return getUidQualifierValue(metric, "metrics");
    }

    /**
     * Gets the user friendly name of a tagk (as a String) from its raw representation (as bytes).
     *
     * @param metric Bytes array representing the tagk
     * @return The name of the tagk
     * @throws IOException
     */
    public String getTagkValue(byte[] tagk) throws IOException {
        return getUidQualifierValue(tagk, "tagk");
    }

    /**
     * Gets the user friendly name of a tagv (as a String) from its raw representation (as bytes).
     *
     * @param metric Bytes array representing the tagv
     * @return The name of the tagv
     * @throws IOException
     */
    public String getTagvValue(byte[] tagv) throws IOException {
        return getUidQualifierValue(tagv, "tagv");
    }

    private String getUidQualifierValue(byte[] row, String qualifier) throws IOException {
        Get getMetric = new Get(row);
        getMetric.addColumn(Bytes.toBytes("name"), Bytes.toBytes(qualifier));
        return BytesHelper.bytesToString(tsdbUidTable.get(getMetric).value());
    }

    /**
     * Retrieves the key associated to a metric.
     *
     * @param metricName Name of the metric
     * @return The key as an array of bytes
     * @throws IOException
     */
    public OpenTSDBKey getFirstKeyForMetric(String metricName) throws IOException {
        Filter metricFilter = new ValueFilter(CompareFilter.CompareOp.EQUAL, new BinaryPrefixComparator(Bytes.toBytes(metricName)));

        Scan scanMetric = new Scan();
        scanMetric.setFilter(metricFilter);
        ResultScanner resultScanner = tsdbUidTable.getScanner(scanMetric);

        return new OpenTSDBKey(resultScanner.next().getRow());
    }

    public OpenTSDBKey getLastKeyForMetric(String metricName) throws IOException {
        //TODO
        return null;
    }

    /**
     * Retrieves the list of tags for a metric.
     *
     * @param metricName Name of the metric
     * @return The list of raw tags
     * @throws IOException
     */
    public ArrayList<OpenTSDBTagVal> getTagsListForMetric(String metricName) throws IOException, JSONException {
        CustomLogger.debug(this, "getTagsListForMetric");
        ArrayList<OpenTSDBTagVal> tagsList = new ArrayList<OpenTSDBTagVal>();

        String allMetricsAsJson = getJsonFromTsdbApi("/search/tsmeta_summary?query=" + metricName + "&limit=1&start_index=0");
        CustomLogger.debug(this, "allMetricsAsJson=" + allMetricsAsJson);

        JSONObject metrics = new JSONObject(allMetricsAsJson);
        CustomLogger.debug(this, "metrics=" + metrics.toString());

        JSONArray results = metrics.getJSONArray("results");
        CustomLogger.debug(this, "results=" + results.toString());

        JSONObject firstResult = results.getJSONObject(0);
        CustomLogger.debug(this, "firstResult=" + firstResult.toString());

        JSONObject tags = firstResult.getJSONObject("tags");
        CustomLogger.debug(this, "tags=" + tags.toString());

        Iterator<?> keys = tags.keys();
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            CustomLogger.debug(this, "key=" + key);

            if ( tags.get(key) instanceof JSONObject ) {
                CustomLogger.debug(this, "val=" + tags.getString(key));
                tagsList.add(new OpenTSDBTagVal(key, tags.getString(key)));
            }
        }

        return tagsList;
    }

    public ArrayList<String> getMetricsForRegion(String regionEncodedName, String metricPrefix) throws IOException {
        List<RegionView> tsdbRegionViews = getTsdbRegionsViews();
        RegionView regionView = new RegionView();

        for(RegionView r: tsdbRegionViews) {
            if(r.encodedName.equals(regionEncodedName)) {
                regionView = r;
            }
        }

        TableName tableName = TableName.valueOf(regionView.tableName);
        HRegionInfo regionInfo = new HRegionInfo(tableName, regionView.startKey.getRawKeyAsBytes(), regionView.endKey.getRawKeyAsBytes());

        return getMetricsForRegion(regionInfo, metricPrefix);
    }

    /**
     * Retrieves the list of metrics for a given region.
     *
     * @param region The region
     * @param metricPrefix The prefix of metric
     * @return A list of metric names
     * @throws IOException
     */
    private ArrayList<String> getMetricsForRegion(HRegionInfo region, String metricPrefix) throws IOException {
        ArrayList<String> metricsList = new ArrayList<String>();
        OpenTSDBKey startKey =  new OpenTSDBKey(region.getStartKey());
        OpenTSDBKey endKey = new OpenTSDBKey(region.getEndKey());

        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);

        if(! startKey.isNull()) {
            Filter filter1 = new RowFilter(CompareFilter.CompareOp.GREATER, new BinaryComparator(startKey.getMetricAsBytes()));
            filterList.addFilter(filter1);
        }

        if(! endKey.isNull()) {
            Filter filter2 = new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL, new BinaryComparator(endKey.getMetricAsBytes()));
            filterList.addFilter(filter2);
        }

        if(metricPrefix.equals("*")) {
            /* To get all the metrics, we use a trick here. Quite strangely using EQUAL with a prefix of "" does not return all the results as expected.
             * So instead we use NOT_EQUAL operator with a prefix "*" which will return all metrics since no metric begins with "*".
             */
            Filter filter3 = new SingleColumnValueFilter("name".getBytes(), "metrics".getBytes(), CompareFilter.CompareOp.NOT_EQUAL, new BinaryPrefixComparator(metricPrefix.getBytes()));
            filterList.addFilter(filter3);
        } else if (metricPrefix.length() > 0) {
            Filter filter3 = new SingleColumnValueFilter("name".getBytes(), "metrics".getBytes(), CompareFilter.CompareOp.EQUAL, new BinaryPrefixComparator(metricPrefix.getBytes()));
            filterList.addFilter(filter3);
        }

        Scan scan = new Scan();
        scan.setFilter(filterList);
        scan.addColumn(Bytes.toBytes("name"), Bytes.toBytes("metrics"));
        ResultScanner resultScanner = tsdbUidTable.getScanner(scan);

        for(Result result: resultScanner) {
            metricsList.add(BytesHelper.bytesToString(result.value()));
        }

        Collections.sort(metricsList);

        return metricsList;
    }

    /**
     * Retrieves the list of all the metrics having a given prefix.
     *
     * @param metricPrefix The prefix of the metric name
     * @return The list of all metrics
     * @throws IOException
     */
    public ArrayList<String> getAllMetrics(String metricPrefix) throws IOException {
        String allMetricsAsJson = getJsonFromTsdbApi("/suggest?type=metrics&q=" + metricPrefix + "&max=100000");
        ArrayList<String> allMetrics = new ArrayList<String>();

        JsonElement jsonElement = new JsonParser().parse(allMetricsAsJson);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        for(JsonElement je: jsonArray) {
            allMetrics.add(je.getAsString());
        }

        return allMetrics;
    }

    private String getJsonFromTsdbApi(String path) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(settingsFacade.opentsdb.getApiUrl() + path);
        CloseableHttpResponse httpResponse = null;

        httpResponse = httpClient.execute(httpGet);
        CustomLogger.debug(this, "GET Response Status:: " + httpResponse.getStatusLine().getStatusCode());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();
        httpClient.close();

        return response.toString();
    }

    public SplitRecommandation getPresplitRecommandation() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        return new SplitRecommandation();
    }

    public PresplitFile getPresplitFile() throws IOException {
        PresplitFile presplitFile = new PresplitFile();
        List<RegionView> regions = getTsdbRegionsViews();
        ArrayList<String> addedKeys = new ArrayList<String>();

        for(RegionView region: regions) {
            if(BytesHelper.compare(region.startKey.getRawKeyAsBytes(), OpenTSDBKey.SMALLEST_KEY.getRawKeyAsBytes()) > 0) {
                if(!addedKeys.contains(region.startKey.getRawKeyAsHex())) {
                    addedKeys.add(region.startKey.getRawKeyAsHex());
                    presplitFile.content += region.startKey.getRawKeyAsHex() + "\n";
                }
            }
        }

        return presplitFile;
    }
}
