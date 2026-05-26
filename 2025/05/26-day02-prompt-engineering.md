# Day 02 - Prompt Engineering 基础

> 对比三种 Prompt 风格（Zero-shot / Few-shot / Chain-of-Thought）对模型回答质量的影响。

---

## 📅 日期

2026-05-26（周二）  
Day 02

## 🎯 今日目标

- [x] 理解 Zero-shot、Few-shot、Chain-of-Thought 三种提示技巧
- [ ] 注册阿里云百炼并获取 API Key
- [ ] 运行 PromptDemo.java 对比三种 Prompt 效果
- [ ] 记录对比结果和心得

## 📖 学习内容

### 核心概念

| 技巧 | 说明 | 适用场景 |
|------|------|----------|
| **Zero-shot** | 直接提问，不给示例 | 简单、通用问题 |
| **Few-shot** | 先给几个问答示例，再问目标问题 | 需要特定格式/风格的输出 |
| **Chain-of-Thought** | 引导模型"一步一步思考" | 复杂推理、数学、逻辑问题 |

### 关键理解

> Prompt 不是"问问题"，而是"编程"。你用自然语言给模型写指令，指令的质量决定了输出的质量。
>
> Zero-shot 最省 token 但最不可控；Few-shot 用示例约束输出格式；CoT 通过分解思考过程提高推理准确性。

---

## 💻 代码实践

### 三种 Prompt 对比实验

```java
// 完整代码见 code/prompt-engineering/PromptDemo.java
// 使用 JDK 内置 HttpURLConnection 调用阿里云百炼 API
```

**编译运行**：
```bash
cd code\prompt-engineering
javac -encoding utf-8 PromptDemo.java
java PromptDemo
```

**代码位置**：`code/prompt-engineering/PromptDemo.java`

---

## 🐛 踩坑记录

### 问题 1：Java 8 不支持文本块

**现象**：`"""` 语法报错
**原因**：文本块（Text Blocks）是 Java 13+ 的特性
**解决**：改用字符串拼接 `"..." + "..."`

### 问题 2：UTF-8 编码问题

**现象**：javac 报"编码GBK的不可映射字符"
**原因**：中文 Windows 默认编码是 GBK，源文件保存为 UTF-8
**解决**：编译时加 `-encoding utf-8`

---

## 🤔 思考与疑问

- Few-shot 给多少个示例最合适？给太多会不会反而让模型困惑？
- Chain-of-Thought 在简单问题上会不会过度思考、答非所问？
- 实际项目中如何根据问题难度动态选择 Prompt 策略？

---

## 📚 参考资料

- [Prompt Engineering Guide 中文](https://www.promptingguide.ai/zh) - Prompt 工程入门必读
- [阿里云百炼 API 文档](https://help.aliyun.com/zh/model-studio/use-api-to-apply-model) - 百炼平台 API 调用方式
- [DashScope Java SDK](https://github.com/aliyun/dashscope-sdk-java) - 官方 Java SDK，生产环境推荐

---

## ✅ 今日完成度

| 目标 | 状态 |
|------|------|
| 理解三种 Prompt 技巧 | ✅ 完成 |
| 编写对比实验代码 | ✅ 完成 |
| 注册阿里云百炼并获取 API Key | ⬜ 待做 |
| 运行实验并对比结果 | ⬜ 待做 |

**明日计划**：完成实验运行，记录对比结果。如果 API Key 已就绪，可以提前进入 Day 3（API 基础）。

---

> 代码已经写好，只差一个 API Key 就能跑了。快去阿里云百炼注册吧！
