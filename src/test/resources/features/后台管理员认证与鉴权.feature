# 后台管理端认证与鉴权（mall-admin，默认 http://localhost:8080）
# 业务码约定：200 成功 / 404 参数校验失败或凭证错误 / 401 未登录或 token 失效 / 403 无权限 / 500 业务失败
# HTTP 状态码约定：被安全过滤器拦截时仍返回 200，业务码体现在响应体 code 字段
# 前置假设：环境已初始化 mall.sql 演示数据，管理端账号 admin 具备 /admin/** 资源权限

Feature: 后台管理员认证与鉴权
  作为 mall-admin 的调用方
  我希望登录、鉴权与令牌维护接口行为稳定
  以便管理端其它业务接口都能基于有效 token 做自动化回归

  Background:
    Given 管理端服务地址为 "http://localhost:8080"

  Scenario: 正确的账号密码可以登录并返回 token
    When 使用管理端账号 "admin" 和密码 "macro123" 登录
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 200
    And 后台响应 data 字段 "token" 应非空
    And 后台响应 data 字段 "tokenHead" 应等于 "Bearer "

  Scenario: 密码错误时登录失败
    When 使用管理端账号 "admin" 和错误密码 "wrong-password" 尝试登录
    Then 后台响应业务码应为 404
    And 后台响应 message 应为 "用户名或密码错误"

  Scenario: 携带有效 token 可以获取当前登录管理员信息
    Given 已使用管理端账号 "admin" 登录并取得 token
    When 携带管理端 token 以 GET 请求 "/admin/info"
    Then 后台响应业务码应为 200
    And 后台响应 data 字段 "username" 应等于 "admin"
    And 后台响应 data 字段 "menus" 应非空

  Scenario: 未携带 token 访问受保护接口返回未登录
    When 不携带 token 以 GET 请求管理端 "/admin/list"
    Then 后台响应业务码应为 401
    And 后台响应 message 应为 "暂未登录或token已经过期"

  Scenario: 携带无效 token 访问受保护接口返回未登录
    When 携带无效 token 以 GET 请求管理端 "/admin/list"
    Then 后台响应业务码应为 401

  Scenario: 未携带 token 访问白名单接口 /admin/info 由业务层返回未登录
    When 不携带 token 以 GET 请求管理端 "/admin/info"
    Then 后台响应 HTTP 状态码应为 200
    And 后台响应业务码应为 401

  Scenario: 携带有效 token 可以刷新 token
    Given 已使用管理端账号 "admin" 登录并取得 token
    When 携带管理端 token 以 GET 请求 "/admin/refreshToken"
    Then 后台响应业务码应为 200
    And 后台响应 data 字段 "token" 应非空

  Scenario: 携带有效 token 可以登出
    Given 已使用管理端账号 "admin" 登录并取得 token
    When 携带管理端 token 以 POST 请求 "/admin/logout"
    Then 后台响应业务码应为 200

  Scenario: 携带有效 token 可分页查询管理员列表
    Given 已使用管理端账号 "admin" 登录并取得 token
    When 携带管理端 token 以 GET 请求 "/admin/list?pageNum=1&pageSize=5"
    Then 后台响应业务码应为 200
    And 后台响应 data 中应包含非空数组 "list"
    And 后台响应 data 字段 "total" 应非空
