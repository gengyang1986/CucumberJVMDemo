Feature: 前台购物车管理
  前台 mall-portal 服务的 /cart/** 需要会员登录态（Authorization: 会员 token），会员身份由 token 解析。
  执行环境：app-portal；baseUrl 通过环境变量 PORTAL_BASE_URL（或 BASE_URL）注入。

  约定（来自 back 仓 mall-portal 模块 OmsCartItemController / OmsCartItemServiceImpl）：
  - POST /cart/add 请求体为 OmsCartItem JSON；memberId 由服务端用当前会员覆盖；返回 data 为受影响行数
  - 同一 memberId + productId + productSkuId 重复加购时在原条目上累加数量
  - GET /cart/list 只返回当前会员未删除的条目
  - GET /cart/update/quantity?id=&quantity= 的 id 是购物车条目 id（oms_cart_item.id），不是商品 id
  - GET /cart/list/promotion?cartIds= 支持按购物车条目 id 过滤
  - GET /cart/getProduct/{productId} 返回商品的规格信息
  - POST /cart/update/attr 的实现是「按条目 id 软删旧条目 + 按新规格重新加购」
  - POST /cart/delete?ids=、POST /cart/clear、update/quantity 在受影响行数为 0 时返回 code=500「操作失败」（当前实现）
  - 每个场景使用新注册的随机会员，购物车天然隔离

  Background:
    Given 前台商城端服务地址已配置
    And 已注册并登录一个随机会员账号

  @P0 @smoke @cart
  Scenario: 添加商品到购物车成功
    When 将商品 1 以数量 1 加入购物车
    Then 响应业务码应为 200
    And 购物车列表中该商品数量应为 1

  @P0 @cart
  Scenario: 重复添加同一商品同一规格时数量累加
    When 将商品 1 以数量 1 加入购物车
    Then 响应业务码应为 200
    When 再次将商品 1 以数量 2 加入购物车
    Then 响应业务码应为 200
    And 购物车列表中该商品数量应为 3

  @P1 @cart
  Scenario: 添加时请求体中的 memberId 被服务端覆盖为当前会员
    When 以请求体 memberId 999999 将商品 1 以数量 1 加入购物车
    Then 响应业务码应为 200
    And 购物车列表中该商品的 memberId 应不等于 999999

  @P0 @cart
  Scenario: 购物车列表只返回当前会员的条目
    Given 另有一个已注册并登录的随机会员账号
    When 将商品 1 以数量 1 加入购物车
    And 查询当前会员购物车列表
    Then 购物车列表中应包含商品 1 并记录其条目
    When 切换到另一会员并查询购物车列表
    Then 响应业务码应为 200
    And 购物车列表中不应包含商品 1

  @P0 @cart
  Scenario: 查询购物车促销信息
    When 将商品 1 以数量 1 加入购物车
    Then 响应业务码应为 200
    When 查询购物车促销信息
    Then 响应业务码应为 200
    And 促销列表中应包含商品 1

  @P1 @cart
  Scenario: 按指定购物车条目过滤促销信息
    When 将商品 1 以数量 1 加入购物车
    And 查询当前会员购物车列表
    Then 购物车列表中应包含商品 1 并记录其条目
    When 按已记录条目查询购物车促销信息
    Then 促销列表中条目数应为 1

  @P0 @cart
  Scenario: 修改购物车中商品的数量
    When 将商品 1 以数量 1 加入购物车
    And 查询当前会员购物车列表
    Then 购物车列表中应包含商品 1 并记录其条目
    When 将已记录条目数量修改为 3
    Then 响应业务码应为 200
    And 购物车列表中该商品数量应为 3

  @P2 @baseline @cart
  Scenario: 修改数量为 0 时记录当前实现基线
    # 当前实现未校验数量下限，quantity=0 仍会更新成功；本场景用于监控实现变化
    # 若此场景失败，说明实现已加入下限校验，请同步更新该基线场景
    When 将商品 1 以数量 1 加入购物车
    And 查询当前会员购物车列表
    Then 购物车列表中应包含商品 1 并记录其条目
    When 将已记录条目数量修改为 0
    Then 响应业务码应为 200
    And 购物车列表中该商品数量应为 0

  @P1 @cart
  Scenario: 他人无法修改当前会员购物车条目的数量
    # 关键断言是「当前会员的数据不被改动」；500 是当前实现的表现
    Given 另有一个已注册并登录的随机会员账号
    When 将商品 1 以数量 1 加入购物车
    And 查询当前会员购物车列表
    Then 购物车列表中应包含商品 1 并记录其条目
    When 使用另一会员的 token 将已记录条目数量修改为 5
    Then 当前实现返回业务码 500 且消息包含 操作失败
    And 已记录条目的数量应保持为 1

  @P0 @cart
  Scenario: 获取购物车中指定商品的规格用于重选
    When 获取商品 1 的购物车规格信息
    Then 响应业务码应为 200
    And 响应中的商品 id 应为 1

  @P0 @cart
  Scenario: 修改购物车中商品的规格
    When 将商品 1 以数量 1 加入购物车
    And 查询当前会员购物车列表
    Then 购物车列表中应包含商品 1 并记录其条目
    When 获取商品 1 的购物车规格信息
    Then 响应业务码应为 200
    When 将已记录条目的规格更新为当前商品规格
    Then 响应业务码应为 200

  @P0 @cart
  Scenario: 删除购物车中的指定商品
    When 将商品 1 以数量 1 加入购物车
    And 查询当前会员购物车列表
    Then 购物车列表中应包含商品 1 并记录其条目
    When 删除已记录的购物车条目
    Then 响应业务码应为 200
    And 购物车列表中不应包含商品 1

  @P1 @cart
  Scenario: 他人无法删除当前会员的购物车条目
    Given 另有一个已注册并登录的随机会员账号
    When 将商品 1 以数量 1 加入购物车
    And 查询当前会员购物车列表
    Then 购物车列表中应包含商品 1 并记录其条目
    When 使用另一会员的 token 删除已记录条目
    Then 已记录条目应仍存在于购物车

  @P0 @cart
  Scenario: 清空当前会员的购物车
    When 将商品 1 以数量 1 加入购物车
    Then 响应业务码应为 200
    When 清空当前会员购物车
    Then 响应业务码应为 200
    And 购物车列表中条目数应为 0

  @P2 @baseline @cart
  Scenario: 购物车为空时清空记录当前实现基线
    # 当前实现：受影响行数为 0 时返回 code=500「操作失败」，清空操作不幂等
    # 若此场景失败，说明实现已改为幂等成功，请同步更新该基线场景
    When 清空当前会员购物车
    Then 当前实现返回业务码 500 且消息包含 操作失败

  @P1 @cart
  Scenario: 未携带 token 访问购物车列表应被拒绝
    When 不携带 token 查询购物车列表
    Then 应返回未认证或无权访问