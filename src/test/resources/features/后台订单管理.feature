Feature: 后台订单管理
  后台 mall-admin 服务的 /order/**，需要后台管理员 token

  说明：为了让后台订单用例自给自足、不依赖预置订单数据，
  Background 先通过前台 /order 接口造一笔真实订单（会员注册 - 加购 - 地址 - 下单），
  再切到后台管理端做查询与操作；订单数据会真实落库。

  约定（来自源码）：
  - GET /order/list：查询参数为 OmsOrderQueryParam，pageNum 默认 1、pageSize 默认 5
  - GET /order/{id}：返回 data 为 OmsOrderDetail（含 orderItemList 与操作记录）
  - POST /order/update/close：ids 为订单 id 列表 + note，批量关闭订单
  - POST /order/update/note：id + note + status，备注订单
  - POST /order/update/delivery 批量发货需要完整的收货信息与物流单号，放到 P1

  Background:
    Given 前台商城端服务可用
    And 已注册并登录的测试会员
    And 请求头携带有效的会员 token
    And 已清空当前会员购物车
    And 已添加商品 "1" 规格 "1" 数量 "1" 到购物车
    And 已新增收货地址并记录其 id
    And 已提交订单并记录订单 id
    And 后台管理端服务可用
    And 管理员已登录并持有有效 token

  @P0 @smoke @admin @order
  Scenario: 分页查询后台订单列表
    Given 请求头携带有效的后台 token
    When 以 GET 请求后台 "/order/list" 参数为：
      | pageNum  | 1 |
      | pageSize | 5 |
    Then 响应业务码应为 200
    And 响应体 "data" 应存在

  @P0 @smoke @admin @order
  Scenario: 查询后台订单详情
    Given 请求头携带有效的后台 token
    When 后台查询当前订单的详情
    Then 响应业务码应为 200
    And 响应体 "data.id" 应存在
    And 响应体 "data.orderItemList" 应存在

  @P0 @admin @order
  Scenario: 后台备注订单
    Given 请求头携带有效的后台 token
    When 后台为当前订单添加备注 "BDD自动化备注"
    Then 响应业务码应为 200

  @P0 @admin @order
  Scenario: 后台批量关闭订单
    Given 请求头携带有效的后台 token
    When 后台关闭当前订单 备注为 "BDD自动化关闭"
    Then 响应业务码应为 200
    When 后台查询当前订单的详情
    Then 响应业务码应为 200
    And 响应体 "data.status" 应等于 "4"

  @P0 @admin @order
  Scenario: 未携带 token 访问后台订单列表被拦截
    Given 请求头不携带 token
    When 以 GET 请求后台 "/order/list" 参数为：
      | pageNum  | 1 |
      | pageSize | 5 |
    Then 响应业务码应为 401
