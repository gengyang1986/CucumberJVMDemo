Feature: 后台管理员认证与鉴权
  作为 mall-admin 的调用方
  我希望登录、鉴权与令牌维护接口行为稳定
  以便管理端其它业务接口都能基于有效 token 做自动化回归

  Background:
    Given 管理端测试环境配置已就绪

  @smoke
  Scenario: 正确的账号密码可以登录并返回 token
    When 使用环境配置的管理端账号登录
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 200
    And 后台响应 data 字段 "token" 应非空
    And 后台响应 data 字段 "tokenHead" 应以 "Bearer" 开头

  Scenario: 密码错误时登录失败并提示密码不正确
    When 使用环境配置的管理端账号搭配错误密码 "wrong-password" 登录
    Then 后台响应业务码应为 500
    And 后台响应 message 应为 "密码不正确"

  @risk-account-enumeration
  Scenario: 账号不存在时登录失败并提示用户名或密码错误
    # 锁定当前实现：账号不存在走 UsernameNotFoundException -> login 返回 null -> validateFailed(404)，
    # 与密码错误的 500 提示不同，可用于探测账号是否存在；需求若要求统一提示需同步调整本场景。
    When 使用不存在的管理端账号登录
    Then 后台响应业务码应为 404
    And 后台响应 message 应为 "用户名或密码错误"

  Scenario: 未提交密码字段时登录参数校验失败
    When 使用环境配置的管理端账号但不提交密码字段登录
    Then 后台响应业务码应为 404
    And 后台响应 message 应非空

  Scenario: 未携带 token 访问非白名单接口返回未登录
    When 不携带 token 以 GET 请求管理端接口 "/admin/list"
    Then 后台响应业务码应为 401

  Scenario: 携带无效 token 访问非白名单接口返回未登录
    When 携带无效 token 以 GET 请求管理端接口 "/admin/list"
    Then 后台响应业务码应为 401

  Scenario: 未携带 token 访问白名单接口 admin/info 由业务层返回未登录
    When 不携带 token 以 GET 请求管理端接口 "/admin/info"
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 401
    And 后台响应 message 应为 "暂未登录或token已经过期"

  Scenario: 携带有效 token 可以获取当前登录管理员信息
    Given 已使用环境配置的管理端账号登录并取得 token
    When 携带管理端 token 以 GET 请求管理端接口 "/admin/info"
    Then 后台响应业务码应为 200
    And 后台响应 data 字段 "username" 应等于环境配置的管理端用户名
    And 后台响应 data 字段 "menus" 应为数组

  Scenario: 携带有效 token 可以刷新 token
    Given 已使用环境配置的管理端账号登录并取得 token
    When 携带管理端 token 以 GET 请求管理端接口 "/admin/refreshToken"
    Then 后台响应业务码应为 200
    And 后台响应 data 字段 "token" 应非空

  Scenario: 携带有效 token 可以登出
    Given 已使用环境配置的管理端账号登录并取得 token
    When 携带管理端 token 以 POST 请求管理端接口 "/admin/logout"
    Then 后台响应业务码应为 200