Feature: 后台管理员认证与鉴权
  作为后台管理端的调用方
  我希望登录、鉴权与令牌维护接口的行为与源码实现保持一致
  以便基于有效 token 对管理端业务接口开展自动化回归

  Background:
    Given 管理端服务地址为 "http://localhost:8080"

  Scenario: 账号密码正确时登录成功并下发 token
    When 使用环境配置的管理端账号和密码登录
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 200
    And 后台响应 data 字段 "token" 应非空
    And 后台响应 data 字段 "tokenHead" 应等于 "Bearer "

  Scenario: 密码错误时返回业务失败
    # 源码：UmsAdminServiceImpl#login 中 Asserts.fail("密码不正确") 抛 ApiException，经 GlobalExceptionHandler 返回 FAILED(500)
    When 使用环境配置的管理端账号和错误密码登录
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 500
    And 后台响应 message 应为 "密码不正确"

  Scenario: 账号不存在时返回 404 且提示不暴露账号是否存在
    # 源码：loadUserByUsername 抛 UsernameNotFoundException 被 login 捕获，token 为 null → validateFailed("用户名或密码错误")
    When 使用不存在的管理端账号登录
    Then 后台响应业务码应为 404
    And 后台响应 message 应为 "用户名或密码错误"

  Scenario: 缺少密码的登录请求被参数校验拦截
    When 提交缺少密码的管理端登录请求
    Then 后台响应业务码应为 404
    And 后台响应 message 应包含 "password"

  Scenario: 携带有效 token 获取当前登录管理员信息
    Given 已使用环境配置的管理端账号登录并取得 token
    When 携带管理端 token 以 GET 请求 "/admin/info"
    Then 后台响应业务码应为 200
    And 后台响应 data 字段 "username" 应等于环境配置的管理端用户名
    And 后台响应 data 中应包含数组 "menus"

  Scenario: 未携带 token 访问受保护接口返回未登录
    When 不携带 token 以 GET 请求管理端 "/admin/list"
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 401
    And 后台响应 message 应为 "暂未登录或token已经过期"

  Scenario: 携带无效 token 访问受保护接口返回未登录
    When 携带无效 token 以 GET 请求管理端 "/admin/list"
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 401
    And 后台响应 message 应为 "暂未登录或token已经过期"

  Scenario: 未携带 token 访问白名单接口 /admin/info 由业务层返回未登录
    When 不携带 token 以 GET 请求管理端 "/admin/info"
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 401
    And 后台响应 message 应为 "暂未登录或token已经过期"

  Scenario: 已登录但无资源权限时访问受保护业务接口返回 403
    # 前置：/admin/register 在白名单，新注册管理员默认无角色、无资源；/brand/list 受 DynamicAuthorizationManager 管控
    When 注册一个无角色的管理端账号并登录取得 token
    Then 后台响应业务码应为 200
    When 携带管理端 token 以 GET 请求 "/brand/list"
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 403
    And 后台响应 message 应为 "没有相关权限"

  Scenario: 携带有效 token 可以刷新 token
    Given 已使用环境配置的管理端账号登录并取得 token
    When 携带管理端 token 以 GET 请求 "/admin/refreshToken"
    Then 后台响应业务码应为 200
    And 后台响应 data 字段 "token" 应非空
    And 后台响应 data 字段 "tokenHead" 应等于 "Bearer "

  Scenario: 携带有效 token 可以登出
    Given 已使用环境配置的管理端账号登录并取得 token
    When 携带管理端 token 以 POST 请求 "/admin/logout"
    Then 后台响应业务码应为 200