# 知识关系最短证据路径

> 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。

## 已实现

按指定关系类型和最大跳数查找有向最短路径，返回逐条关系证据，避免把所有关联当作直接事实。

最多200节点、500关系、20跳；按边编号稳定BFS，只沿允许的有向关系。source=target可返回0跳路径。节点/边必须是调用方已获授权的数据，关系筛选不是ACL，不读取外部图数据库。

## 入口与身份

从原工作台点击“专项业务工具”，或访问同源`/practical.html`。原页面与架构保留，不替换为新框架。

轻量H5项目没有登录体系，本新增接口默认关闭（503）。部署时以环境变量ZHUATECH_PRACTICAL_TOKEN配置独立32–256位可打印ASCII令牌，然后在专项页面填写该令牌；令牌只保留在本页内存。Compose已透传该变量。不要把真实令牌写入前端、示例文件或Git。旧演示接口不因此变成生产授权体系。

参考根目录.env.practical.example。可使用`openssl rand -hex 32`在部署机器上生成随机令牌；不要把生成结果粘贴到公开文档。启动Compose前在本地未提交的.env或部署环境中配置此变量。直接运行Java时同样通过环境变量注入。此共享令牌仅保护新增纯计算端点，不替代企业用户、角色与租户权限体系。

接口`POST /api/practical/typed-shortest-path`：JSON请求，`Authorization: Bearer <配置令牌或当前会话>`。200返回`success:true,data`；凭据缺失/无效401；参数错误400；未配置/错误配置服务令牌503。不读取其他租户记录、不读取文件、不执行命令、不上传第三方模型、不实际审批/发布/发信/记账。

## 合成样例

```json
{
  "source": "A",
  "target": "C",
  "maxDepth": 3,
  "allowedTypes": [
    "USES",
    "FEEDS"
  ],
  "nodes": [
    "A",
    "B",
    "C"
  ],
  "edges": [
    {
      "id": "E1",
      "from": "A",
      "to": "B",
      "type": "USES"
    },
    {
      "id": "E2",
      "from": "B",
      "to": "C",
      "type": "FEEDS"
    },
    {
      "id": "E3",
      "from": "A",
      "to": "C",
      "type": "PRIVATE"
    }
  ]
}
```

## 参数与显示限制

- 页面按UTF-8字节限制128 KiB，超时15秒，无离线成功兜底，后端异常会直接提示。
- 后端校验最大12层、10000节点、总文本128000字符、单文本64000字符；通用数值≤30位有效数字、尺度绝对值≤12。各业务数组和文本有更小上限，详见实现。
- 日期限定1900–2200；金额只接受非负且最多2位小数；不暗中换算币种或忽略非法记录。
- 字段未知、标识重复和错误引用按各业务规则明确拒绝；所有结果来自本次输入，不改变输入对象。
- 结果表预览最多100行；JSON导出包含完整数据。
- 返回字符串使用DOM.textContent显示，来源文字不注入HTML。真实令牌不写浏览器持久存储、不进入导出文件。
- 服务端校验在JSON解析之后；生产网关仍需HTTPS、请求体上限、速率限制、日志脱敏和明确的访问控制，不能用这些校验替代网关防护。

## 测试与部署

Java21：`cd backend && mvn test`，包含原有用例、新算法单元和真实随机端口HTTP测试。新增接口测试覆盖匿名/错误凭据、有效调用、参数缺失、未知字段、非法JSON、数组根请求及错误认证协议；另有默认关闭与短令牌拒绝用例。

前端：`node --test frontend/test/practical-format.test.mjs`；该项目为无需打包的原生H5，检查新增JS语法并通过HTTP静态服务/原Nginx部署；无需npm安装或构建。

本次未增加数据库迁移。5个业务项目沿用原MySQL部署及初始化方式；轻量H5保持原有演示边界，不因此被描述成完整企业生产系统。完整执行结果以本批交付报告为准，不以本说明代替测试证据。

## Scope

Typed shortest evidence path is implemented as a deterministic computation over caller-provided, authorized data. This endpoint does not call external inference, fetch URLs, inspect private files or persist workflow decisions. Algorithm-specific limits above remain part of its contract. Existing source-available/non-commercial licensing, website and contacts are preserved.

## 既有接口回归修复

原实体合并接口在省略可选权威标识时会出现空指针。本次将缺失标识视为未提供证据，不获得权威标识分；名称相似度缺失则明确校验，HTTP返回400而非服务器异常。新增3个服务回归及1个真实HTTP用例。此修复没有引入自动实体写入或撤销功能。

### 新接口JSON精度防护

新增接口单独使用BigDecimal解析小数，拒绝重复JSON字段和尾随JSON对象；不修改旧接口的全局解析配置。数字在业务计算前统一校验精度/尺度，极端指数不能先下溢成0再进入金额或数量计算。前端只检查JSON语法和根对象，发送用户输入原文，不对数值重编码；后端响应内的BigDecimal统一以十进制字符串表示，避免浏览器解析和导出时丢失金额尾数。整数计数和布尔字段仍保持原类型。
