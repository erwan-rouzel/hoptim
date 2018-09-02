package com.dassault_systemes.infra.hoptim.opentsdb;

import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import org.codehaus.jettison.json.JSONException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ERL1 on 6/21/2016.
 */
interface IOpenTSDBDriver {
    List<RegionView> getTsdbRegionsViews() throws IOException;
    RegionView getTsdbRegionsInfos(String encodedName) throws IOException;
    RegionView getRegionForMetric(String metricName) throws IOException;
    MetricLocation getRegionServerForMetric(String metricName) throws IOException;
    Metric getMetric(String metricName) throws IOException, JSONException;
    Metric getMetricById(int id) throws IOException;
    Metric getMetricByKey(byte[] key) throws IOException;
    ArrayList<String> getMetricsForRange(OpenTSDBKey startKey, OpenTSDBKey endKey) throws IOException;
    String getMetricValue(byte[] metric) throws IOException;
    String getTagkValue(byte[] tagk) throws IOException;
    String getTagvValue(byte[] tagv) throws IOException;
    OpenTSDBKey getFirstKeyForMetric(String metricName) throws IOException;
    OpenTSDBKey getLastKeyForMetric(String metricName) throws IOException;
    ArrayList<OpenTSDBTagVal> getTagsListForMetric(String metricName) throws IOException, JSONException;
    ArrayList<String> getMetricsForRegion(String regionEncodedName, String metricPrefix) throws IOException;
    ArrayList<String> getAllMetrics(String metricPrefix) throws IOException;
    SplitRecommandation getPresplitRecommandation() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException;
    PresplitFile getPresplitFile() throws IOException;
}
