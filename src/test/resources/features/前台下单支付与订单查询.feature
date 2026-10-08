Feature: 前台下单支付与订单查询
  前台 mall-portal 服务的 /member/address/** 与 /order/**，需要会员登录

  约定（来自源码）：
  - POST /order/generateConfirmOrder 请求体为被选中的购物车条目 id 数组，如 [1,2]
  - POST /order/generateOrder 请求体为 OrderParam：{memberReceiveAddressId, couponId, useIntegration, payType, cartIds}
    - memberReceiveAddressId 为空时 Asserts.fail("请选择收货地址！") -> code=500
    - 返回 data = {order: OmsOrder, orderItemList: [...]}，订单 id 在 data.order.id
    - 下单成功后会删除已下单的购物车条目，并发送延迟消息（RabbitMQ）用于超时关单
  - POST /order/paySuccess?orderId=&payType= 模拟第三方支付成功回调，把订单置为已支付并扣减真实库存
  - GET /order/list 需要 status 参数：-1 全部 / 0 待付款 / 1 待发货 / 2 已发货 / 3 已完成 / 4 已关闭
  - 确认收货 /order/confirmReceiveOrder 要求订单状态为已发货（需后台发货），不在 P0 范围
  - 不接入真实支付宝：alipay.appId 仍为占位值，P0 只验证 paySuccess 回调

  Background:
    Given 前台商城端服务可用
    And 已注册并登录的测试会员
    And 请求头携带有效的会员 token
    And 已清空当前会员购物车
    And 已添加商品 "1" 规格 "1" 数量 "1" 到购物车

  @P0 @smoke @order
  Scenario: 新增收货地址并记录
    When 新增一条默认收货地址并记录其 id
    Then 响应业务码应为 200
    And 响应体 "data" 应等于 "1"

  @P0 @smoke @order
  Scenario: 根据购物车生成订单确认单
    When 生成订单确认单
    Then 响应业务码应为 200
    And 响应体 "data.cartPromotionItemList" 不应为空
    And 响应体 "data.calcAmount.payAmount" 应存在

  @P0 @smoke @order
  Scenario: 会员提交订单成功
    Given 已新增收货地址并记录其 id
    When 提交订单
    Then 响应业务码应为 200
    And 响应体 "message" 应等于 "下单成功"
    And 响应体 "data.order.orderSn" 不应为空
    And 下单响应中应记录订单 id

  @P0 @order
  Scenario: 未选择收货地址时下单失败
    When 提交缺少收货地址的订单
    Then 响应业务码应为 500
    And 响应体 "message" 应等于 "请选择收货地址！"

  @P0 @smoke @order
  Scenario: 支付成功回调更新订单状态
    Given 已新增收货地址并记录其 id
    And 已提交订单并记录订单 id
    When 以 payType 1 回报订单支付成功
    Then 响应业务码应为 200
    And 响应体 "message" 应等于 "支付成功"

  @P0 @order
  Scenario Outline: 按订单状态分页查询订单列表
    Given 已新增收货地址并记录其 id
    And 已提交订单并记录订单 id
    When 以状态 <status> 分页查询订单列表
    Then 响应业务码应为 200
    And 响应体 "data" 应存在

    Examples:
      | status |
      | -1     |
      | 0      |
      | 1      |
      | 2      |
      | 3      |
      | 4      |

  @P0 @order
  Scenario: 查询订单详情
    Given 已新增收货地址并记录其 id
    And 已提交订单并记录订单 id
    When 查询当前订单详情
    Then 响应业务码应为 200
    And 响应体 "data.orderItemList" 不应为空
    And 响应体 "data.orderSn" 不应为空

  @P0 @order
  Scenario: 会员取消未付款订单
    Given 已新增收货地址并记录其 id
    And 已提交订单并记录订单 id
    When 取消当前订单
    Then 响应业务码应为 200
    When 以状态 4 分页查询订单列表
    Then 响应业务码应为 200
    And 订单列表中应包含已取消的订单
