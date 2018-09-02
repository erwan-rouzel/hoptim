package com.dassault_systemes.infra.hoptim.smartbalancing;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by ERL1 on 5/31/2016.
 */
public interface IBalancingAlgorithm {
    ExecutionPlan getExecutionPlan() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException;
}
