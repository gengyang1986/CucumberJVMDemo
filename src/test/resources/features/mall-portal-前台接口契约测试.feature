Feature: mall-portal 前台接口契约测试
  依据 back 仓库 mall-portal 模块 controller 包（/sso、/home、/product、/brand、/cart、/order、/returnApply、/member/**、/alipay）
  分析生成的接口契约，验证对外接口的可用性与响应体结构（CommonResult.code / data）
  基准文档：src/test/resources/openapi/mall-portal.yaml（由源码生成的 OpenAPI 3.0.3 描述文件）

  Background:
    Given 前台服务地址为 "http://localhost:8085"

  Scenario: 会员注册接口契约
    When 调用 "POST /sso/register" 并传入参数 "username=apitest,password=123456,telephone=13800000000,authCode=123456"
    Then 响应体 code 应为 200
    And 响应体 message 应为 "注册成功"

  Scenario: 会员登录成功返回 token
    Given 会员账号 "apitest" 密码 "123456" 已存在
    When 调用 "POST /sso/login" 并传入参数 "username=apitest,password=123456"
    Then 响应体 code 应为 200
    And 响应体 data 应包含字段 "token"
    And 响应体 data 应包含字段 "tokenHead"
    And 从本次响应中记录 token

  Scenario: 未登录访问会员信息接口应返回未授权
    When 不带 token 调用 "GET /sso/info"
    Then 响应体 code 应为 401

  Scenario: 登录后获取会员信息
    When 携带 token 调用 "GET /sso/info"
    Then 响应体 code 应为 200
    And 响应体 data 应包含字段 "username"
    And 响应体 data 应包含字段 "integration"

  Scenario: 首页内容接口契约
    When 调用 "GET /home/content" 并传入参数 ""
    Then 响应体 code 应为 200
    And 响应体 data 应包含字段 "advertiseList"
    And 响应体 data 应包含字段 "brandList"
    And 响应体 data 应包含字段 "newProductList"
    And 响应体 data 应包含字段 "hotProductList"
    And 响应体 data 应包含字段 "subjectList"

  Scenario: 商品搜索分页接口契约
    When 调用 "GET /product/search" 并传入参数 "keyword=手机,pageNum=0,pageSize=5,sort=0"
    Then 响应体 code 应为 200
    And 响应体 data 应包含分页字段 "pageNum,pageSize,totalPage,total,list"

  Scenario: 商品详情接口契约
    Given 存在已上架商品 "1"
    When 调用 "GET /product/detail/1" 并传入参数 ""
    Then 响应体 code 应为 200
    And 响应体 data 应包含字段 "product"
    And 响应体 data 应包含字段 "brand"

  Scenario: 购物车增删改查接口契约
    When 携带 token 调用 "POST /cart/add" 并传入 JSON "{\"productId\":1,\"productSkuId\":1,\"quantity\":1}"
    Then 响应体 code 应为 200
    When 携带 token 调用 "GET /cart/list" 并传入参数 ""
    Then 响应体 code 应为 200
    And 响应体 data 应为数组
    When 携带 token 调用 "GET /cart/list/promotion" 并传入参数 ""
    Then 响应体 code 应为 200
    When 携带 token 调用 "POST /cart/clear" 并传入参数 ""
    Then 响应体 code 应为 200

  Scenario: 下单确认单接口契约
    Given 购物车中存在商品 "1"
    When 携带 token 调用 "POST /order/generateConfirmOrder" 并传入 JSON "[1]"
    Then 响应体 code 应为 200
    And 响应体 data 应包含字段 "cartPromotionItemList"
    And 响应体 data 应包含字段 "memberReceiveAddressList"
    And 响应体 data 应包含字段 "calcAmount"

  Scenario: 订单列表状态参数契约
    When 携带 token 调用 "GET /order/list" 并传入参数 "status=-1,pageNum=1,pageSize=5"
    Then 响应体 code 应为 200
    And 响应体 data 应包含分页字段 "pageNum,pageSize,totalPage,total,list"
    When 携带 token 调用 "GET /order/list" 并传入参数 "status=9,pageNum=1,pageSize=5"
    Then 响应体 data 中 list 长度应不大于 "0"

  Scenario: 收货地址管理接口契约
    When 携带 token 调用 "POST /member/address/add" 并传入 JSON "{\"name\":\"测试\",\"phoneNumber\":\"13800000000\",\"defaultStatus\":0,\"province\":\"广东省\",\"city\":\"深圳市\",\"region\":\"南山区\",\"detailAddress\":\"科技园\"}"
    Then 响应体 code 应为 200
    When 携带 token 调用 "GET /member/address/list" 并传入参数 ""
    Then 响应体 code 应为 200
    And 响应体 data 应为数组

  Scenario: 会员优惠券列表接口契约
    When 携带 token 调用 "GET /member/coupon/list" 并传入参数 "useStatus=0"
    Then 响应体 code 应为 200
    And 响应体 data 应为数组
    When 携带 token 调用 "GET /member/coupon/listHistory" 并传入参数 "useStatus=0"
    Then 响应体 code 应为 200

  Scenario: 品牌与品牌商品接口契约
    When 调用 "GET /brand/recommendList" 并传入参数 "pageNum=1,pageSize=6"
    Then 响应体 code 应为 200
    And 响应体 data 应为数组
    When 调用 "GET /brand/detail/1" 并传入参数 ""
    Then 响应体 code 应为 200
    And 响应体 data 应包含字段 "name"
    When 调用 "GET /brand/productList" 并传入参数 "brandId=1,pageNum=1,pageSize=6"
    Then 响应体 code 应为 200
    And 响应体 data 应包含分页字段 "pageNum,pageSize,total,list"