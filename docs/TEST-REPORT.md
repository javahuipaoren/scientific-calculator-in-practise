# 测试与运行验证报告

## 1. 验证环境

- 验证日期：2026-09-15
- Java：17.0.8
- Spring Boot：3.3.5
- 构建工具：Maven
- 测试框架：JUnit 5 / Spring Boot Test / MockMvc
- 数据依赖：计算历史和访问日志仅使用 JVM 内存
- 外部接口：无

## 2. 最终构建命令

```bash
mvn -B -ntp \
  -Dmaven.repo.local="$PWD/.m2/repository" \
  clean test package
```

最终构建实际结果：

```text
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 3. 测试分类

| 测试类 | 数量 | 覆盖内容 |
|---|---:|---|
| `LayerDependencyTest` | 2 | 领域层、应用层禁止反向依赖 |
| `CalculationApplicationServiceTest` | 3 | 用例编排、历史淘汰、清空语义 |
| `DomainModelTest` | 2 | 值对象和聚合不变条件 |
| `ExpressionCalculationTemplateTest` | 7 | 运算、函数、微积分、扩展和重复注册 |
| `InMemoryHttpAccessLogStoreTest` | 2 | 日志淘汰、只读快照和清空 |
| `CalculatorControllerTest` | 6 | HTTP、日志、历史、错误输入和请求限制 |
| **合计** | **22** | **0 失败、0 错误、0 跳过** |

## 4. HTTP 访问日志验证

自动化测试验证：

- 调用方传入合法 `X-Request-Id` 时服务继续使用该标识；
- 响应头返回相同的 `X-Request-Id`；
- 请求完成后内存日志包含 requestId、方法、路径和状态码；
- 内存容量达到上限后淘汰最旧记录；
- 返回的日志快照不可修改；
- 清空内存日志不影响已返回的快照。

自动化与 runnable JAR 冒烟测试期间，后台实际输出了以下类型的日志：

```text
level=INFO requestId=sre-smoke-001 ...
message=http_access method=POST path=/api/calculations status=200 ...

level=WARN requestId=sre-smoke-invalid-001 ...
message=http_access method=POST path=/api/calculations status=400 ...

level=WARN requestId=<uuid> ...
message=http_access method=POST path=/api/calculations status=413 ...
```

这证明正常、业务错误和请求体超限调用均被记录。

## 5. 核心数学验证

已自动验证：

- `2 + 3 * 4 - 6 / 2 = 11`
- `10 % 3 = 1`
- `2^3^2 = 512`
- `-2^2 = -4`
- `2^-2 = 0.25`
- `sqrt(9) + sin(30) * 2 = 4`
- `log(100) = 2`
- `ln(e) = 1`
- `integral(x^2, 0, 1) ≈ 1/3`
- `derivative(x^3, 2) ≈ 12`
- `integral(x, 1, 0) ≈ -0.5`

错误路径覆盖除零、负数平方根、未知函数、未知标识符、重复规则注册、非法 JSON
和超大请求体。

## 6. 开闭原则验证

测试中定义了一个仅存在于测试代码中的 `cube` 函数规则，并把它加入
`CalculationRuleRegistry`。没有修改 `ExpressionCalculationTemplate`、
`PrattExpressionParser` 或 `AstEvaluationContext`，表达式 `cube(3) + 1`
成功得到 `28`。

## 7. runnable JAR 冒烟验证

启动命令：

```bash
java -jar target/scientific-calculator-1.0.0.jar \
  --server.port=18081
```

运行态验证包括：

1. 发送带 `X-Request-Id: sre-smoke-001` 的计算请求；
2. 响应头返回相同 requestId；
3. 响应结果为 `516.0`；
4. 后台出现 `http_access` 结构化日志；
5. 非法表达式返回 HTTP 400，并打印 `WARN` 访问日志；
6. 验证结束后停止临时服务进程。

最终实际输出记录在交付目录的 `SERVER-ACCESS-LOG.txt` 和
`SMOKE-TEST.txt`。

## 8. 配置和产物检查

- `src/main/resources` 中只有 `application.yml`；
- 最终 JAR 包含 `BOOT-INF/classes/application.yml`；
- 不包含 `application.properties`；
- 默认访问日志容量为 1000；
- JAR 包含 Spring Boot launcher，可由 `java -jar` 独立启动；
- 源码没有数据库、Redis、AI 模型或第三方 HTTP 接口调用。
