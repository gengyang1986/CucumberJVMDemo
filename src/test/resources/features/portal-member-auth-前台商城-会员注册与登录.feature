@portal @member
Feature: 前台商城-会员注册与登录
  会员相关接口均在 /sso/** 白名单内（无需令牌即可访问 register/login/getAuthCode）：
    GET  /sso/getAuthCode?telephone=xxx                            获取验证码，响应 data 即验证码
    POST /sso/register?username=&password=&telephone=&authCode=    会员注册
    POST /sso/login?username=&password=                            会员登录，成功时 data.token 为访问令牌
    GET  /sso/info                                                 获取当前会员信息（需 Authorization: Bearer <token>）
  验证码存于 Redis key ums:authCode，有效期 90 秒（redis.expire.authCode）。

  Background:
    Given 前台商城服务地址使用测试配置

  @P0 @smoke
  Scenario: 新会员获取验证码后注册成功并可以登录
    When 会员为随机生成的手机号获取验证码
    Then 会员接口返回码为 200
    And 验证码响应消息为 "获取验证码成功"
    When 会员使用该手机号、随机生成的用户名和密码 "123456" 完成注册
    Then 会员接口返回码为 200
    And 会员注册响应消息为 "注册成功"
    When 会员使用该随机生成的用户名和密码 "123456" 登录
    Then 会员接口返回码为 200
    And 会员登录响应中的 token 不为空

  @P0
  Scenario: 已存在的会员使用正确的用户名和密码登录成功
    When 会员使用配置中的会员账号和密码登录
    Then 会员接口返回码为 200
    And 会员登录响应中的 token 不为空

  @P0
  Scenario: 会员使用错误的密码登录失败
    When 会员使用配置中的会员账号和错误密码 "bdd_wrong_password" 登录
    Then 会员接口返回码为 404
    And 会员登录响应消息为 "用户名或密码错误"

  @P0
  Scenario: 携带令牌获取当前会员信息
    Given 会员已使用配置中的会员账号登录并获取访问令牌
    When 携带会员令牌请求会员信息接口
    Then 会员接口返回码为 200
    And 会员信息接口返回的会员名等于配置中的会员账号
