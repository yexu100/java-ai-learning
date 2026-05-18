# Day 01 - 项目初始化与学习路线规划

> 今天是这个学习仓库的第一天。不急着写代码，先把路看清楚。

---

## 📅 日期

2025-05-18（周一）  
Day 01

## 🎯 今日目标

- [x] 确定学习方向：Java + AI 大模型应用开发
- [x] 规划 GitHub 仓库结构
- [x] 设计每日学习记录模板
- [x] 制定 5 阶段学习路线图

## 📖 学习内容

### 为什么要学 AI（从 Java 开发者视角）

**现状**：
- 大模型 API 已经标准化（OpenAI 格式成为事实标准）
- Java 生态在追赶：Spring AI、LangChain4j 逐渐成熟
- 企业需求在爆发：客服机器人、知识库问答、智能助手...

**我的优势**：
- 有后端工程化经验（高并发、微服务、数据库）
- 理解业务系统架构，知道 AI 应该插在哪个环节
- 不需要成为算法专家，**成为 AI 应用工程师**就够了

**学习策略**：
1. 先会用（调 API、写 Prompt）
2. 再理解（RAG、Function Calling 原理）
3. 最后工程化（监控、安全、成本控制）

---

## 💻 代码实践

今日无代码，但创建了一个标准的项目骨架：

```
java-ai-learning/
├── README.md
├── ROADMAP.md
├── CONTRIBUTING.md
├── .gitignore
├── 2025/05/18-day01-init.md  <- 本文件
├── code/
│   ├── spring-ai/
│   ├── langchain4j/
│   └── prompt-engineering/
├── notes/
│   ├── concepts/
│   └── tools/
└── templates/
    └── daily-note-template.md
```

---

## 🐛 踩坑记录

### 问题：没有 gh CLI，如何创建 GitHub 仓库？

**现象**：
环境中没有安装 GitHub CLI (`gh`)，无法通过命令行快速创建仓库。

**解决**：
1. 先在本地初始化 git 仓库
2. 去 GitHub 网页手动创建仓库 `java-ai-learning`
3. 关联远程地址后推送

具体命令见 CONTRIBUTING.md 中的快速启动。

---

## 🤔 思考与疑问

- **疑问**：Spring AI 和 LangChain4j 到底选哪个？
  - 初步想法：两个都学，Spring AI 更贴合 Spring 生态（工作在用），LangChain4j 功能更丰富。先 Spring AI 上手，遇到瓶颈再对比。

- **思考**：公开学习会不会暴露自己的无知？
  - 想通了：开源仓库的意义不是展示完美，而是展示成长。今天的"幼稚"笔记，半年后回看会很有成就感。

---

## 📚 参考资料

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/) - 看了目录结构，很清晰
- [Prompt Engineering Guide](https://www.promptingguide.ai/zh) - 明天开始精读

---

## ✅ 今日完成度

| 目标 | 状态 |
|------|------|
| 确定学习方向 | ✅ 完成 |
| 规划仓库结构 | ✅ 完成 |
| 设计记录模板 | ✅ 完成 |
| 制定路线图 | ✅ 完成 |

**明日计划**：Phase 1 Day 2 - 精读 Prompt Engineering 基础，手调 API 体验不同 Prompt 的效果差异。

---

> 好的开始是成功的一半。更重要的是：别停。
