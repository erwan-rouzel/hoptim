package com.dassault_systemes.infra.hoptim.hbase;

import com.dassault_systemes.infra.hoptim.opentsdb.OpenTSDBKey;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * Created by ERL1 on 5/25/2016.
 */
public class HBaseTest extends TestCase {
    private HBaseFacade hBaseFacade;
    private SettingsFacade settingsFacade;

    /**
     * Create the test case
     *
     * @param testName regionEncodedName of the test case
     */
    public HBaseTest( String testName ) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        super( testName );
        settingsFacade = SettingsFacade.getInstance();
        initHBaseFacade();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( HBaseTest.class );
    }

    private boolean initHBaseFacade() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        try {
            hBaseFacade = HBaseFacade.getInstance();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void testConnexion() {
        try {
            assertTrue(initHBaseFacade());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (InstantiationException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetTable() {
        try {
            TableView tableView = hBaseFacade.getTable(settingsFacade.opentsdb.getTsdbTable());
            assertTrue(tableView.tableName.equals(settingsFacade.opentsdb.getTsdbTable()));
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    public void testRegionView() {
        RegionView r = new RegionView();

        r.startKey = new OpenTSDBKey(new byte[]{0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00});
        r.endKey =  new OpenTSDBKey(new byte[]{0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x00});

        assertTrue(r.containsKey(new byte[]{0x00, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00}));
        assertTrue(r.containsKey(new byte[]{0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00}));
        assertFalse(r.containsKey(new byte[]{0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x00}));
        assertFalse(r.containsKey(new byte[]{0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00}));
        assertFalse(r.containsKey(new byte[]{0x00, 0x00, 0x1A, 0x00, 0x00, 0x00, 0x00}));
    }
}
