package com.ailearn;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * AI 聊天控制器
 * 对比之前手写 HttpClient + 手拼 JSON，Spring AI 只需要一行调用
 */
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;

    /**
     * Spring AI 自动注入 ChatClient
     * 它封装了：HTTP 连接、JSON 序列化、API Key 传递、响应解析
     */
    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 普通问答（非流式）
     * GET http://localhost:8080/ai/chat?msg=什么是JVM
     */
    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "用一句话解释什么是 AI") String msg) {
        return chatClient.prompt(msg)
                .call()
                .content();
    }

    /**
     * 流式问答（一个字一个字地返回）
     * GET http://localhost:8080/ai/stream?msg=讲个笑话
     */
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> stream(@RequestParam(defaultValue = "讲一个关于程序员的笑话") String msg,
                               HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt(msg)
                .stream()
                .content()
                .map(chunk -> chunk + "\n\n");  // SSE 格式
    }
}
