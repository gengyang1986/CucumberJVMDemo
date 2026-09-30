@admin @product
Feature: 后台管理-商品管理
  覆盖商品管理 P0 接口（均需携带管理员 JWT 令牌）：
    POST /product/create            创建商品，入参为 PmsProductParam JSON
    GET  /product/list              分页查询商品，支持 pageNum/pageSize/productSn 等 PmsProductQueryParam 条件
    GET  /product/updateInfo/{id}   按商品 id 获取编辑回显信息
    POST /product/update/{id}       按商品 id 更新商品
  分页返回体：data = CommonPage{pageNum, pageSize, totalPage, total, list}
  测试数据依赖：商品必须归属有效商品分类，需通过 -Dtest.product.categoryId / -Dtest.product.brandId 指定环境已存在的 id。

  Background:
    Given 后台管理服务地址使用测试配置
    And 后台管理员 "admin" 已使用密码 "123456" 登录后台管理服务

  @P0 @smoke
  Scenario: 创建商品成功并可按货号查询到
    When 后台创建一件货号为自动生成、名称为 "BDD自动化商品" 的商品
    Then 商品接口返回码为 200
    When 后台以刚创建的货号作为筛选条件分页查询商品
    Then 商品接口返回码为 200
    And 商品分页结果的 total 大于 0
    And 商品分页结果第一条的货号等于刚创建的货号

  @P0
  Scenario: 按分页参数查询商品列表
    When 后台以页码 1、每页 5 条查询商品列表
    Then 商品接口返回码为 200
    And 商品分页结果的 pageSize 为 5
    And 商品分页结果的 list 字段存在

  @P0
  Scenario: 查询商品编辑信息用于回显
    Given 后台已创建一件自动生成货号、名称为 "BDD自动化商品" 的商品
    When 后台根据刚创建的商品 id 查询商品编辑信息
    Then 商品接口返回码为 200
    And 商品编辑信息中的名称等于 "BDD自动化商品"

  @P0
  Scenario: 更新商品名称后编辑信息同步更新
    Given 后台已创建一件自动生成货号、名称为 "BDD自动化商品" 的商品
    When 后台把刚创建的商品名称更新为 "BDD自动化商品-已更新"
    Then 商品接口返回码为 200
    When 后台根据刚创建的商品 id 再次查询商品编辑信息
    Then 商品编辑信息中的名称等于 "BDD自动化商品-已更新"
