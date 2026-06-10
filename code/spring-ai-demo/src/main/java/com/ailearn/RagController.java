package com.ailearn;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * RAG 问答接口
 */
@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 索引学习笔记
     * GET http://localhost:8080/rag/index?dir=../../../2025/05
     * 不传 dir 默认索引笔记目录
     */
    @GetMapping("/index")
    public String index(@RequestParam(defaultValue = "../../2025/05") String dir) {
        return ragService.indexNotes(dir);
    }

    /**
     * RAG 问答（流式）
     * GET http://localhost:8080/rag/ask?q=Day02学了什么
     */
    @GetMapping(value = "/ask", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> ask(@RequestParam String q, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        String answer = ragService.ask(q);

        if (answer.startsWith("请先索引")) {
            return Flux.just("data: " + answer + "\n\n");
        }

        // 模拟流式输出（按句号拆分）
        String[] parts = answer.split("(?<=[。！？\n])");
        return Flux.fromArray(parts)
                .map(chunk -> chunk + "\n\n")
                .delayElements(java.time.Duration.ofMillis(50));
    }

    @GetMapping("/status")
    public String status() {
        return ragService.isIndexed() ? "已索引" : "未索引";
    }
}
