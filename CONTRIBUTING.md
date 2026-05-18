# 提交规范与每日学习流程

> 这是写给自己看的规范。目的是让仓库保持清晰、可追溯、有节奏感。

---

## 📅 每日学习流程

### Step 1：创建今日笔记

```bash
# 文件名格式：年/月/日-序号-主题.md
# 示例：2025/05/18-day01-init.md
```

复制 `templates/daily-note-template.md` 到对应日期路径，开始填写。

### Step 2：学习与记录

- 边学边记，不要等学完了再补
- 代码要可运行，贴进笔记前先本地跑通
- 踩坑必须记录，比"学到了什么"更有价值

### Step 3：整理代码（如果有）

- 代码放入 `code/` 下对应模块
- 每个示例都要有独立的 `README.md` 说明如何运行
- 不要上传 API Key！用 `application-template.yml` 代替

### Step 4：提交

```bash
git add .
git commit -m "2025-05-18: Spring AI 项目初始化与第一个 ChatClient"
git push origin main
```

---

## 📝 Commit Message 规范

格式：`<date>: <简要描述>`

- ✅ `2025-05-18: 学习 Prompt Engineering 基础，添加 Few-shot 示例`
- ✅ `2025-05-20: 完成 Spring AI ChatClient 入门，添加天气查询 Demo`
- ❌ `update`（太 vague）
- ❌ `fix bug`（没有上下文）
- ❌ `2025/5/18`（日期格式不统一）

**特殊情况**：
- 修改旧笔记：`2025-05-18: 补充 RAG 流程图`
- 纯文档更新：`docs: 更新 resources.md，添加智谱 API 文档链接`

---

## 📁 文件命名规范

| 类型 | 命名示例 | 说明 |
|------|---------|------|
| 每日笔记 | `2025/05/18-day01-init.md` | 年/月/日-序号-主题 |
| 代码项目 | `code/spring-ai/hello-assistant/` | 框架/功能模块/项目名 |
| 概念笔记 | `notes/concepts/embedding.md` | 专题知识，长期维护 |
| 工具笔记 | `notes/tools/openai-api.md` | 平台/工具使用备忘 |

---

## 🔒 安全红线

- **绝不提交 API Key**：`.gitignore` 已配置 `application.yml`，请使用 `application-template.yml`
- **敏感信息脱敏**：笔记中如有公司内网地址、私有数据，用 `xxx` 或 `***` 替换
- **截图检查**：截图中的 Key、Token、密码要打码

---

## 📊 周回顾（每周日）

花 15 分钟做周回顾：

1. 更新 `README.md` 中的"学习统计"表格
2. 检查本周笔记是否完整，有无遗漏的代码未提交
3. 在本周最后一天笔记末尾添加"周小结"段落
4. 根据进度调整下周计划（可以更新 ROADMAP.md）

---

## 🚀 快速启动（每天复制粘贴）

```bash
# 1. 进入项目
cd java-ai-learning

# 2. 拉取最新（如果在多台设备上）
git pull origin main

# 3. 创建今日笔记
cp templates/daily-note-template.md 2025/05/18-day01-init.md

# 4. 写笔记、写代码...

# 5. 提交
git add .
git commit -m "2025-05-18: 你的描述"
git push origin main
```

---

> 规范是工具，不是负担。如果某天只想随便记两句，也可以，但尽量保持节奏。
