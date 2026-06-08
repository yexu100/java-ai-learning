package com.ailearn;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带聊天记忆的 AI 对话控制器
 * 手动管理会话历史，不依赖 Spring AI 的 memory 类
 */
@RestController
@RequestMapping("/api")
public class ChatAgentController {

    private final ChatClient chatClient;

    /**
     * 手动管理会话记忆
     * Map<sessionId, List<{role, content}>>
     */
    private final Map<String, List<Map<String, String>>> memory = new ConcurrentHashMap<>();

    public ChatAgentController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 发送消息（流式）
     * GET http://localhost:8080/api/chat?msg=你好&sessionId=abc123
     */
    @GetMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chat(@RequestParam String msg,
                             @RequestParam(defaultValue = "default") String sessionId,
                             HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        // 1. 保存用户消息
        memory.computeIfAbsent(sessionId, k -> new ArrayList<>())
              .add(Map.of("role", "user", "content", msg));

        // 2. 取出该会话的历史消息
        List<Map<String, String>> history = memory.get(sessionId);

        // 3. 构建带上下文的 prompt
        String contextPrompt = buildContextPrompt(history);

        // 4. 收集完整回复，流结束后保存
        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt(contextPrompt)
                .stream()
                .content()
                .map(chunk -> {
                    fullResponse.append(chunk);
                    return chunk + "\n\n";
                })
                .doFinally(signalType -> {
                    // 5. 保存 AI 回复到历史
                    memory.get(sessionId).add(Map.of("role", "assistant", "content", fullResponse.toString()));
                });
    }

    /**
     * 把历史消息拼成带上下文的 Prompt
     */
    private String buildContextPrompt(List<Map<String, String>> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是一个对话历史，请基于上下文回答最新的问题。\n\n");
        for (Map<String, String> turn : history) {
            String role = turn.get("role");
            String content = turn.get("content");
            if ("user".equals(role)) {
                sb.append("用户: ").append(content).append("\n");
            } else {
                sb.append("助手: ").append(content).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 获取指定会话的历史消息
     * GET http://localhost:8080/api/history?sessionId=abc123
     */
    @GetMapping("/history")
    public List<Map<String, String>> history(@RequestParam(defaultValue = "default") String sessionId) {
        return memory.getOrDefault(sessionId, Collections.emptyList());
    }

    /**
     * 清除历史
     * GET http://localhost:8080/api/clear?sessionId=abc123
     */
    @GetMapping("/clear")
    public String clear(@RequestParam(defaultValue = "default") String sessionId) {
        memory.remove(sessionId);
        return "ok";
    }
}
