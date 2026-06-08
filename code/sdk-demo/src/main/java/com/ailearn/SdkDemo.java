package com.ailearn;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * DashScope SDK 调用示例
 * 对比手拼 JSON 的方式，SDK 帮你封装了所有 HTTP 细节
 */
public class SdkDemo {

    /** 读取 API Key：先读环境变量，没有则从 .env 文件读取 */
    private static String loadApiKey() {
        String key = System.getenv("DASHSCOPE_API_KEY");
        if (key != null && !key.isEmpty()) return key;

        File[] candidates = {
                new File(".env"),
                new File("../.env"),
                new File("../../.env"),
                new File(System.getProperty("user.home") + "/java-ai-learning/.env")
        };
        for (File f : candidates) {
            if (f.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("DASHSCOPE_API_KEY=")) {
                            return line.substring("DASHSCOPE_API_KEY=".length()).trim();
                        }
                    }
                } catch (IOException ignored) {}
            }
        }
        return null;
    }

    public static void main(String[] args) throws ApiException, NoApiKeyException, InputRequiredException {
        String apiKey = loadApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("================================================");
            System.err.println("  ERROR: DASHSCOPE_API_KEY not set");
            System.err.println("  在项目根目录创建 .env 文件：");
            System.err.println("  DASHSCOPE_API_KEY=sk-你的Key");
            System.err.println("================================================");
            return;
        }
        System.setProperty("dashscope.api.key", apiKey);

        // 1. 准备消息
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are a helpful Java tutor. Keep answers concise.")
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("What is the JVM memory model?")
                .build();


        // 2. 构造请求参数（替代手拼 JSON）
        GenerationParam param = GenerationParam.builder()
                .model("qwen-plus")          // 模型名
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))  // API Key
                .messages(Arrays.asList(systemMsg, userMsg))
                .temperature(0.3F)             // 控制创造力
                .maxTokens(500)               // 控制长度
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        System.out.println("Sending via SDK...");
        System.out.println("================================================");

        // 3. 调用 API（不用管 HTTP 连接、JSON 拼装、响应解析）
        Generation gen = new Generation();
        GenerationResult result = gen.call(param);

        // 4. 直接拿结果
        String content = result.getOutput().getChoices().get(0).getMessage().getContent();
        System.out.println(content);

        System.out.println("================================================");
        System.out.println("Tokens used: " + result.getUsage().getInputTokens()
                + " in / " + result.getUsage().getOutputTokens() + " out");
    }
}
