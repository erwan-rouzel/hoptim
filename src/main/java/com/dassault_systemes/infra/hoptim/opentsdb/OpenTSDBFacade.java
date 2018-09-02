package com.dassault_systemes.infra.hoptim.opentsdb;

import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import org.codehaus.jettison.json.JSONException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the main entry point to this package (following the Facade Design Pattern).
 *
 * Created by ERL1 on 4/15/2016.
 */
public class OpenTSDBFacade implements IOpenTSDBDriver {
    private static OpenTSDBFacade instance;
    private IOpenTSDBDriver openTSDBDriver;

    /**
     * Private constructor. This just prevent from instanciating this class without going through the getInstance method.
     *
     * @throws IOException
     */
    private OpenTSDBFacade() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        String openTSDBDriverClassName = SettingsFacade.getInstance().opentsdb.getDriverClassName();
        Class<?> clazz = Class.forName(openTSDBDriverClassName);
        Method method = clazz.getMethod("getInstance");
        openTSDBDriver = (IOpenTSDBDriver) method.invoke(null);
    }

    /**
     * Gets an instance of this class if it exists, otherwise create this first instance and then return it.
     * (following the Singleton Design Pattern).
     *
     * @return The single instance of this class
     * @throws IOException
     */
    public static OpenTSDBFacade getInstance() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        if(instance == null) {
            instance = new OpenTSDBFacade();
        }

        return instance;
    }

    /**
     * Retrieves a list of region views accross which is spanned the TSDB table.
     *
     * @return List of region views
     * @throws IOException
     */
    public List<RegionView> getTsdbRegionsViews() throws IOException {
        return openTSDBDriver.getTsdbRegionsViews();
    }

    public RegionView getTsdbRegionsInfos(String encodedName) throws IOException {
        return openTSDBDriver.getTsdbRegionsInfos(encodedName);
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
        return openTSDBDriver.getRegionServerForMetric(metricName);
    }

    /**
     * Retrieves all informations concerning a metric given its name.
     *
     * @param metricName Name of the metric
     * @return The information on the metric
     * @throws IOException
     */
    public Metric getMetric(String metricName) throws IOException, JSONException {
        return openTSDBDriver.getMetric(metricName);
    }

    public Metric getMetricById(int id) throws IOException {
        return openTSDBDriver.getMetricById(id);
    }

    public Metric getMetricByKey(byte[] key) throws IOException {
        return openTSDBDriver.getMetricByKey(key);
    }

    public ArrayList<String> getMetricsForRange(OpenTSDBKey startKey, OpenTSDBKey endKey) throws IOException {
        return openTSDBDriver.getMetricsForRange(startKey, endKey);
    }

    /**
     * Gets the user friendly name of a metric (as a String) from its raw representation (as bytes).
     *
     * @param metric Bytes array representing the metric
     * @return The name of the metric
     * @throws IOException
     */
    public String getMetricValue(byte[] metric) throws IOException {
        return openTSDBDriver.getMetricValue(metric);
    }

    /**
     * Gets the user friendly name of a tagk (as a String) from its raw representation (as bytes).
     *
     * @param metric Bytes array representing the tagk
     * @return The name of the tagk
     * @throws IOException
     */
    public String getTagkValue(byte[] tagk) throws IOException {
        return openTSDBDriver.getTagkValue(tagk);
    }

    /**
     * Gets the user friendly name of a tagv (as a String) from its raw representation (as bytes).
     *
     * @param metric Bytes array representing the tagv
     * @return The name of the tagv
     * @throws IOException
     */
    public String getTagvValue(byte[] tagv) throws IOException {
        return openTSDBDriver.getTagvValue(tagv);
    }

    /**
     * Retrieves the key associated to a metric.
     *
     * @param metricName Name of the metric
     * @return The key as an array of bytes
     * @throws IOException
     */
    public OpenTSDBKey getFirstKeyForMetric(String metricName) throws IOException {
        return openTSDBDriver.getFirstKeyForMetric(metricName);
    }
    /**
     * Retrieves the key associated to a metric.
     *
     * @param metricName Name of the metric
     * @return The key as an array of bytes
     * @throws IOException
     */
    public OpenTSDBKey getLastKeyForMetric(String metricName) throws IOException {
        return openTSDBDriver.getLastKeyForMetric(metricName);
    }


    /**
     * Retrieves the list of tags for a metric.
     *
     * @param metricName Name of the metric
     * @return The list of raw tags
     * @throws IOException
     */
    public ArrayList<OpenTSDBTagVal> getTagsListForMetric(String metricName) throws IOException, JSONException {
        return openTSDBDriver.getTagsListForMetric(metricName);
    }

    public ArrayList<String> getMetricsForRegion(String regionEncodedName, String metricPrefix) throws IOException {
        return openTSDBDriver.getMetricsForRegion(regionEncodedName, metricPrefix);
    }

    /**
     * Retrieves the list of all the metrics having a given prefix.
     *
     * @param metricPrefix The prefix of the metric name
     * @return The list of all metrics
     * @throws IOException
     */
    public ArrayList<String> getAllMetrics(String metricPrefix) throws IOException {
        return openTSDBDriver.getAllMetrics(metricPrefix);
    }

    public SplitRecommandation getPresplitRecommandation() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        return openTSDBDriver.getPresplitRecommandation();
    }

    public PresplitFile getPresplitFile() throws IOException {
        return openTSDBDriver.getPresplitFile();
    }
}
