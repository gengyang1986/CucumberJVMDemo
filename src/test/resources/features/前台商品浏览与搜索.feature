Feature: 前台商品浏览与搜索
  前台 mall-portal 服务的 /product/**，全部在白名单内，无需登录

  约定（来自源码）：
  - GET /product/search：keyword / brandId / productCategoryId / pageNum(默认0) / pageSize(默认5) / sort(0按相关度 1按新品 2按销量 3价格升序 4价格降序)
  - 搜索只返回 deleteStatus=0 且 publishStatus=1 的商品
  - GET /product/categoryTreeList：只取 parentId=0 的分类，递归拼 children
  - GET /product/detail/{id}：返回商品 + 品牌 + 属性 + 属性值 + skuStockList + 可用优惠券
  - 列表非空断言依赖 mall.sql 演示数据未被清空

  Background:
    Given 前台商城端服务可用

  @P0 @product
  Scenario: 以树形结构获取商品分类
    When 以 GET 请求前台 "/product/categoryTreeList"
    Then 响应业务码应为 200
    And 响应体 "data" 不应为空
    And 响应体 "data[0].children" 应存在
    And 商品分类树中每个顶级分类都应包含 children 字段

  @P0 @smoke @product
  Scenario: 关键字搜索商品
    When 以 GET 请求前台 "/product/search" 参数为：
      | keyword  | 手机 |
      | pageNum  | 1    |
      | pageSize | 5    |
    Then 响应业务码应为 200
    And 响应体 "data" 应存在
    And 响应体 "data.pageNum" 应等于 "1"

  @P0 @product
  Scenario Outline: 按不同排序方式搜索商品均成功返回
    When 以 GET 请求前台 "/product/search" 参数为：
      | pageNum  | 1      |
      | pageSize | 5      |
      | sort     | <sort> |
    Then 响应业务码应为 200
    And 响应体 "data" 应存在
    And 响应体 "data.pageSize" 应等于 "5"

    Examples:
      | sort |
      | 0    |
      | 1    |
      | 2    |
      | 3    |
      | 4    |

  @P0 @smoke @product
  Scenario: 获取商品详情
    When 以 GET 请求前台 "/product/detail/1"
    Then 响应业务码应为 200
    And 响应体 "data.product" 应存在
    And 响应体 "data.product.id" 应等于 "1"
    And 响应体 "data.product.publishStatus" 应等于 "1"
    And 响应体 "data.skuStockList" 应存在

  @P0 @product
  Scenario: 前台商品搜索不需要 token
    Given 请求头不携带 token
    When 以 GET 请求前台 "/product/search" 参数为：
      | pageNum  | 1 |
      | pageSize | 5 |
    Then 响应业务码应为 200
