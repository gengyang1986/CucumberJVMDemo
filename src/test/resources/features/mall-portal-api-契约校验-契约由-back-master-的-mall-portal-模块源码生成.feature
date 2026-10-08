Feature: mall-portal API 契约校验（契约由 back@master 的 mall-portal 模块源码生成）

  # 契约原文（OpenAPI 3.0.3）内嵌在本 feature 的 step 文件
  # src/test/java/com/macro/mall/contract/MallPortalApiContractSteps.java
  # 常量 OPENAPI_YAML 中，与用例同仓维护、同分支评审。
  #
  # 运行示例：
  #   mvn test -Dcucumber.filter.tags="@api-contract" \
  #            -Dapi.base.url=http://localhost:8085 \
  #            -Dmall.member.username=<账号> -Dmall.member.password=<密码>

  Background:
    Given the mall-portal service is running at "http://localhost:8085"

  @smoke @no-auth
  Scenario: 首页内容接口免登录可用
    When I send GET "/home/content" without login
    Then the response business code is 200
    And the response data is not null

  @smoke @no-auth
  Scenario: 商品分类树接口免登录可用
    When I send GET "/product/categoryTreeList" without login
    Then the response business code is 200

  @security
  Scenario: 未登录访问会员信息返回 401
    When I send GET "/sso/info" without login
    Then the response business code is 401

  @security
  Scenario: 用户名或密码错误时登录被拒绝
    When I send POST "/sso/login" with form params
      | username | wrong_user_for_contract_test |
      | password | wrong_password_for_contract_test |
    Then the response business code is 404

  @auth
  Scenario: 登录会员可读取购物车列表
    Given I am logged in as the configured member
    When I send GET "/cart/list" with the current token
    Then the response business code is 200

  @contract
  Scenario: OpenAPI 契约文件随测试仓维护
    Then the embedded OpenAPI contract declares the portal endpoints