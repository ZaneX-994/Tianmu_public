package com.bytewizard.userservice.utils;

import java.util.Random;

public class NickNameGeneratorUtil {
    static String[] adj = { "高效的", "敏捷的", "可靠的", "灵活的", "稳健的",
            "轻量的", "强大的", "优雅的", "可扩展的", "可维护的",
            "智能的", "简洁的", "友好的", "安全的", "高性能的",
            "分布式的", "模块化的", "可复用的", "低延迟的"};
    static String[] none = {"系统", "平台", "服务", "引擎", "组件",
            "框架", "模块", "接口", "网关", "集群",
            "任务", "流程", "策略", "算法", "模型",
            "缓存", "队列", "索引", "控制台", "监控",
            "日志", "配置", "会话", "事务", "存储",
            "连接", "容器", "调度器", "代理", "插件"
    };

    public static String generateNickName() {
        Random random = new Random();
        return adj[random.nextInt(adj.length)] + none[random.nextInt(none.length)];
    }
}
