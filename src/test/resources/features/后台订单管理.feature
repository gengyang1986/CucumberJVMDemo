Feature: 后台订单管理
  mall-admin 后台管理端的 /order/** 接口，需要后台管理员 token

  说明：为了让后台订单用例自给自足、不依赖预置订单数据，
  Background 先通过前台 /order 接口造一笔真实订单（会员注册 - 加购 - 地址 - 下单），
  再切到后台管理端做查询与操作；订单数据会真实落库。

  约定（已逐条核对 back 仓的 OmsOrderController / OmsOrderServiceImpl / OmsOrderDao.xml）：
  - GET /order/list：查询参数为 OmsOrderQueryParam（orderSn 为精确匹配，receiverKeyword 为模糊匹配，
    另有 status / orderType / sourceType / createTime），pageNum 默认 1、pageSize 默认 5；
    返回 CommonResult<CommonPage<OmsOrder>>，data 含 pageNum / pageSize / totalPage / total / list
  - GET /order/{id}：data 为 OmsOrderDetail（继承 OmsOrder，含 orderItemList 商品列表、historyList 操作记录）
  - POST /order/update/close：表单参数 ids（逗号分隔的订单 id 列表）+ note；关闭后订单状态置 4（已关闭）
  - POST /order/update/note：表单参数 id + note + status（status 随备注写入操作记录）
  - POST /order/update/delivery：JSON body 为 List<OmsOrderDeliveryParam>，
    每项仅需 orderId / deliveryCompany / deliverySn（不含收货信息）；
    发货成功后追加一条“完成发货”的操作记录；该接口只在订单为待发货态（status=1）时生效，
    因此本用例先通过前台支付回调把订单置为待发货
  - 未登录或 token 无效时由 RestAuthenticationEntryPoint 输出结果：
    HTTP 状态码仍为 200，响应体为 {"code":401,...}。
    本 feature 因此统一断言“业务码”，不要断言 HTTP 状态码。

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
    And 响应体 "data.list" 应存在
    And 响应体 "data.total" 应存在

  @P0 @smoke @admin @order
  Scenario: 查询后台订单详情
    Given 请求头携带有效的后台 token
    When 后台查询当前订单的详情
    Then 响应业务码应为 200
    And 响应体 "data.id" 应存在
    And 响应体 "data.orderSn" 应存在
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

  # 缺口补齐 1：列表筛选（关闭后按状态 4 筛选，覆盖 OmsOrderQueryParam.status 分支）
  @P1 @admin @order
  Scenario: 按订单状态筛选后台订单列表
    Given 请求头携带有效的后台 token
    When 后台关闭当前订单 备注为 "BDD筛选前置关闭"
    And 以 GET 请求后台 "/order/list" 参数为：
      | pageNum  | 1 |
      | pageSize | 5 |
      | status   | 4 |
    Then 响应业务码应为 200
    And 响应体 "data" 应存在
    And 响应体 "data.list" 应存在
    And 响应体 "data.total" 应存在

  # 缺口补齐 2：越权访问（会员 token 访问后台接口）
  # 注意：预期为业务码 401（未认证，由 RestAuthenticationEntryPoint 返回）；
  # 若实际环境返回其它业务码，请按实测结果修正并回填到本场景
  @P1 @admin @order
  Scenario: 使用会员 token 访问后台订单列表应被拦截
    Given 请求头携带有效的会员 token
    When 以 GET 请求后台 "/order/list" 参数为：
      | pageNum  | 1 |
      | pageSize | 5 |
    Then 响应业务码应为 401

  # 缺口补齐 3：批量发货（P1，需先把订单置为待发货态）
  @P1 @admin @order
  Scenario: 后台批量发货待发货订单
    Given 请求头携带有效的会员 token
    When 会员支付当前订单
    Given 请求头携带有效的后台 token
    When 后台批量发货当前订单 物流公司 "顺丰速运" 物流单号 "BDD2026000000001"
    Then 响应业务码应为 200
    When 后台查询当前订单的详情
    Then 响应业务码应为 200
    And 响应体 "data.historyList" 应存在