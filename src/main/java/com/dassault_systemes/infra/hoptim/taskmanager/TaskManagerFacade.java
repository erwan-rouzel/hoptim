package com.dassault_systemes.infra.hoptim.taskmanager;

import com.dassault_systemes.infra.hoptim.log.CustomLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by ERL1 on 5/23/2016.
 */
public class TaskManagerFacade {
    private static TaskManagerFacade instance;
    private CustomThreadPoolExecutor threadPoolExecutor;

    private TaskManagerFacade() {
        CustomLogger.debug(this, "Init TaskManagerFacade: START");
        Integer threadCounter = 0;
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(50);
        threadPoolExecutor = new CustomThreadPoolExecutor(10);
        CustomLogger.debug(this, "Init TaskManagerFacade: DONE");
    }

    /**
     * Gets an instance of this class if it exists, otherwise create this first instance and then return it.
     * (following the Singleton Design Pattern).
     *
     * @return The single instance of this class
     * @throws IOException
     */
    public static TaskManagerFacade getInstance() throws IOException {
        if(instance == null) {
            instance = new TaskManagerFacade();
        }

        return instance;
    }

    public void execute(Task task) {
        CustomLogger.debug(this, "Execute task: " + task.getName());
        threadPoolExecutor.execute(task);
    }

    public void scheduleAtFixedRate(Task task, long initialDelay, long period) { threadPoolExecutor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);}

    public Collection<Runnable> getActiveTasks() {
        return threadPoolExecutor.getActiveTasks();
    }
}
