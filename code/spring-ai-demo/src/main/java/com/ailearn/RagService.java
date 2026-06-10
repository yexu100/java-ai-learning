package com.ailearn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RAG 服务——手动实现全流程
 *
 * 流程：读文件 → 切分块 → 嵌入向量 → 存索引 → 检索 → 增强Prompt → 回答
 * 手动实现比依赖框架 API 更可靠，也更清楚每一步在干什么
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private static final int TOP_K = 3;          // 检索返回前几个相关片段
    private static final int CHUNK_SIZE = 400;   // 每个文本块最大字符数

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    /** 存储切分后的文本块和对应的向量 */
    private final List<Chunk> chunks = new ArrayList<>();
    private boolean indexed = false;

    public RagService(ChatClient.Builder builder, EmbeddingModel embeddingModel) {
        this.chatClient = builder.build();
        this.embeddingModel = embeddingModel;
    }

    // ==================== 索引流程 ====================

    /**
     * 索引指定目录下的 .md 文件
     */
    public String indexNotes(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return "目录不存在: " + directoryPath;
        }

        // 1. 读取所有 .md 文件内容
        List<File> mdFiles;
        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            mdFiles = paths
                    .filter(p -> p.toString().endsWith(".md"))
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return "读取目录失败: " + e.getMessage();
        }

        if (mdFiles.isEmpty()) {
            return "未找到 .md 文件";
        }

        // 读取文件内容，附带文件名作为来源标记
        List<String> rawTexts = new ArrayList<>();
        for (File f : mdFiles) {
            try {
                String content = Files.readString(f.toPath());
                if (content.trim().length() > 20) {
                    rawTexts.add("【" + f.getName() + "】\n" + content);
                    log.info("读取: {} ({} 字符)", f.getName(), content.length());
                }
            } catch (IOException e) {
                log.warn("跳过文件: {}", f.getName());
            }
        }

        // 2. 文本切分：按段落 + 长度限制
        List<String> allChunks = new ArrayList<>();
        for (String text : rawTexts) {
            String[] paragraphs = text.split("\n\n");
            for (String para : paragraphs) {
                para = para.trim();
                if (para.length() < 20) continue;  // 过短跳过
                if (para.length() > CHUNK_SIZE) {
                    // 超长的按字符切分
                    for (int i = 0; i < para.length(); i += CHUNK_SIZE) {
                        int end = Math.min(i + CHUNK_SIZE, para.length());
                        allChunks.add(para.substring(i, end));
                    }
                } else {
                    allChunks.add(para);
                }
            }
        }
        log.info("切分完成: {} 个文本块", allChunks.size());

        // 3. 批量生成嵌入向量（DashScope 的 embedding API）
        chunks.clear();
        int batchSize = 10;
        for (int i = 0; i < allChunks.size(); i += batchSize) {
            List<String> batch = allChunks.subList(i, Math.min(i + batchSize, allChunks.size()));
            List<float[]> vectors = embeddingModel.embed(batch);
            for (int j = 0; j < batch.size(); j++) {
                chunks.add(new Chunk(batch.get(j), toPrimitive(vectors.get(j))));
            }
            log.info("嵌入进度: {}/{}", Math.min(i + batchSize, allChunks.size()), allChunks.size());
        }

        indexed = true;
        return String.format("索引完成：%d 个文件 → %d 个文本块 → %d 个向量",
                mdFiles.size(), allChunks.size(), chunks.size());
    }

    // ==================== 检索问答 ====================

    /**
     * RAG 问答：检索笔记 → 增强 Prompt → 回答
     */
    public String ask(String question) {
        if (!indexed) {
            return "请先索引笔记！调用 /rag/index?dir=笔记目录 加载笔记后再提问。";
        }

        // 1. 把问题转为向量
        double[] questionVec = toPrimitive(embeddingModel.embed(question));

        // 2. 找最相关的 TOP_K 个文本块（余弦相似度）
        List<Chunk> topChunks = chunks.stream()
                .sorted((a, b) -> Double.compare(
                        cosineSimilarity(questionVec, b.vector),
                        cosineSimilarity(questionVec, a.vector)))
                .limit(TOP_K)
                .collect(Collectors.toList());

        // 3. 构建增强 Prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是 Java AI 学习者的私人学习助手。请基于以下参考资料回答问题。\n");
        prompt.append("如果资料中没有相关信息，请如实说我的笔记中没有相关内容。\n\n");
        prompt.append("=== 参考资料 ===\n");
        for (int i = 0; i < topChunks.size(); i++) {
            prompt.append("【片段 ").append(i + 1).append("】\n");
            prompt.append(topChunks.get(i).text).append("\n\n");
        }
        prompt.append("=== 用户问题 ===\n").append(question);

        // 4. 发给 AI
        return chatClient.prompt(prompt.toString())
                .call()
                .content();
    }

    // ==================== 工具方法 ====================

    /** float[] 转 double[] */
    private double[] toPrimitive(float[] arr) {
        double[] result = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    /** 余弦相似度计算 */
    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /** 文本块 + 向量 */
    private static class Chunk {
        final String text;
        final double[] vector;
        Chunk(String text, double[] vector) {
            this.text = text;
            this.vector = vector;
        }
    }

    public boolean isIndexed() {
        return indexed;
    }

    // ==================== 调试/学习用方法 ====================

    public String debugShowChunks() {
        if (chunks.isEmpty()) return "还没有索引数据，先调用 /rag/index?dir=...";
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(chunks.size()).append(" 个文本块\n");
        sb.append("======================\n\n");
        for (int i = 0; i < chunks.size(); i++) {
            sb.append("--- 片段 ").append(i + 1).append(" ---\n");
            sb.append("长度: ").append(chunks.get(i).text.length()).append(" 字符\n");
            sb.append("内容:\n").append(chunks.get(i).text).append("\n\n");
        }
        return sb.toString();
    }

    public String debugShowVector(int chunkIndex) {
        if (chunks.isEmpty()) return "还没有索引数据";
        if (chunkIndex < 0 || chunkIndex >= chunks.size())
            return "索引范围 0~" + (chunks.size() - 1);
        double[] vec = chunks.get(chunkIndex).vector;
        StringBuilder sb = new StringBuilder();
        sb.append("片段 ").append(chunkIndex).append(" 的向量（共 ").append(vec.length).append(" 维）\n");
        sb.append("前 10 个值: ");
        for (int i = 0; i < Math.min(10, vec.length); i++) {
            sb.append(String.format("%.6f", vec[i])).append(" ");
        }
        sb.append("\n后 5 个值: ");
        for (int i = Math.max(0, vec.length - 5); i < vec.length; i++) {
            sb.append(String.format("%.6f", vec[i])).append(" ");
        }
        return sb.toString();
    }

    public String debugAskWithDetail(String question) {
        if (!indexed) return "请先索引笔记";

        // 1. 问题转向量
        double[] questionVec = toPrimitive(embeddingModel.embed(question));

        // 2. 计算每个片段的相似度
        List<ScoredChunk> scored = new ArrayList<>();
        for (Chunk c : chunks) {
            double score = cosineSimilarity(questionVec, c.vector);
            scored.add(new ScoredChunk(c.text, score));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));

        StringBuilder sb = new StringBuilder();
        sb.append("问题: ").append(question).append("\n\n");
        sb.append("相似度排名:\n");
        for (int i = 0; i < scored.size(); i++) {
            sb.append("  #").append(i + 1)
              .append(" (相似度: ").append(String.format("%.4f", scored.get(i).score)).append(")")
              .append("  ").append(scored.get(i).text.substring(0, Math.min(80, scored.get(i).text.length()))).append("...\n");
        }

        // 3. 取 TOP_K
        List<Chunk> topChunks = scored.subList(0, Math.min(TOP_K, scored.size())).stream()
                .map(s -> new Chunk(s.text, null))
                .collect(Collectors.toList());

        // 4. 构建增强 Prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是 Java AI 学习者的私人学习助手。请基于以下参考资料回答问题。\n");
        prompt.append("如果资料中没有相关信息，请如实说我的笔记中没有相关内容。\n\n");
        prompt.append("=== 参考资料 ===\n");
        for (int i = 0; i < topChunks.size(); i++) {
            prompt.append("【片段 ").append(i + 1).append("】\n");
            prompt.append(topChunks.get(i).text).append("\n\n");
        }
        prompt.append("=== 用户问题 ===\n").append(question);

        sb.append("\n实际发给 AI 的 Prompt:\n");
        sb.append("======================\n");
        sb.append(prompt);

        return sb.toString();
    }

    private static class ScoredChunk {
        final String text;
        final double score;
        ScoredChunk(String text, double score) {
            this.text = text;
            this.score = score;
        }
    }
}
