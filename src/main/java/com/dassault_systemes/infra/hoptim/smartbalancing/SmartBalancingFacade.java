package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import com.dassault_systemes.infra.hoptim.taskmanager.TaskManagerFacade;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * This is the main entry point to this package (following the Facade Design Pattern).
 *
 * Created by ERL1 on 5/11/2016.
 */
public class SmartBalancingFacade {
    private static SmartBalancingFacade instance;
    private Configuration conf;
    private HBaseFacade hBaseFacade;
    private SettingsFacade settingsFacade;

    /**
     * Private constructor. This just prevent from instanciating this class without going through the getInstance method.
     *
     * @throws IOException
     */
    private SmartBalancingFacade() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        CustomLogger.debug(this, "Init SmartBalancingFacade: START");
        hBaseFacade = HBaseFacade.getInstance();
        settingsFacade = SettingsFacade.getInstance();
        CustomLogger.debug(this, "Init SmartBalancingFacade: DONE");
    }

    /**
     * Gets an instance of this class if it exists, otherwise create this first instance and then return it.
     * (following the Singleton Design Pattern).
     *
     * @return The single instance of this class
     * @throws IOException
     */
    public static SmartBalancingFacade getInstance() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        if(instance == null) {
            instance = new SmartBalancingFacade();
        }

        return instance;
    }

    public ExecutionPlan getSmartBalancingExecutionPlan(String algorithmClassName, String jsonConfig) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException, InvocationTargetException, NoSuchMethodException {
        Class<?> clazz = Class.forName(algorithmClassName);
        IBalancingAlgorithm balancingAlgorithm = (IBalancingAlgorithm) clazz.getConstructor(String.class).newInstance(jsonConfig);

        return balancingAlgorithm.getExecutionPlan();
    }

    /**
     * Same as getRegionServersSmartBalancingExecute but adds a delay to operation to avoid storm compactions.
     * Plus it
     *
     * @return A Json with the status of operation (1 for success)
     * @throws IOException
     */
    public String getRegionServersSmartBalancingExecuteDelayedWithRepeat(String algorithmClassName, String jsonConfig) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = Class.forName(algorithmClassName);
        IBalancingAlgorithm balancingAlgorithm = (IBalancingAlgorithm) clazz.getConstructor(String.class).newInstance(jsonConfig);

        int delayedExecutionTime = settingsFacade.opentsdb.getDelayedExecutionTime();
        long period = settingsFacade.opentsdb.getSchedulerPeriod();
        long delay = settingsFacade.opentsdb.getSchedulerDelay();
        SmartBalancingTask smartBalancingTask = new SmartBalancingTask("Smart Balancing Delayed [periodic]", delayedExecutionTime, balancingAlgorithm, delay, period);
        TaskManagerFacade.getInstance().scheduleAtFixedRate(smartBalancingTask, delay, period);

        return "{\"status\":1}";
    }

    /**
     * Same as getRegionServersSmartBalancingExecute but adds a delay to operation to avoid storm compactions.
     *
     * @return A Json with the status of operation (1 for success)
     * @throws IOException
     */
    public String getRegionServersSmartBalancingExecuteDelayed(String algorithmClassName, String jsonConfig) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = Class.forName(algorithmClassName);
        IBalancingAlgorithm balancingAlgorithm = (IBalancingAlgorithm) clazz.getConstructor(String.class).newInstance(jsonConfig);

        int delayedExecutionTime = settingsFacade.opentsdb.getDelayedExecutionTime();
        SmartBalancingTask smartBalancingTask = new SmartBalancingTask("Smart Balancing Delayed", delayedExecutionTime, balancingAlgorithm);
        System.out.println("Execute smart balancing delayed");
        TaskManagerFacade.getInstance().execute(smartBalancingTask);

        return "{\"status\":1}";
    }

    /**
     * Actually executes the operations to optimize the cluster. The execution is done immediatly but we should
     * be careful here about possible storm compactions during the process. For this reason there exists also
     * a delayed version of this method.
     *
     * @return A Json with the status of operation (1 for success)
     * @throws IOException
     */
    public String getRegionServersSmartBalancingExecute(String algorithmClassName, String jsonConfig) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = Class.forName(algorithmClassName);
        //Car.class.getConstructor(String.class).newInstance("Lightning McQueen");
        IBalancingAlgorithm balancingAlgorithm = (IBalancingAlgorithm) clazz.getConstructor(String.class).newInstance(jsonConfig);

        SmartBalancingTask smartBalancingTask = new SmartBalancingTask("Smart Balancing", 0, balancingAlgorithm);
        TaskManagerFacade.getInstance().execute(smartBalancingTask);

        return "{\"status\":1}";
    }
}
