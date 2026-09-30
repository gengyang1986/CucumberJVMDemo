@admin @auth
Feature: 后台管理-管理员登录与鉴权
  后台管理服务（mall-admin）除白名单外所有接口均需 JWT 鉴权。
  白名单 secure.ignored.urls 含：/admin/login、/admin/register、/admin/info、/admin/logout、/minio/upload、/aliyun/oss/policy 等。
  统一返回体：{"code":200,"message":"操作成功","data":{...}}
  返回码约定（ResultCode）：200 成功、404 参数校验失败、401 未登录或 token 过期、403 无权限、500 操作失败。
  登录接口：POST /admin/login，入参 JSON {username, password}，成功时 data 含 token 与 tokenHead。

  Background:
    Given 后台管理服务地址使用测试配置

  @P0 @smoke
  Scenario: 管理员使用正确的用户名和密码登录成功
    When 管理员使用用户名 "admin" 和密码 "123456" 登录后台
    Then 后台登录接口返回码为 200
    And 后台登录响应中的 token 不为空
    And 后台登录响应中的 tokenHead 包含 "Bearer"

  @P0
  Scenario: 管理员使用错误的密码登录失败
    When 管理员使用用户名 "admin" 和密码 "bdd_wrong_password" 登录后台
    Then 后台登录接口返回码为 404
    And 后台登录接口返回消息为 "用户名或密码错误"

  @P0
  Scenario: 携带有效令牌获取当前管理员信息
    Given 后台管理员 "admin" 已登录并成功获取访问令牌
    When 携带已获取的访问令牌请求当前管理员信息接口
    Then 后台信息接口返回码为 200
    And 后台信息接口返回的管理员用户名不为空

  @P0
  Scenario: 未携带令牌访问受保护接口被拒绝
    When 不携带访问令牌请求后台接口 "/product/list"
    Then 后台受保护接口返回码为 401
