package com.km.data.core.job.scheduler;

import com.km.data.common.exception.DataETLException;
import com.km.data.common.util.Configuration;
import com.km.data.common.util.FrameworkErrorCode;
import com.km.data.core.statistics.communication.LocalTGCommunicationManager;
import com.km.data.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.km.data.core.taskgroup.TaskGroupContainer;
import com.km.data.core.taskgroup.runner.TaskGroupContainerRunner;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hongjiao.hj on 2014/12/22.
 */
public class StandAloneScheduler extends AbstractScheduler {

    private ExecutorService taskGroupContainerExecutorService;


    public StandAloneScheduler(AbstractContainerCommunicator containerCommunicator) {
        super(containerCommunicator);
    }

    @Override
    public void startAllTaskGroup(List<Configuration> configurations,LocalTGCommunicationManager tgCommunicationManager) {
        this.taskGroupContainerExecutorService = Executors
                .newFixedThreadPool(configurations.size());
        for (Configuration taskGroupConfiguration : configurations) {
            TaskGroupContainerRunner taskGroupContainerRunner = newTaskGroupContainerRunner(taskGroupConfiguration,tgCommunicationManager);
            this.taskGroupContainerExecutorService.execute(taskGroupContainerRunner);
        }
        this.taskGroupContainerExecutorService.shutdown();
    }

    @Override
    protected boolean isJobKilling(Long jobId) {
        return false;
    }

    @Override
    public void dealFailedStat(AbstractContainerCommunicator frameworkCollector, Throwable throwable) {
        this.taskGroupContainerExecutorService.shutdownNow();
        throw DataETLException.asDataETLException(
                FrameworkErrorCode.PLUGIN_RUNTIME_ERROR, throwable);
    }

    @Override
    public void dealKillingStat(AbstractContainerCommunicator frameworkCollector, int totalTasks) {
        //通过进程退出返回码标示状态
        this.taskGroupContainerExecutorService.shutdownNow();
        throw DataETLException.asDataETLException(FrameworkErrorCode.KILLED_EXIT_VALUE,
                "job killed status");
    }


    private TaskGroupContainerRunner newTaskGroupContainerRunner(
            Configuration configuration,LocalTGCommunicationManager tgCommunicationManager) {
        TaskGroupContainer taskGroupContainer = new TaskGroupContainer(configuration,tgCommunicationManager);

        return new TaskGroupContainerRunner(taskGroupContainer);
    }

}
