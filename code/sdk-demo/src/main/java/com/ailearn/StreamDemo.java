package com.ailearn;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * 流式输出（SSE）示例
 * 一个字一个字地返回，就像 ChatGPT 网页上那样
 */
public class StreamDemo {

    /** 读取 API Key：先读环境变量，没有则从 .env 文件读取 */
    private static String loadApiKey() {
        // 1. 环境变量优先
        String key = System.getenv("DASHSCOPE_API_KEY");
        if (key != null && !key.isEmpty()) return key;

        // 2. 尝试从 .env 文件读
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

    public static void main(String[] args) throws ApiException, NoApiKeyException, InputRequiredException, InterruptedException {
        // 读取 API Key
        String apiKey = loadApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("================================================");
            System.err.println("  ERROR: DASHSCOPE_API_KEY not set");
            System.err.println("  1. Register at https://bailian.console.aliyun.com/");
            System.err.println("  2. Run: $env:DASHSCOPE_API_KEY = \"sk-xxx\"");
            System.err.println("     或者在项目根目录创建 .env 文件：");
            System.err.println("     DASHSCOPE_API_KEY=sk-你的Key");
            System.err.println("================================================");
            return;
        }

        // 设到系统属性，SDK 内部也能读到
        System.setProperty("dashscope.api.key", apiKey);

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("用三句话解释什么是 ChatGPT")
                .build();

        GenerationParam param = GenerationParam.builder()
                .model("qwen-plus")
                .apiKey(apiKey)
                .messages(Arrays.asList(userMsg))
                .temperature(0.5F)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        System.out.println("流式输出效果（一个字一个字出现）:");
        System.out.println("================================================");

        CountDownLatch latch = new CountDownLatch(1);

        Generation gen = new Generation();
        gen.streamCall(param, new ResultCallback<GenerationResult>() {
            @Override
            public void onEvent(GenerationResult result) {
                // 每次收到一个片段，立刻打印
                String delta = result.getOutput().getChoices().get(0).getMessage().getContent();
                if (delta != null) {
                    System.out.print(delta);
                    System.out.flush();  // 强制刷新，实现"一个字一个字出现"的效果
                }
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Error: " + e.getMessage());
                latch.countDown();
            }

            @Override
            public void onComplete() {
                System.out.println("\n================================================");
                System.out.println("输出完成");
                latch.countDown();
            }
        });

        latch.await();  // 等待流式输出结束
    }
}
