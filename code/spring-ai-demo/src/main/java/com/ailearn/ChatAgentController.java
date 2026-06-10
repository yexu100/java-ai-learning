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
     * GET http://localhost:8080/api/chat?msg=你好&sessionId=abc123&role=comedian
     */
    @GetMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chat(@RequestParam String msg,
                             @RequestParam(defaultValue = "default") String sessionId,
                             @RequestParam(defaultValue = "default") String role,
                             HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        // 1. 保存用户消息
        memory.computeIfAbsent(sessionId, k -> new ArrayList<>())
              .add(Map.of("role", "user", "content", msg));

        // 2. 取出该会话的历史消息
        List<Map<String, String>> history = memory.get(sessionId);

        // 3. 构建带角色和上下文的 prompt
        String contextPrompt = buildContextPrompt(history, role);

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
     * 把角色设定 + 历史消息拼成 Prompt
     */
    private String buildContextPrompt(List<Map<String, String>> history, String role) {
        StringBuilder sb = new StringBuilder();

        // 角色设定
        String systemMessage = getSystemMessage(role);
        if (systemMessage != null) {
            sb.append("【系统指令】").append(systemMessage).append("\n\n");
        }

        // 对话历史
        for (Map<String, String> turn : history) {
            String r = turn.get("role");
            String content = turn.get("content");
            if ("user".equals(r)) {
                sb.append("用户: ").append(content).append("\n");
            } else {
                sb.append("助手: ").append(content).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 根据角色返回对应的系统指令
     */
    private String getSystemMessage(String role) {
        switch (role) {
            case "comedian":
                return "你是一个脱口秀演员，每句话都要幽默风趣，多用比喻和段子，让用户笑出来。";
            case "interviewer":
                return "你是一个严肃的技术面试官，用提问的方式回答用户的问题，引导用户思考。";
            case "kid":
                return "你是一个刚上小学的孩子，用最简单的语言回答问题，语气天真可爱。";
            default:
                return null;
        }
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
