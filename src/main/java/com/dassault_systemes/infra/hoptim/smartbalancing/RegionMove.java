package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * This represents a region move from one region server to another.
 *
 * Created by ERL1 on 5/10/2016.
 */
public class RegionMove extends ClusterOperation {
    public String regionName;
    public String encodedRegionName;
    public String fromRegionServer;
    public String toRegionServer;

    public RegionMove() {
        super();
    }

    @Override
    public void execute() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        CustomLogger.debug(this, "execute");
        HBaseFacade.getInstance().moveRegion(encodedRegionName, toRegionServer);
    }
}
