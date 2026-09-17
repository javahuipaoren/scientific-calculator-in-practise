# 科学计算器 HTTP 后端

本项目使用 Java 17、Spring Boot 3、Maven 和 JUnit 5，实现纯内存、无外部
接口依赖的科学计算器服务。项目采用 DDD 四层结构，并使用“计算模板 + Pratt
解析器 + AST + 策略规则注册表”解耦计算流程和具体数学规则。

## 1. 快速启动

```bash
java -jar scientific-calculator-1.0.0.jar
```

默认监听 `127.0.0.1:8080`。可通过启动参数覆盖：

```bash
java -jar scientific-calculator-1.0.0.jar \
  --server.address=0.0.0.0 \
  --server.port=9090
```

无需安装 MySQL、Redis 或其他中间件。

## 2. HTTP API

### 2.1 执行计算

```http
POST /api/calculations
Content-Type: application/json
```

请求示例：

```json
{
  "expression": "sqrt(9) + sin(30) * 2"
}
```

响应示例：

```json
{
  "id": 1,
  "expression": "sqrt(9) + sin(30) * 2",
  "result": 4.0,
  "createdAt": "2026-09-15T09:00:00Z"
}
```

### 2.2 查询历史

```http
GET /api/calculations
```

返回当前 JVM 进程中的计算历史，最多保留 1000 条；服务重启后历史清空。

### 2.3 清空历史

```http
DELETE /api/calculations
```

成功返回 HTTP `204 No Content`。

## 3. 支持的表达式

### 运算符

| 类型 | 语法 | 说明 |
|---|---|---|
| 加减 | `+`、`-` | 左结合 |
| 乘除、取模 | `*`、`/`、`%` | 左结合 |
| 幂 | `^` | 右结合，如 `2^3^2 = 512` |
| 一元正负号 | `+x`、`-x` | `-2^2 = -4` |

### 函数与常量

- 基础函数：`sqrt`、`abs`、`floor`、`ceil`
- 三角函数：`sin`、`cos`、`tan`，输入采用度数制
- 对数和指数：`log`、`ln`、`exp`
- 常量：`pi`、`e`
- 科学计数法：`1e-3`、`2E2`

### 数值微积分

```text
integral(x^2, 0, 1)     # 约等于 1/3
derivative(x^3, 2)      # 约等于 12
```

`integral(expression, lower, upper)` 使用 Simpson 法；
`derivative(expression, point)` 使用中心差分。变量名固定为 `x`，两者均为数值
近似，不是符号微积分。

## 4. DDD 与可扩展计算架构

```text
interfaces  ->  application  ->  domain
                      ^
                      |
               infrastructure
```

- `interfaces`：HTTP 协议、DTO、参数校验、统一异常响应
- `application`：计算用例编排、领域对象转换
- `domain`：聚合根、值对象、计算模板、AST、策略接口和数学规则
- `infrastructure`：内存仓储、ID 生成器、Spring 组合根

核心计算流程类似 `JdbcTemplate`：

```text
MathematicalExpression
  -> PrattExpressionParser
  -> ExpressionNode AST
  -> AstEvaluationContext
  -> CalculationRuleRegistry
  -> BinaryOperatorRule / UnaryOperatorRule / FunctionRule / ConstantRule
  -> CalculationResult
```

`ExpressionCalculationTemplate` 只维护稳定流程，不包含加减乘除、三角函数或
微积分实现。每条数学规则由独立策略类承担，从而符合单一职责、开闭和依赖倒置原则。

## 5. 新增计算规则

以新增 `cube(x)` 为例：

1. 新建类并实现 `FunctionRule`；
2. 在策略中声明名称、参数校验和运算逻辑；
3. 在基础设施组合根 `CalculatorBeanConfiguration` 的 `@Import` 中注册该类；
4. 无需修改 `ExpressionCalculationTemplate`、解析器或 AST 求值流程。

函数策略接收未求值的 `ExpressionNode` 参数和 `EvaluationContext`。因此策略既能
实现普通函数，也能绑定变量并重复计算同一 AST，从而支持积分、导数等高级扩展。

新增运算符时实现 `BinaryOperatorRule` 或 `UnaryOperatorRule`，同时声明符号、
优先级和结合方向；Pratt 解析器会从注册表读取元数据，无需新增运算符分支。

## 6. 配置

项目仅使用 YAML 配置：

```text
src/main/resources/application.yml
```

其中包含监听地址、端口、Tomcat 资源上限、Jackson 严格反序列化选项、
访问日志容量和控制台日志格式。项目中不存在 `application.properties`。

## 7. HTTP 访问日志与链路标识

所有外部 HTTP 请求完成后，后台都会输出一条结构化访问日志：

```text
2026-09-15T18:55:00.123+08:00 level=INFO requestId=demo-001 \
logger=c.e.c.i.logging.HttpAccessLogFilter \
message=http_access method=POST path=/api/calculations status=200 \
durationMs=12 clientIp=127.0.0.1
```

日志规则：

- `2xx/3xx` 使用 `INFO`；
- `4xx` 使用 `WARN`；
- `5xx` 使用 `ERROR`；
- 接受合法的 `X-Request-Id`，未提供时由服务生成 UUID；
- 响应始终返回 `X-Request-Id`，便于调用方和后台日志关联；
- 不记录请求体、响应体、表达式、鉴权头和查询参数；
- 同时在 JVM 内存中保留最近 1000 条日志，超出后淘汰最旧记录；
- 内存日志不通过未鉴权 REST 接口暴露，避免泄露运维信息。

调用示例：

```bash
curl -i -X POST 'http://127.0.0.1:8080/api/calculations' \
  -H 'Content-Type: application/json' \
  -H 'X-Request-Id: calculator-demo-001' \
  -d '{"expression":"2^3^2 + sqrt(16)"}'
```

容量可通过 YAML 或启动参数调整：

```bash
java -jar scientific-calculator-1.0.0.jar \
  --calculator.access-log.capacity=2000
```

## 8. 构建与测试

```bash
mvn -B -ntp \
  -Dmaven.repo.local="$PWD/.m2/repository" \
  clean test package
```

生成：

```text
target/scientific-calculator-1.0.0.jar
```

测试覆盖领域模型、运算优先级、策略扩展、微积分、应用服务、DDD 依赖方向和
HTTP 接口。

## 9. 安全和资源边界

- 表达式最大 512 字符；
- 单次 HTTP 请求体最大 16 KiB；
- 表达式嵌套深度最大 64；
- 历史记录最大 1000 条；
- 内存访问日志默认最大 1000 条；
- 访问日志不记录请求体和敏感数据；
- 计算结果和中间结果必须为有限 `double`；
- 默认仅监听 `127.0.0.1`；
- 全部数据只保存在当前 JVM 内存中。

## 10. AI 辅助声明

需求拆解、架构方案、代码、测试和文档初稿由 AI 辅助生成；AI 同时执行了编译、
自动化测试和运行态验证。候选人应在提交前独立复核领域边界、数学精度、异常协议、
资源限制与测试覆盖，并在 `docs/DELIVERY.md` 的个人复核表中记录自己的判断和修改。
