package com.dassault_systemes.infra.hoptim.hbase;

import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by ERL1 on 6/6/2016.
 */
public class SettingsTest extends TestCase {

    public void testReadSettings() {
        try {
            SettingsFacade settings = SettingsFacade.getInstance();
            assertTrue(settings.hbase.getRegionServerMemory().length() > 0);
            assertTrue(settings.hbase.getDriverClassName().length() > 0);
            assertTrue(settings.opentsdb.getTsdbTable().length() > 0);
            assertTrue(settings.opentsdb.getTsdbUidTable().length() > 0);
            assertTrue(settings.opentsdb.getApiUrl().length() > 0);
            assertTrue(settings.opentsdb.getVipMetrics().size() >= 3);
            assertTrue(settings.opentsdb.getVipMetrics().get("paas.supervision.alert") == 0.4);
            assertTrue(settings.opentsdb.getDelayedExecutionTime() > 0);
            assertTrue(settings.opentsdb.getScoreGainThreshold() > 0);
            assertTrue(settings.opentsdb.getSchedulerPeriod() > 0);
        } catch (IOException e) {
            assertTrue(false);
            e.printStackTrace();
        }
    }
}
