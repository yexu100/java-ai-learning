package com.ailearn;

import org.springframework.web.bind.annotation.*;

/**
 * RAG 调试控制器——让你在浏览器里看到每一步的中间结果
 */
@RestController
@RequestMapping("/rag/debug")
public class RagDebugController {

    private final RagService ragService;

    public RagDebugController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 查看所有文本块
     * GET http://localhost:8080/rag/debug/chunks
     */
    @GetMapping("/chunks")
    public String chunks() {
        return ragService.debugShowChunks();
    }

    /**
     * 查看指定块的向量（embedding 数值）
     * GET http://localhost:8080/rag/debug/vector?index=0
     */
    @GetMapping("/vector")
    public String vector(@RequestParam(defaultValue = "0") int index) {
        return ragService.debugShowVector(index);
    }

    /**
     * 手把手展示 RAG 检索全过程
     * GET http://localhost:8080/rag/debug/ask?q=Day02学了什么
     */
    @GetMapping("/ask")
    public String askDebug(@RequestParam String q) {
        return ragService.debugAskWithDetail(q);
    }
}
