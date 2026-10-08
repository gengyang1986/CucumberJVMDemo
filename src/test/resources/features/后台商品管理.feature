Feature: 后台商品管理
  后台 mall-admin 服务的 /product/**，需要后台管理员 token（/admin/** 之外均不在白名单）

  约定（来自源码）：
  - GET /product/list：查询参数为 PmsProductQueryParam（keyword/productSn/brandId/productCategoryId/publishStatus/verifyStatus），pageNum 默认 1、pageSize 默认 5
  - GET /product/simpleList：按商品名称或货号模糊查询，不传 keyword 时返回全部
  - GET /product/updateInfo/{id}：商品编辑回显，返回 data 含商品信息 + skuStockList + 属性值
  - POST /product/update/publishStatus：ids 为商品 id 列表、publishStatus 0 下架 / 1 上架
  - 上下架场景会改动商品状态，用例内自动还原为原状态，避免污染演示数据
  - 创建商品 POST /product/create 会落库且无法回滚，放到 P1

  Background:
    Given 后台管理端服务可用
    And 管理员已登录并持有有效 token
    And 请求头携带有效的后台 token

  @P0 @smoke @admin @product
  Scenario: 分页查询后台商品列表
    When 以 GET 请求后台 "/product/list" 参数为：
      | pageNum  | 1 |
      | pageSize | 5 |
    Then 响应业务码应为 200
    And 响应体 "data.list" 不应为空
    And 响应体 "data.pageNum" 应等于 "1"
    And 响应体 "data.pageSize" 应等于 "5"

  @P0 @admin @product
  Scenario: 按名称或货号模糊查询商品
    When 以 GET 请求后台 "/product/simpleList"
    Then 响应业务码应为 200
    And 响应体 "data" 不应为空

  @P0 @smoke @admin @product
  Scenario: 获取商品编辑信息
    When 以 GET 请求后台 "/product/updateInfo/1"
    Then 响应业务码应为 200
    And 响应体 "data" 应存在
    And 响应体 "data.id" 应等于 "1"
    And 响应体 "data.skuStockList" 应存在

  @P0 @admin @product
  Scenario: 商品上下架状态切换并还原
    Given 记录后台商品 "1" 的当前上架状态
    When 将后台商品 "1" 的上架状态置为 "0"
    Then 响应业务码应为 200
    And 后台商品 "1" 的上架状态应为 "0"
    When 将后台商品 "1" 的上架状态还原
    Then 响应业务码应为 200
    And 后台商品 "1" 的上架状态应为 "1"

  @P0 @admin @product
  Scenario: 未携带 token 访问后台商品列表被拦截
    Given 请求头不携带 token
    When 以 GET 请求后台 "/product/list" 参数为：
      | pageNum  | 1 |
      | pageSize | 5 |
    Then 响应业务码应为 401
