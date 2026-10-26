package com.ecommerce.dashboard.config;

import org.springframework.stereotype.Component;

/**
 * 数据就绪状态（/api/ready 就绪检查，借鉴队友版 DataReadyState）。
 *
 * <p>由 {@link DatabaseInitializer} 在启动导入流程中更新：
 * <ul>
 *   <li>导入开始前调用 {@link #markImporting()}，ready=false；</li>
 *   <li>导入 / 校验完成后调用 {@link #markReady(String)}，ready=true。</li>
 * </ul>
 * 作为单例 Bean 注入 HealthController 与 DatabaseInitializer，共享同一份状态。
 */
@Component
public class DataReadyState {

    /** 是否就绪（volatile 保证跨线程可见） */
    private volatile boolean ready = false;

    /** 当前状态描述（导入中 / 已就绪 等） */
    private volatile String message = "初始化中…";

    public boolean isReady() {
        return ready;
    }

    public String getMessage() {
        return message;
    }

    /** 标记“正在导入数据”：先写 message，ready 保持 false（/api/ready 返回 503）。 */
    public void markImporting() {
        this.message = "正在导入数据，请稍候…";
        this.ready = false;
    }

    /**
     * 标记“数据已就绪”：先写 message，再置 ready=true（/api/ready 返回 200）。
     * @param msg 就绪描述，空则用默认文案
     */
    public void markReady(String msg) {
        this.message = (msg == null || msg.isEmpty()) ? "数据已就绪" : msg;
        this.ready = true;
    }
}
