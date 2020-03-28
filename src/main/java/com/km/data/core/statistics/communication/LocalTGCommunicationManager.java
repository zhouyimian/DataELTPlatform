package com.km.data.core.statistics.communication;

import com.km.data.core.enums.State;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalTGCommunicationManager {
    private static Map<Integer, Communication> taskGroupCommunicationMap =
            new ConcurrentHashMap<>();

    public void registerTaskGroupCommunication(
            int taskGroupId, Communication communication) {
        taskGroupCommunicationMap.put(taskGroupId, communication);
    }

    public Communication getJobCommunication() {
        Communication communication = new Communication();
        communication.setState(State.SUCCEEDED);

        for (Communication taskGroupCommunication :
                taskGroupCommunicationMap.values()) {

            communication.mergeFrom(taskGroupCommunication);
        }

        return communication;
    }

    /**
     * 采用获取taskGroupId后再获取对应communication的方式，
     * 防止map遍历时修改，同时也防止对map key-value对的修改
     *
     * @return
     */
    public Set<Integer> getTaskGroupIdSet() {
        return taskGroupCommunicationMap.keySet();
    }

    public Communication getTaskGroupCommunication(int taskGroupId) {
        Validate.isTrue(taskGroupId >= 0, "taskGroupId不能小于0");
        return taskGroupCommunicationMap.get(taskGroupId);
    }

    public void updateTaskGroupCommunication(final int taskGroupId,
                                                    final Communication communication) {
        Validate.isTrue(taskGroupCommunicationMap.containsKey(
                taskGroupId), String.format("taskGroupCommunicationMap中没有注册taskGroupId[%d]的Communication，" +
                "无法更新该taskGroup的信息", taskGroupId));
        taskGroupCommunicationMap.put(taskGroupId, communication);
    }

    public void clear() {
        taskGroupCommunicationMap.clear();
    }

    public Map<Integer, Communication> getTaskGroupCommunicationMap() {
        return taskGroupCommunicationMap;
    }
}