# 前台商城会员（mall-portal /sso/**，默认 http://localhost:8085）
# 说明：/sso/** 属于安全白名单，未登录时由 Controller 自行返回业务码 401
# 说明：用例使用“随机手机号 + 随机用户名”注册，避免依赖预置账号；每次运行都会新增会员，建议定期清理 bdd_ 前缀账号

Feature: 前台会员注册与登录
  作为 mall-portal 前台商城的调用方
  我希望验证码、注册、登录与令牌接口行为稳定
  以便购物车与下单等需要登录态的业务可以自动化准备数据

  Background:
    Given 商城端服务地址为 "http://localhost:8085"

  Scenario: 为手机号获取验证码成功
    When 为手机号 "13800000001" 获取验证码
    Then 商城端响应业务码应为 200
    And 商城端响应 message 应为 "获取验证码成功"
    And 商城端响应 data 应为 6 位数字

  Scenario: 使用随机手机号与验证码注册会员后可以直接登录
    When 使用随机手机号与验证码注册会员
    Then 商城端响应业务码应为 200
    And 商城端响应 message 应为 "注册成功"
    When 使用该随机会员账号与密码 "123456" 登录商城端
    Then 商城端响应业务码应为 200
    And 商城端响应 data 字段 "token" 应非空

  Scenario: 验证码错误时注册失败
    When 使用随机手机号与错误验证码 "000000" 注册会员
    Then 商城端响应业务码应为 500
    And 商城端响应 message 应为 "验证码错误"

  Scenario: 会员登录成功返回 token
    When 已注册并登录一个随机会员账号
    Then 商城端响应业务码应为 200
    And 商城端响应 data 字段 "token" 应非空

  Scenario: 会员密码错误时登录失败
    When 已注册并登录一个随机会员账号
    And 使用该随机会员账号与错误密码 "wrong-password" 登录商城端
    Then 商城端响应业务码应为 404
    And 商城端响应 message 应为 "用户名或密码错误"

  Scenario: 未携带 token 获取会员信息返回未登录
    When 不携带 token 以 GET 请求商城端 "/sso/info"
    Then 商城端响应业务码应为 401

  Scenario: 携带会员 token 获取会员信息成功
    When 已注册并登录一个随机会员账号
    And 携带会员 token 以 GET 请求商城端 "/sso/info"
    Then 商城端响应业务码应为 200
    And 商城端响应 data 字段 "phone" 应非空
