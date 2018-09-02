package com.dassault_systemes.infra.hoptim.hbase;

import com.dassault_systemes.infra.hoptim.opentsdb.Metric;
import com.dassault_systemes.infra.hoptim.opentsdb.OpenTSDBFacade;
import com.dassault_systemes.infra.hoptim.opentsdb.OpenTSDBKey;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * Created by ERL1 on 5/25/2016.
 */
public class OpenTSDBTest extends TestCase {
    private OpenTSDBFacade openTSDBFacade;

    /**
     * Create the test case
     *
     * @param testName regionEncodedName of the test case
     */
    public OpenTSDBTest( String testName ) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        super( testName );
        init();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( OpenTSDBTest.class );
    }

    private boolean init() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            openTSDBFacade = OpenTSDBFacade.getInstance();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void testMetric() {
        Metric metric = new Metric();
        metric.id = 9;

        byte[] idAsBytes = metric.getMetricIdAsBytes();
        assertEquals(idAsBytes[0], 0x00);
        assertEquals(idAsBytes[1], 0x00);
        assertEquals(idAsBytes[2], 0x09);
    }

    public void testGetMetric() {

    }
}
