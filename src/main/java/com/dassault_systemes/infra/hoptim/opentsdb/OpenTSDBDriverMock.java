package com.dassault_systemes.infra.hoptim.opentsdb;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.util.BytesHelper;
import org.apache.hadoop.hbase.HRegionInfo;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ERL1 on 6/21/2016.
 */
public class OpenTSDBDriverMock implements IOpenTSDBDriver {
    private static OpenTSDBDriverMock instance;
    private final static int NB_METRICS = 300;
    private HBaseFacade hBaseFacade;
    private Random random;
    ArrayList<Metric> metrics;

    private ArrayList<Metric> generateMetrics() {
        int metricCounter = 0;
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        ArrayList<Metric> metricsList = new ArrayList<Metric>();

        for(int i = 0; i < NB_METRICS - 4; i++) {
            if(metricCounter == 0) {
                Metric metric1 = new Metric();
                metric1.name = "a.vip.1";
                metric1.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
                metric1.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
                metric1.id = metricCounter++;
                metricsList.add(metric1);
            }

            if(metricCounter == NB_METRICS/5) {
                Metric metric2 = new Metric();
                metric2.name = "d.vip.2";
                metric2.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
                metric2.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
                metric2.id = metricCounter++;
                metricsList.add(metric2);
            }

            if(metricCounter == 2*NB_METRICS/5) {
                Metric metric3 = new Metric();
                metric3.name = "g.vip.3";
                metric3.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
                metric3.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
                metric3.id = metricCounter++;
                metricsList.add(metric3);
            }

            if(metricCounter == 3*NB_METRICS/5) {
                Metric metric4 = new Metric();
                metric4.name = "k.vip.4";
                metric4.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
                metric4.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
                metric4.id = metricCounter++;
                metricsList.add(metric4);
            }

            Metric metric = new Metric();
            metric.name = alphabet.charAt(random.nextInt(alphabet.length())) + ".metric.num" + i;
            metric.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
            metric.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
            metric.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
            metric.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
            metric.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
            metric.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
            metric.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
            metricsList.add(metric);

            metric.id = metricCounter++;
        }

        Metric metric5 = new Metric();
        metric5.name = "paas.supervision.alert";
        metric5.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
        metric5.tags.add(new OpenTSDBTagVal("tag" + random.nextInt(100), new Integer(random.nextInt(1000)).toString()));
        metric5.id = metricCounter++;
        metricsList.add(metric5);

        return metricsList;
    }


    /**
     * Private constructor. This just prevent from instanciating this class without going through the getInstance method.
     *
     * @throws IOException
     */
    private OpenTSDBDriverMock() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        hBaseFacade = HBaseFacade.getInstance();
        random = new Random();

        metrics = generateMetrics();
        ArrayList<RegionView> tsdbRegions = hBaseFacade.getRegionsForTable("tsdb");
        int numberOfMetricsPerRegion;

        if(tsdbRegions.size() != 0) {
            numberOfMetricsPerRegion = NB_METRICS / tsdbRegions.size();
        } else {
            numberOfMetricsPerRegion = 1;
        }

        int index = 0;

        for(RegionView region: tsdbRegions) {
            int indexStart = index*numberOfMetricsPerRegion;
            int indexEnd;

            if(index == (tsdbRegions.size() - 1) ) {
                indexEnd = metrics.size() - 1;
            } else {
                indexEnd = index * numberOfMetricsPerRegion + numberOfMetricsPerRegion - 1;
            }

            region.startMetricName = metrics.get(indexStart).name;
            region.endMetricName = metrics.get(indexEnd).name;


            ByteBuffer b;
            b = ByteBuffer.allocate(4);
            b.putInt(indexStart);
            byte[] indexStartAsBytes = b.array();
            byte[] startKeyAsBytes = new byte[7];
            startKeyAsBytes[0] = indexStartAsBytes[1];
            startKeyAsBytes[1] = indexStartAsBytes[2];
            startKeyAsBytes[2] = indexStartAsBytes[3];

            b = ByteBuffer.allocate(4);
            b.putInt(indexEnd);
            byte[] indexEndAsBytes = b.array();
            byte[] endKeyAsBytes = new byte[7];
            endKeyAsBytes[0] = indexEndAsBytes[1];
            endKeyAsBytes[1] = indexEndAsBytes[2];
            endKeyAsBytes[2] = indexEndAsBytes[3];

            int timestamp = (int) (System.currentTimeMillis() / 1000L);
            b = ByteBuffer.allocate(4);
            b.putInt(timestamp);
            byte[] timestampAsBytes = b.array();
            startKeyAsBytes[3] = endKeyAsBytes[3] = timestampAsBytes[0];
            startKeyAsBytes[4] = endKeyAsBytes[4] = timestampAsBytes[1];
            startKeyAsBytes[5] = endKeyAsBytes[5] = timestampAsBytes[2];
            startKeyAsBytes[6] = endKeyAsBytes[6] = timestampAsBytes[3];

            OpenTSDBKey startKey = new OpenTSDBKey(startKeyAsBytes);
            OpenTSDBKey endKey = new OpenTSDBKey(endKeyAsBytes);

            region.startKey = startKey;
            region.endKey = endKey;

            List<Metric> subMetrics = metrics.subList(indexStart, indexEnd + 1);
            ArrayList<String> metricsList = new ArrayList<String>();
            for(Metric metric: subMetrics) {
                metricsList.add(metric.name);
            }

            region.metrics.addAll(metricsList);
            index++;
        }
    }

    /**
     * Gets an instance of this class if it exists, otherwise create this first instance and then return it.
     * (following the Singleton Design Pattern).
     *
     * @return The single instance of this class
     * @throws IOException
     */
    public static OpenTSDBDriverMock getInstance() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        if(instance == null) {
            instance = new OpenTSDBDriverMock();
        }

        return instance;
    }

    public List<RegionView> getTsdbRegionsViews() throws IOException {
        return hBaseFacade.getRegionsForTable("tsdb");
    }

    public RegionView getTsdbRegionsInfos(String encodedName) throws IOException {
        return hBaseFacade.getRegion(encodedName);
    }

    public RegionView getRegionForMetricId(int metricId) throws IOException {
        Metric metric = getMetricById(metricId);
        return getRegionForMetric(metric.name);
    }

    public RegionView getRegionForMetric(String metricName) throws IOException {
        List<RegionView> regions = getTsdbRegionsViews();

        for(RegionView regionView: regions) {
            if(regionView.metrics.contains(metricName)) {
                return regionView;
            }
        }
        return null;
    }

    public MetricLocation getRegionServerForMetric(String metricName) throws IOException {
        List<RegionView> regions = getTsdbRegionsViews();
        MetricLocation metricLocation = new MetricLocation();

        for(RegionView regionView: regions) {
            if(regionView.metrics.contains(metricName)) {
                metricLocation.regionServers.add(regionView.currentRegionServer);
            }
        }
        return metricLocation;
    }

    public Metric getMetricById(int id) {
        for(Metric metric: metrics) {
            if(metric.id == id) {
                return metric;
            }
        }
        return null;
    }

    public Metric getMetricByKey(byte[] key) {
        byte[] subKeyForMetric = new byte[]{key[0], key[1], key[2]};
        for(Metric metric: metrics) {
            if(BytesHelper.compare(metric.getMetricIdAsBytes(), subKeyForMetric) == 0) {
                return metric;
            }
        }
        return metrics.get(metrics.size() - 1);
    }

    public Metric getMetric(String metricName) throws IOException {
        for(Metric metric: metrics) {
            if(metric.name.equals(metricName)) {
                return metric;
            }
        }
        return null;
    }

    public ArrayList<String> getMetricsForRange(OpenTSDBKey startKey, OpenTSDBKey endKey) {
        ArrayList<String> result = new ArrayList<String>();

        for(Metric metric: metrics) {
            if( BytesHelper.compare(startKey.getMetricAsBytes(), metric.getMetricIdAsBytes()) <= 0 &&
                BytesHelper.compare(metric.getMetricIdAsBytes(), endKey.getMetricAsBytes()) < 0) {
                result.add(metric.name);
            }
        }

        return result;
    }

    public String getMetricValue(byte[] metric) throws IOException {
        return null;
    }

    public String getTagkValue(byte[] tagk) throws IOException {
        return null;
    }

    public String getTagvValue(byte[] tagv) throws IOException {
        return null;
    }

    public String getUidQualifierValue(byte[] row, String qualifier) throws IOException {
        return null;
    }

    public OpenTSDBKey getFirstKeyForMetric(String metricName) throws IOException {
        Metric metric = getMetric(metricName);
        byte[] b = metric.getMetricIdAsBytes();
        if(metric.id == 0) {
            return OpenTSDBKey.SMALLEST_KEY;
        } else {
            return new OpenTSDBKey(new byte[]{b[0], b[1], b[2], 0x00, 0x00, 0x00, 0x00});
        }
    }

    public OpenTSDBKey getLastKeyForMetric(String metricName) throws IOException {
        Metric metric = getMetric(metricName);
        Metric nextMetric = getMetricById(metric.id + 1);

        if(nextMetric == null) {
            return OpenTSDBKey.BIGGEST_KEY;
        } else {
            byte[] b = nextMetric.getMetricIdAsBytes();
            return new OpenTSDBKey(new byte[]{b[0], b[1], b[2], 0x00, 0x00, 0x00, 0x00});
        }
    }

    public ArrayList<OpenTSDBTagVal> getTagsListForMetric(String metricName) throws IOException {
        return new ArrayList<OpenTSDBTagVal>();
    }

    public ArrayList<String> getMetricsForRegion(String regionEncodedName, String metricPrefix) throws IOException {
        RegionView region = hBaseFacade.getRegion(regionEncodedName);
        return region.metrics;
    }

    public ArrayList<String> getMetricsForRegion(HRegionInfo region, String metricPrefix) throws IOException {
        return getMetricsForRegion(region.getEncodedName(), metricPrefix);
    }

    public ArrayList<String> getAllMetrics(String metricPrefix) throws IOException {
        ArrayList<String> metricsList = new ArrayList<String>();
        for(Metric metric: metrics) {
            if(metric.name.startsWith(metricPrefix)) {
                metricsList.add(metric.name);
            }
        }

        return metricsList;
    }

    public SplitRecommandation getPresplitRecommandation() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        return new SplitRecommandation();
    }

    public PresplitFile getPresplitFile() throws IOException {
        PresplitFile presplitFile = new PresplitFile();
        List<RegionView> regions = getTsdbRegionsViews();
        ArrayList<String> addedKeys = new ArrayList<String>();

        for(RegionView region: regions) {
            if(region.startKey != null && BytesHelper.compare(region.startKey.getRawKeyAsBytes(), OpenTSDBKey.SMALLEST_KEY.getRawKeyAsBytes()) > 0) {
                if(!addedKeys.contains(region.startKey.getRawKeyAsHex())) {
                    addedKeys.add(region.startKey.getRawKeyAsHex());
                    presplitFile.content += region.startKey.getRawKeyAsHex() + "\n";
                }
            }
        }

        return presplitFile;
    }
}
