package com.km.core.util;


import com.km.common.exception.ErrorCode;

/**
 * TODO: 根据现有日志数据分析各类错误，进行细化。
 * 
 * <p>请不要格式化本类代码</p>
 */
public enum FrameworkErrorCode implements ErrorCode {

	RUNTIME_ERROR("Framework-02", "运行过程出错，具体原因请参看运行结束时的错误诊断信息  ."),

    PLUGIN_SPLIT_ERROR("Framework-15", "DataX插件切分出错, 该问题通常是由于DataX各个插件编程错误引起，请联系DataX开发团队解决"),
    KILLED_EXIT_VALUE("Framework-143", "Job 收到了 Kill 命令.");

    private final String code;

    private final String description;

    private FrameworkErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Description:[%s]. ", this.code,
                this.description);
    }

    /**
     * 通过 "Framework-143" 来标示 任务是 Killed 状态
     */
    public int toExitValue() {
        if (this == FrameworkErrorCode.KILLED_EXIT_VALUE) {
            return 143;
        } else {
            return 1;
        }
    }

}
