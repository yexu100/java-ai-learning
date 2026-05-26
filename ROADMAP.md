# AI 学习路线图（Java 开发者视角）

> 路线设计原则：**从能用的到懂原理的，从单点到系统。**  
> 不追热点，不赶进度，每天稳一点。

---

## Phase 1：筑基期（预计 2-3 周）

**目标**：建立对 AI 应用开发的基础认知，能独立完成简单的大模型调用

- [ ] **Day 1-3：Prompt Engineering 基础**
  - 什么是 Prompt？为什么同样的模型，不同的人问效果差很多？
  - Zero-shot / Few-shot / Chain-of-Thought 提示技巧
  - 角色设定、上下文管理、输出格式约束（JSON/XML）
  - 练习：用纯文本 + API 调试验证不同 Prompt 的效果差异

- [ ] **Day 4-6：大模型 API 基础**
  - 了解 OpenAI API / 阿里云百炼 / 百度千帆 / 智谱 API 的基本用法
  - HTTP 调用 vs SDK 调用（先用 curl/HttpClient 手调，再用 SDK）
  - 核心参数：temperature、top_p、max_tokens、system message
  - 费用计算方式：按 Token 计费是怎么算的？

- [ ] **Day 7-10：Java 生态初探**
  - Spring AI 入门：项目初始化、配置 API Key、第一个 `ChatClient`
  - LangChain4j 入门：对比 Spring AI 的差异，选哪个？
  - 核心抽象：ChatModel、EmbeddingModel、VectorStore
  - 跑通第一个"问答机器人" Demo

- [ ] **Day 11-14：小项目实战**
  - 做一个"Java 知识问答助手"（基于自己的笔记做回答）
  - 纯 Prompt 版本 vs 带上下文版本的效果对比
  - 代码整理进 `code/spring-ai/hello-assistant`

---

## Phase 2：RAG 与知识库（预计 3-4 周）

**目标**：让 AI 能读你的文档、回答私域知识

- [ ] **Embedding 与向量数据库**
  - 什么是 Embedding？为什么需要把文本变成向量？
  - 向量数据库选型：Redis Stack、Milvus、PgVector、Chroma
  - Java 中如何生成和存储 Embedding？

- [ ] **RAG 完整流程**
  - Document → Chunk → Embedding → Store → Retrieve → Augment Prompt → Answer
  - 文本切分策略：按字符？按语义？重叠多少？
  - 召回质量优化：Top-K、相似度阈值、重排序（Rerank）

- [ ] **Spring AI 的 RAG 支持**
  - `DocumentReader`、`DocumentTransformer`、`VectorStore`
  - `Advisor` 机制：`QuestionAnswerAdvisor`、`RetrievalAugmentationAdvisor`
  - 实战：做一个能读 PDF/Word 的私域问答系统

- [ ] **LangChain4j 的 RAG 支持**
  - `EmbeddingStore`、`EmbeddingStoreIngestor`、`RetrievalAugmentor`
  - 对比 Spring AI 的实现差异

---

## Phase 3：Agent 与工具调用（预计 3-4 周）

**目标**：让 AI 不只是聊天，还能"动手"干活

- [ ] **Function Calling（工具调用）**
  - 为什么需要 Function Calling？
  - 声明 Schema → 模型决策 → 本地执行 → 结果返回
  - Spring AI 的 `@Tool` / `@FunctionBean`
  - LangChain4j 的 `ToolSpecification`
  - 实战：让 AI 能查天气、查数据库、发邮件

- [ ] **Agent 基础概念**
  - 什么是 Agent？ReAct、Plan-and-Execute、AutoGPT 的兴衰
  - 单 Agent vs 多 Agent 架构
  - Java 中可用的 Agent 框架：Spring AI 的 `Agent` 支持、LangGraph4j

- [ ] **Memory 与状态管理**
  - 短期记忆 vs 长期记忆
  - ConversationBufferMemory、滑动窗口、摘要式记忆
  - 用户级别的记忆隔离

---

## Phase 4：工程化与生产落地（持续进行）

**目标**：从 Demo 到可上线的系统

- [ ] **API 设计与并发控制**
  - 流式输出（SSE/WebFlux）的实现
  - Token 限流、并发控制、熔断降级
  - 异步处理与响应优化

- [ ] ** observability**
  - Token 消耗监控
  - Prompt/Response 日志记录与审计
  - 延迟、错误率指标采集

- [ ] **安全与合规**
  - Prompt Injection 攻击与防御
  - 敏感数据过滤与脱敏
  - 输出内容的合规检查

- [ ] **模型选型与成本控制**
  - 不同模型的能力对比与适用场景
  - 多模型路由（简单问题用便宜的，复杂问题用强的）
  - 缓存策略：相似问题直接复用答案

---

## Phase 5：底层原理拓展（长期）

- [ ] Transformer 架构浅析（不推导公式，理解原理）
- [ ] 训练 vs 推理的区别
- [ ] 微调（Fine-tuning）与本地部署（Ollama + Java）
- [ ] 多模态：图像理解、语音识别与 Java 结合

---

## 🏃 当前进度

**正在执行**：Phase 1 - Day 2  
**下一个里程碑**：跑通第一个 Spring AI ChatClient

---

> 路线图不是死计划，会根据实际学习情况调整。  
> 重要的不是多快，而是持续。
