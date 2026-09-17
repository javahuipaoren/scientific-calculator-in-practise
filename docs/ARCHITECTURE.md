# 架构设计说明

## 1. 架构目标

项目需要同时满足以下目标：

1. 业务模型和技术框架解耦；
2. HTTP、应用编排、计算领域和内存实现职责清晰；
3. 新增函数、运算符或微积分规则时，不修改核心计算流程；
4. 所有状态进程内保存，单个 runnable JAR 可直接运行；
5. 代码具有中文注释、稳定异常语义和自动化测试。

## 2. DDD 分层与依赖方向

```text
com.example.calculator
├── interfaces
│   └── rest               # Controller、DTO、异常处理、请求过滤器
├── application
│   ├── command            # 应用命令
│   ├── result             # 应用只读视图
│   └── service            # 用例编排
├── domain
│   ├── model              # 聚合根和值对象
│   ├── service            # 领域服务端口
│   ├── repository         # 仓储端口
│   ├── expression         # 表达式 AST
│   ├── engine             # 解析、注册、模板、求值上下文
│   └── rule               # 策略接口及内置数学规则
└── infrastructure
    ├── persistence        # 内存仓储、原子 ID 生成器
    └── config             # Spring 组合根
```

合法依赖方向：

```text
interfaces -> application -> domain
infrastructure -----------> domain/application
```

`LayerDependencyTest` 扫描 import，阻止领域层依赖 Spring、Jakarta、接口层或
基础设施层，也阻止应用层反向依赖外层。

## 3. 领域模型

- `Calculation`：一次计算记录的聚合根；
- `CalculationId`：正整数记录标识值对象；
- `MathematicalExpression`：表达式值对象，维护非空和长度不变条件；
- `CalculationResult`：只接受有限 `double` 的结果值对象；
- `ExpressionCalculator`：表达式计算领域服务端口；
- `CalculationHistoryRepository`：计算历史仓储端口；
- `CalculationIdGenerator`：标识生成端口。

应用服务只负责编排：创建表达式、调用领域服务、创建聚合、保存历史和输出视图。

## 4. 计算引擎设计

### 4.1 原设计问题

把全部运算写入单一计算类会同时承担词法识别、语法解析、优先级判断、函数分派、
定义域校验和具体数学公式，产生以下问题：

- 违反单一职责原则；
- 新增规则需要修改大量 `switch` 或条件分支，违反开闭原则；
- 核心流程直接依赖具体运算，违反依赖倒置原则；
- 单类持续膨胀，测试难以隔离。

### 4.2 Template + Strategy + Registry

优化后由 `ExpressionCalculationTemplate` 固定通用流程：

```text
校验后的表达式
  -> Pratt 解析
  -> AST
  -> AST 求值上下文
  -> 查询规则注册表
  -> 委托策略
  -> 结果值对象
```

可插拔策略接口：

- `BinaryOperatorRule`：二元运算符、优先级、结合方向和计算行为；
- `UnaryOperatorRule`：前缀一元运算符；
- `FunctionRule`：函数规则；
- `ConstantRule`：数学常量；
- `CalculationRuleRegistry`：规则注册、重复校验和运行时查询。

它与 `JdbcTemplate` 的思想相似：模板持有稳定算法骨架，变化部分由策略或回调提供。

### 4.3 Pratt 解析器

`PrattExpressionParser` 从注册表动态读取运算符的优先级和结合方向，而不是硬编码
每一个运算符分支。当前约定如下：

| 运算符 | 优先级 | 结合方向 |
|---|---:|---|
| `+ -` | 10 | 左结合 |
| `* / %` | 20 | 左结合 |
| 一元 `+ -` | 25 | 前缀 |
| `^` | 30 | 右结合 |

因此可以稳定支持：

```text
2^3^2 = 512
-2^2 = -4
2^-2 = 0.25
```

### 4.4 AST 与求值上下文

解析结果由 `NumberNode`、`IdentifierNode`、`UnaryOperationNode`、
`BinaryOperationNode` 和 `FunctionCallNode` 组成。

`AstEvaluationContext` 负责遍历稳定 AST，并把具体运算委派给注册表。上下文支持
`withVariable(name, value)` 创建不可变变量绑定副本，使策略可以对同一 AST 使用
不同变量值重复求值。

### 4.5 微积分扩展示例

`FunctionRule` 接收未求值参数而不是预先计算后的 `double`：

```java
double apply(List<ExpressionNode> arguments, EvaluationContext context);
```

因此：

- `DefiniteIntegralFunctionRule` 可对被积表达式反复绑定不同 `x`，使用 Simpson 法；
- `DerivativeFunctionRule` 可在目标点两侧绑定 `x`，使用中心差分。

新增这两项能力没有修改计算模板、解析器和 AST 求值器，直接证明扩展点有效。

## 5. SOLID 对应关系

- **S：单一职责**：解析、树遍历、规则查找、具体公式、持久化分别由独立类负责；
- **O：开闭原则**：增加规则主要通过实现小接口和注册 Bean 完成；
- **L：里氏替换**：所有同类规则均可通过对应接口替换；
- **I：接口隔离**：二元、一元、函数、常量使用四个小接口；
- **D：依赖倒置**：模板依赖规则抽象，应用层依赖领域端口，仓储实现位于外层。

组合根增加一条策略注册记录属于依赖装配变化，不是核心计算流程修改。

## 6. 内存与并发设计

- `InMemoryCalculationHistoryRepository` 使用有界双端队列；
- 同一把锁保护写入、清空和快照读取；
- 达到 1000 条后淘汰最旧记录；
- `AtomicCalculationIdGenerator` 使用原子序列；
- 规则注册表构造后不可变，可安全共享；
- 解析器和求值上下文按请求创建，不共享可变解析状态。

## 7. Spring 组合根与 YAML

领域策略类不使用 Spring 注解。`CalculatorBeanConfiguration` 位于基础设施层，通过
`@Import` 注册内置策略，并将四类规则列表注入 `CalculationRuleRegistry`。

服务参数全部位于 `src/main/resources/application.yml`，包括：

- 服务地址和端口；
- Tomcat 线程、连接和超时限制；
- Spring 应用名称；
- Jackson 严格输入选项；
- 错误信息和堆栈隐藏策略；
- 访问日志容量、日志级别和结构化控制台格式。

## 8. SRE 可观测性设计

`HttpAccessLogFilter` 位于基础设施层，作为最外层 Servlet Filter 覆盖正常请求、
业务错误、非法 JSON 和超大请求等所有外部调用。它不会污染领域模型和应用用例。

每条访问日志记录：

- `requestId`；
- HTTP 方法；
- 不含查询参数的请求路径；
- 响应状态码；
- 处理耗时；
- 客户端地址；
- 完成时间。

调用方可通过 `X-Request-Id` 传递链路标识。服务会校验其字符集合和最大长度，
非法或缺失时生成 UUID，并在响应头中返回最终标识。MDC 将该标识附加到请求线程
内的其他日志，便于一次调用的日志关联。

日志按状态码分级：成功请求为 `INFO`，客户端错误为 `WARN`，服务端错误为
`ERROR`。日志采用单行键值格式，方便标准输出被容器或日志采集平台解析。

`InMemoryHttpAccessLogStore` 使用加锁的有界双端队列保留最近日志。日志只按时间
追加，不存在按键访问，因此“容量满时淘汰最旧记录”的滚动策略比传统 LRU 更符合
访问日志语义。默认容量 1000，可通过 YAML 调整。内存日志没有公开查询接口，避免
在无认证的示例服务中泄露客户端地址等运维信息。

为遵守最小披露原则，访问日志不保存请求体、响应体、表达式、鉴权信息或查询参数。
