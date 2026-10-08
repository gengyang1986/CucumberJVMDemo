Feature: 前台购物车管理
  前台 mall-portal 服务的 /cart/**，需要会员登录（Authorization: Bearer <会员token>）

  约定（来自源码）：
  - 会员身份由 token 解析，add 接口忽略请求体中的 memberId，由服务端用当前会员覆盖
  - POST /cart/add 请求体为 OmsCartItem JSON；返回 data 为受影响行数（成功为 1）
  - GET /cart/update/quantity 的 id 是购物车条目 id（oms_cart_item.id），不是商品 id
  - POST /cart/delete 的 ids 为购物车条目 id 列表；POST /cart/clear 清空当前会员购物车
  - clear/delete/updateQuantity 在受影响行数为 0 时返回 code=500「操作失败」（当前实现，购物车为空时 clear 即为此结果）
  - 每个场景使用新注册的随机会员，购物车天然隔离

  Background:
    Given 前台商城端服务可用
    And 已注册并登录的测试会员

  @P0 @smoke @cart
  Scenario: 添加商品到购物车成功
    Given 请求头携带有效的会员 token
    And 已清空当前会员购物车
    When 添加商品 "1" 规格 "1" 数量 "1" 到购物车
    Then 响应业务码应为 200
    And 响应体 "data" 应等于 "1"

  @P0 @smoke @cart
  Scenario: 购物车列表包含已添加的商品
    Given 请求头携带有效的会员 token
    And 已清空当前会员购物车
    And 已添加商品 "1" 规格 "1" 数量 "1" 到购物车
    When 以 GET 请求前台 "/cart/list"
    Then 响应业务码应为 200
    And 响应体 "data" 不应为空
    And 购物车列表中应包含商品 "1" 并记录其条目 id

  @P0 @cart
  Scenario: 查询购物车促销信息
    Given 请求头携带有效的会员 token
    And 已清空当前会员购物车
    And 已添加商品 "1" 规格 "1" 数量 "1" 到购物车
    When 以 GET 请求前台 "/cart/list/promotion"
    Then 响应业务码应为 200
    And 响应体 "data" 不应为空

  @P0 @cart
  Scenario: 修改购物车中商品的数量
    Given 请求头携带有效的会员 token
    And 已清空当前会员购物车
    And 已添加商品 "1" 规格 "1" 数量 "1" 到购物车
    When 将购物车条目数量修改为 3
    Then 响应业务码应为 200

  @P0 @cart
  Scenario: 获取购物车中指定商品的规格用于重选
    Given 请求头携带有效的会员 token
    And 已清空当前会员购物车
    And 已添加商品 "1" 规格 "1" 数量 "1" 到购物车
    When 以 GET 请求前台 "/cart/getProduct/1"
    Then 响应业务码应为 200
    And 响应体 "data" 应存在
    And 响应体 "data.productId" 应等于 "1"

  @P0 @cart
  Scenario: 修改购物车中商品的规格
    Given 请求头携带有效的会员 token
    And 已清空当前会员购物车
    And 已添加商品 "1" 规格 "1" 数量 "1" 到购物车
    When 将购物车条目的规格改为当前商品的 "1" 号规格
    Then 响应业务码应为 200

  @P0 @cart
  Scenario: 删除购物车中的指定商品
    Given 请求头携带有效的会员 token
    And 已清空当前会员购物车
    And 已添加商品 "1" 规格 "1" 数量 "1" 到购物车
    When 删除购物车中已记录的商品条目
    Then 响应业务码应为 200

  @P0 @cart
  Scenario: 清空当前会员的购物车
    Given 请求头携带有效的会员 token
    And 已清空当前会员购物车
    And 已添加商品 "1" 规格 "1" 数量 "1" 到购物车
    When 以 POST 无参请求前台 "/cart/clear"
    Then 响应业务码应为 200
    When 以 GET 请求前台 "/cart/list"
    Then 响应业务码应为 200
    And 响应体 "data" 应为空数组

  @P0 @cart
  Scenario: 未携带 token 访问购物车列表返回未登录
    Given 请求头不携带 token
    When 以 GET 请求前台 "/cart/list"
    Then 响应业务码应为 401
