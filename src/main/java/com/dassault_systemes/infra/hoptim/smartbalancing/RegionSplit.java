package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by ERL1 on 6/7/2016.
 */
public class RegionSplit extends ClusterOperation {
    public final static byte[] AUTO_SPLIT_KEY = new byte[]{};
    public String regionEncodedName;
    public byte[] splitKey;
    public String splitKeyAsHex;

    public RegionSplit() {
        super();
    }

    @Override
    public void execute() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InterruptedException, InvocationTargetException, NoSuchMethodException {
        CustomLogger.debug(this, "execute");
        HBaseFacade.getInstance().splitRegion(regionEncodedName, splitKey);
    }
}
