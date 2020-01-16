package com.km.common.exception;

/**
 *
 */
public enum CommonErrorCode implements ErrorCode {

    CONFIG_ERROR("Common-00", "您提供的配置文件存在错误信息，请检查您的作业配置 ."),
    CONVERT_NOT_SUPPORT("Common-01", "同步数据出现业务脏数据情况，数据类型转换错误 ."),
    CONVERT_OVER_FLOW("Common-02", "同步数据出现业务脏数据情况，数据类型转换溢出 ."),
    RUNTIME_ERROR("Common-11", "运行时内部调用错误 .");

    private final String code;

    private final String describe;

    private CommonErrorCode(String code, String describe) {
        this.code = code;
        this.describe = describe;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.describe;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Describe:[%s]", this.code,
                this.describe);
    }

}
