package com.dassault_systemes.infra.hoptim.smartbalancing;

import org.apache.hadoop.hbase.HBaseIOException;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * This abstract class represents any possible operations concerning regions.
 *
 * Created by ERL1 on 5/10/2016.
 */
public abstract class ClusterOperation {
    public final String operationName;

    public ClusterOperation() {
        operationName = this.getClass().getSimpleName();
    }

    public void execute() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InterruptedException, InvocationTargetException, NoSuchMethodException {}
}
