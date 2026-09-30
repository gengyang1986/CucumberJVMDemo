Feature: 后台管理员认证与鉴权
  后台 mall-admin 服务（默认 http://localhost:8080）
  作为后台管理员，我需要登录换取 JWT 凭证，并按鉴权规则访问后台接口

  约定（来自源码）：
  - CommonResult 结构为 {code, message, data}；code 语义：200 成功 / 404 参数或凭证错误 / 401 未登录或 token 过期 / 403 无权限 / 500 业务失败
  - RestAuthenticationEntryPoint 与 RestfulAccessDeniedHandler 只写响应体、不设置 HTTP 状态码，因此 HTTP 状态码恒为 200，业务结果一律看响应体 code
  - 请求头 Authorization: Bearer <token>（jwt.tokenHeader=Authorization，jwt.tokenHead="Bearer "）
  - 白名单（secure.ignored.urls）：/admin/login、/admin/register、/admin/info、/admin/logout、/minio/upload、/aliyun/oss/policy
  - 默认管理员账号 admin/macro123，可用 -Dadmin.username / -Dadmin.password 覆盖

  Background:
    Given 后台管理端服务可用

  @P0 @smoke @admin
  Scenario: 管理员使用正确的用户名和密码登录成功
    When 管理员以默认账号登录
    Then 响应业务码应为 200
    And 响应体 "data.token" 不应为空
    And 响应体 "data.tokenHead" 应等于 "Bearer "

  @P0 @admin
  Scenario: 管理员使用错误的密码登录失败
    When 管理员以用户名 "admin" 和密码 "bdd_wrong_password" 登录
    Then 响应业务码应为 404
    And 响应体 "message" 应等于 "用户名或密码错误"

  @P0 @admin
  Scenario: 使用不存在的用户名登录失败
    When 管理员以用户名 "bdd_no_such_admin" 和密码 "macro123" 登录
    Then 响应业务码应为 404
    And 响应体 "message" 应等于 "用户名或密码错误"

  @P0 @smoke @admin
  Scenario: 携带有效 token 获取当前登录管理员信息
    Given 管理员已登录并持有有效 token
    And 请求头携带有效的后台 token
    When 以 GET 请求后台 "/admin/info"
    Then 响应业务码应为 200
    And 响应体 "data.username" 应等于 "admin"
    And 响应体 "data.menus" 应存在

  @P0 @admin
  Scenario: 未携带 token 获取当前管理员信息返回未登录
    Given 请求头不携带 token
    When 以 GET 请求后台 "/admin/info"
    Then 响应业务码应为 401
    And 响应体 "message" 应等于 "暂未登录或token已经过期"

  @P0 @admin
  Scenario: 未携带 token 访问受保护的后台用户列表被拦截
    Given 请求头不携带 token
    When 以 GET 请求后台 "/admin/list"
    Then 响应业务码应为 401

  @P0 @admin
  Scenario: 携带有效 token 分页查询后台用户列表
    Given 管理员已登录并持有有效 token
    And 请求头携带有效的后台 token
    When 以 GET 请求后台 "/admin/list" 参数为：
      | pageNum  | 1 |
      | pageSize | 5 |
    Then 响应业务码应为 200
    And 响应体 "data.list" 不应为空
    And 响应体 "data.pageNum" 应等于 "1"

  @P0 @admin
  Scenario: 刷新有效的后台 token
    Given 管理员已登录并持有有效 token
    And 请求头携带有效的后台 token
    When 以 GET 请求后台 "/admin/refreshToken"
    Then 响应业务码应为 200
    And 响应体 "data.token" 不应为空

  @P0 @admin
  Scenario: 管理员登出成功
    Given 管理员已登录并持有有效 token
    And 请求头携带有效的后台 token
    When 以 POST 无参请求后台 "/admin/logout"
    Then 响应业务码应为 200

  @P1 @admin
  Scenario: 使用过期或伪造的 token 访问受保护接口返回未登录
    Given 请求头不携带 token
    When 使用伪造 token "Bearer bdd.invalid.token" 以 GET 请求后台 "/admin/list"
    Then 响应业务码应为 401
