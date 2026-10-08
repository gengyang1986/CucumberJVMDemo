# language: zh-CN
功能: mall-portal OpenAPI 契约文档归档与解析校验
  依据 back 仓 master 分支源码（com.macro.mall.portal.controller，服务端口 8085）生成的 OpenAPI 3.0.3 契约。
  本功能把契约原文随产物归档为 src/test/resources/openapi/mall-portal.yaml（由 step 从 docstring 写出），
  并校验其可被 OpenAPI 3.0.3 解析、路径数量覆盖完整，便于后续做契约回归（openapi-diff / 与运行时 /v3/api-docs 比对）。

  场景: 归档并解析校验 mall-portal OpenAPI 文档
    假如 将下列 OpenAPI 文档写入 "src/test/resources/openapi/mall-portal.yaml"
      """
      openapi: 3.0.3
      info:
        title: mall-portal 前台接口文档
        description: >-
          由 back 仓库 (master) 源码分析生成，对应 springdoc group mall-portal，
          Controller 位于 com.macro.mall.portal.controller。返回体统一为 CommonResult。
        version: 1.0.0
        license:
          name: Apache 2.0
          url: https://github.com/macrozheng/mall-learning
      servers:
        - url: http://localhost:8085
          description: 本地开发环境（application-dev.yml: server.port=8085，无 context-path）
      tags:
        - {name: SSO, description: 会员登录注册管理（UmsMemberController）}
        - {name: Home, description: 首页内容管理（HomeController）}
        - {name: Product, description: 前台商品管理（PmsPortalProductController）}
        - {name: Brand, description: 前台品牌管理（PmsPortalBrandController）}
        - {name: Cart, description: 购物车管理（OmsCartItemController）}
        - {name: Order, description: 订单管理（OmsPortalOrderController）}
        - {name: ReturnApply, description: 退货申请管理（OmsPortalOrderReturnApplyController）}
        - {name: MemberAttention, description: 会员关注品牌管理（MemberAttentionController）}
        - {name: MemberProductCollection, description: 会员收藏管理（MemberProductCollectionController）}
        - {name: MemberReadHistory, description: 会员浏览记录管理（MemberReadHistoryController）}
        - {name: MemberCoupon, description: 用户优惠券管理（UmsMemberCouponController）}
        - {name: MemberAddress, description: 会员收货地址管理（UmsMemberReceiveAddressController）}
        - {name: Alipay, description: 支付宝支付（AlipayController）}

      paths:
        /sso/register:
          post:
            tags: [SSO]
            summary: 会员注册
            operationId: register
            parameters:
              - {name: username, in: query, required: true, schema: {type: string}}
              - {name: password, in: query, required: true, schema: {type: string}}
              - {name: telephone, in: query, required: true, schema: {type: string}}
              - {name: authCode, in: query, required: true, schema: {type: string}}
            responses:
              '200':
                description: 注册结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /sso/login:
          post:
            tags: [SSO]
            summary: 会员登录
            operationId: login
            parameters:
              - {name: username, in: query, required: true, schema: {type: string}}
              - {name: password, in: query, required: true, schema: {type: string}}
            responses:
              '200':
                description: 成功返回 token；用户名或密码错误返回 code=404
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultToken'}}
        /sso/info:
          get:
            tags: [SSO]
            summary: 获取会员信息
            operationId: info
            security: [{bearerAuth: []}]
            responses:
              '200':
                description: 会员信息
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultUmsMember'}}
        /sso/getAuthCode:
          get:
            tags: [SSO]
            summary: 获取验证码
            operationId: getAuthCode
            parameters:
              - {name: telephone, in: query, required: true, schema: {type: string}}
            responses:
              '200':
                description: 验证码
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultString'}}
        /sso/updatePassword:
          post:
            tags: [SSO]
            summary: 会员修改密码
            operationId: updatePassword
            parameters:
              - {name: telephone, in: query, required: true, schema: {type: string}}
              - {name: password, in: query, required: true, schema: {type: string}}
              - {name: authCode, in: query, required: true, schema: {type: string}}
            responses:
              '200':
                description: 修改结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /sso/refreshToken:
          get:
            tags: [SSO]
            summary: 刷新 token
            operationId: refreshToken
            parameters:
              - {name: Authorization, in: header, required: false, schema: {type: string}}
            responses:
              '200':
                description: token 已过期返回 code=500
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultToken'}}
        /home/content:
          get:
            tags: [Home]
            summary: 首页内容信息展示
            operationId: content
            responses:
              '200':
                description: 首页聚合内容
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultHomeContentResult'}}
        /home/recommendProductList:
          get:
            tags: [Home]
            summary: 分页获取推荐商品
            operationId: recommendProductList
            parameters:
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 4}}
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
            responses:
              '200':
                description: 商品列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfPmsProduct'}}
        /home/productCateList/{parentId}:
          get:
            tags: [Home]
            summary: 获取首页商品分类
            operationId: getProductCateList
            parameters:
              - {name: parentId, in: path, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 分类列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfPmsProductCategory'}}
        /home/subjectList:
          get:
            tags: [Home]
            summary: 根据分类获取专题
            operationId: getSubjectList
            parameters:
              - {name: cateId, in: query, required: false, schema: {type: integer, format: int64}}
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 4}}
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
            responses:
              '200':
                description: 专题列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfCmsSubject'}}
        /home/hotProductList:
          get:
            tags: [Home]
            summary: 分页获取人气推荐商品
            operationId: hotProductList
            parameters:
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 6}}
            responses:
              '200':
                description: 商品列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfPmsProduct'}}
        /home/newProductList:
          get:
            tags: [Home]
            summary: 分页获取新品推荐商品
            operationId: newProductList
            parameters:
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 6}}
            responses:
              '200':
                description: 商品列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfPmsProduct'}}
        /product/search:
          get:
            tags: [Product]
            summary: 综合搜索、筛选、排序
            operationId: search
            parameters:
              - {name: keyword, in: query, required: false, schema: {type: string}}
              - {name: brandId, in: query, required: false, schema: {type: integer, format: int64}}
              - {name: productCategoryId, in: query, required: false, schema: {type: integer, format: int64}}
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 0}}
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 5}}
              - name: sort
                in: query
                required: false
                description: 排序字段 - 0->按相关度；1->按新品；2->按销量；3->价格从低到高；4->价格从高到低
                schema: {type: integer, default: 0, enum: [0, 1, 2, 3, 4]}
            responses:
              '200':
                description: 分页商品列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultCommonPagePmsProduct'}}
        /product/categoryTreeList:
          get:
            tags: [Product]
            summary: 以树形结构获取所有商品分类
            operationId: categoryTreeList
            responses:
              '200':
                description: 分类树
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfPmsProductCategoryNode'}}
        /product/detail/{id}:
          get:
            tags: [Product]
            summary: 获取前台商品详情
            operationId: detail
            parameters:
              - {name: id, in: path, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 商品详情
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultPmsPortalProductDetail'}}
        /brand/recommendList:
          get:
            tags: [Brand]
            summary: 分页获取推荐品牌
            operationId: brandRecommendList
            parameters:
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 6}}
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
            responses:
              '200':
                description: 品牌列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfPmsBrand'}}
        /brand/detail/{brandId}:
          get:
            tags: [Brand]
            summary: 获取品牌详情
            operationId: brandDetail
            parameters:
              - {name: brandId, in: path, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 品牌详情
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultPmsBrand'}}
        /brand/productList:
          get:
            tags: [Brand]
            summary: 分页获取品牌相关商品
            operationId: brandProductList
            parameters:
              - {name: brandId, in: query, required: true, schema: {type: integer, format: int64}}
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 6}}
            responses:
              '200':
                description: 分页商品列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultCommonPagePmsProduct'}}
        /cart/add:
          post:
            tags: [Cart]
            summary: 添加商品到购物车
            operationId: cartAdd
            security: [{bearerAuth: []}]
            requestBody:
              required: true
              content:
                application/json: {schema: {$ref: '#/components/schemas/OmsCartItem'}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /cart/list:
          get:
            tags: [Cart]
            summary: 获取当前会员的购物车列表
            operationId: cartList
            security: [{bearerAuth: []}]
            responses:
              '200':
                description: 购物车列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfOmsCartItem'}}
        /cart/list/promotion:
          get:
            tags: [Cart]
            summary: 获取当前会员的购物车列表，包括促销信息
            operationId: cartListPromotion
            security: [{bearerAuth: []}]
            parameters:
              - name: cartIds
                in: query
                required: false
                schema: {type: array, items: {type: integer, format: int64}}
            responses:
              '200':
                description: 含促销信息的购物车列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfCartPromotionItem'}}
        /cart/update/quantity:
          get:
            tags: [Cart]
            summary: 修改购物车中指定商品的数量
            operationId: cartUpdateQuantity
            security: [{bearerAuth: []}]
            parameters:
              - {name: id, in: query, required: true, schema: {type: integer, format: int64}}
              - {name: quantity, in: query, required: true, schema: {type: integer}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /cart/getProduct/{productId}:
          get:
            tags: [Cart]
            summary: 获取购物车中指定商品的规格，用于重选规格
            operationId: cartGetProduct
            security: [{bearerAuth: []}]
            parameters:
              - {name: productId, in: path, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 带规格、SKU 的商品信息
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultCartProduct'}}
        /cart/update/attr:
          post:
            tags: [Cart]
            summary: 修改购物车中商品的规格
            operationId: cartUpdateAttr
            security: [{bearerAuth: []}]
            requestBody:
              required: true
              content:
                application/json: {schema: {$ref: '#/components/schemas/OmsCartItem'}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /cart/delete:
          post:
            tags: [Cart]
            summary: 删除购物车中的指定商品
            operationId: cartDelete
            security: [{bearerAuth: []}]
            parameters:
              - name: ids
                in: query
                required: true
                schema: {type: array, items: {type: integer, format: int64}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /cart/clear:
          post:
            tags: [Cart]
            summary: 清空当前会员的购物车
            operationId: cartClear
            security: [{bearerAuth: []}]
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /order/generateConfirmOrder:
          post:
            tags: [Order]
            summary: 根据购物车信息生成确认单
            operationId: generateConfirmOrder
            security: [{bearerAuth: []}]
            requestBody:
              required: true
              description: 购物车商品 id 列表
              content:
                application/json:
                  schema: {type: array, items: {type: integer, format: int64}}
            responses:
              '200':
                description: 确认单信息
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultConfirmOrderResult'}}
        /order/generateOrder:
          post:
            tags: [Order]
            summary: 根据购物车信息生成订单
            operationId: generateOrder
            security: [{bearerAuth: []}]
            requestBody:
              required: true
              content:
                application/json: {schema: {$ref: '#/components/schemas/OrderParam'}}
            responses:
              '200':
                description: 下单结果，data 为 Map
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultMap'}}
        /order/paySuccess:
          post:
            tags: [Order]
            summary: 用户支付成功的回调
            operationId: paySuccess
            security: [{bearerAuth: []}]
            parameters:
              - {name: orderId, in: query, required: true, schema: {type: integer, format: int64}}
              - name: payType
                in: query
                required: true
                description: 支付方式 0->未支付；1->支付宝；2->微信
                schema: {type: integer, enum: [0, 1, 2]}
            responses:
              '200':
                description: 支付结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /order/cancelTimeOutOrder:
          post:
            tags: [Order]
            summary: 自动取消超时订单
            operationId: cancelTimeOutOrder
            security: [{bearerAuth: []}]
            responses:
              '200':
                description: 处理结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /order/cancelOrder:
          post:
            tags: [Order]
            summary: 取消单个超时订单
            operationId: cancelOrder
            security: [{bearerAuth: []}]
            parameters:
              - {name: orderId, in: query, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 处理结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /order/list:
          get:
            tags: [Order]
            summary: 按状态分页获取用户订单列表
            operationId: orderList
            security: [{bearerAuth: []}]
            parameters:
              - name: status
                in: query
                required: true
                description: 订单状态 -1->全部；0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭
                schema: {type: integer, default: -1, enum: [-1, 0, 1, 2, 3, 4]}
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 5}}
            responses:
              '200':
                description: 分页订单列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultCommonPageOmsOrderDetail'}}
        /order/detail/{orderId}:
          get:
            tags: [Order]
            summary: 根据 ID 获取订单详情
            operationId: orderDetail
            security: [{bearerAuth: []}]
            parameters:
              - {name: orderId, in: path, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 订单详情
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultOmsOrderDetail'}}
        /order/cancelUserOrder:
          post:
            tags: [Order]
            summary: 用户取消订单
            operationId: cancelUserOrder
            security: [{bearerAuth: []}]
            parameters:
              - {name: orderId, in: query, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 处理结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /order/confirmReceiveOrder:
          post:
            tags: [Order]
            summary: 用户确认收货
            operationId: confirmReceiveOrder
            security: [{bearerAuth: []}]
            parameters:
              - {name: orderId, in: query, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 处理结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /order/deleteOrder:
          post:
            tags: [Order]
            summary: 用户删除订单
            operationId: deleteOrder
            security: [{bearerAuth: []}]
            parameters:
              - {name: orderId, in: query, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 处理结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /returnApply/create:
          post:
            tags: [ReturnApply]
            summary: 申请退货
            operationId: returnApplyCreate
            security: [{bearerAuth: []}]
            requestBody:
              required: true
              content:
                application/json: {schema: {$ref: '#/components/schemas/OmsOrderReturnApplyParam'}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/attention/add:
          post:
            tags: [MemberAttention]
            summary: 添加品牌关注
            operationId: attentionAdd
            security: [{bearerAuth: []}]
            requestBody:
              required: true
              content:
                application/json: {schema: {$ref: '#/components/schemas/MemberBrandAttention'}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/attention/delete:
          post:
            tags: [MemberAttention]
            summary: 取消品牌关注
            operationId: attentionDelete
            security: [{bearerAuth: []}]
            parameters:
              - {name: brandId, in: query, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/attention/list:
          get:
            tags: [MemberAttention]
            summary: 分页查询当前用户品牌关注列表
            operationId: attentionList
            security: [{bearerAuth: []}]
            parameters:
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 5}}
            responses:
              '200':
                description: 分页关注列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultCommonPageMemberBrandAttention'}}
        /member/attention/detail:
          get:
            tags: [MemberAttention]
            summary: 根据品牌 ID 获取品牌关注详情
            operationId: attentionDetail
            security: [{bearerAuth: []}]
            parameters:
              - {name: brandId, in: query, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 关注详情
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultMemberBrandAttention'}}
        /member/attention/clear:
          post:
            tags: [MemberAttention]
            summary: 清空当前用户品牌关注列表
            operationId: attentionClear
            security: [{bearerAuth: []}]
            responses:
              '200':
                description: 处理结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /member/productCollection/add:
          post:
            tags: [MemberProductCollection]
            summary: 添加商品收藏
            operationId: collectionAdd
            security: [{bearerAuth: []}]
            requestBody:
              required: true
              content:
                application/json: {schema: {$ref: '#/components/schemas/MemberProductCollection'}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/productCollection/delete:
          post:
            tags: [MemberProductCollection]
            summary: 删除商品收藏
            operationId: collectionDelete
            security: [{bearerAuth: []}]
            parameters:
              - {name: productId, in: query, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/productCollection/list:
          get:
            tags: [MemberProductCollection]
            summary: 显示当前用户商品收藏列表
            operationId: collectionList
            security: [{bearerAuth: []}]
            parameters:
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 5}}
            responses:
              '200':
                description: 分页收藏列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultCommonPageMemberProductCollection'}}
        /member/productCollection/detail:
          get:
            tags: [MemberProductCollection]
            summary: 显示商品收藏详情
            operationId: collectionDetail
            security: [{bearerAuth: []}]
            parameters:
              - {name: productId, in: query, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 收藏详情
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultMemberProductCollection'}}
        /member/productCollection/clear:
          post:
            tags: [MemberProductCollection]
            summary: 清空当前用户商品收藏列表
            operationId: collectionClear
            security: [{bearerAuth: []}]
            responses:
              '200':
                description: 处理结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /member/readHistory/create:
          post:
            tags: [MemberReadHistory]
            summary: 创建浏览记录
            operationId: readHistoryCreate
            security: [{bearerAuth: []}]
            requestBody:
              required: true
              content:
                application/json: {schema: {$ref: '#/components/schemas/MemberReadHistory'}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/readHistory/delete:
          post:
            tags: [MemberReadHistory]
            summary: 删除浏览记录
            operationId: readHistoryDelete
            security: [{bearerAuth: []}]
            parameters:
              - name: ids
                in: query
                required: true
                schema: {type: array, items: {type: string}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/readHistory/clear:
          post:
            tags: [MemberReadHistory]
            summary: 清空浏览记录
            operationId: readHistoryClear
            security: [{bearerAuth: []}]
            responses:
              '200':
                description: 处理结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /member/readHistory/list:
          get:
            tags: [MemberReadHistory]
            summary: 分页获取浏览记录
            operationId: readHistoryList
            security: [{bearerAuth: []}]
            parameters:
              - {name: pageNum, in: query, required: false, schema: {type: integer, default: 1}}
              - {name: pageSize, in: query, required: false, schema: {type: integer, default: 5}}
            responses:
              '200':
                description: 分页浏览记录
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultCommonPageMemberReadHistory'}}
        /member/coupon/add/{couponId}:
          post:
            tags: [MemberCoupon]
            summary: 领取指定优惠券
            operationId: couponAdd
            security: [{bearerAuth: []}]
            parameters:
              - {name: couponId, in: path, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 领取结果
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResult'}}
        /member/coupon/listHistory:
          get:
            tags: [MemberCoupon]
            summary: 获取会员优惠券历史列表
            operationId: couponListHistory
            security: [{bearerAuth: []}]
            parameters:
              - name: useStatus
                in: query
                required: false
                description: 优惠券筛选类型 0->未使用；1->已使用；2->已过期
                schema: {type: integer, enum: [0, 1, 2]}
            responses:
              '200':
                description: 优惠券历史列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfSmsCouponHistory'}}
        /member/coupon/list:
          get:
            tags: [MemberCoupon]
            summary: 获取会员优惠券列表
            operationId: couponList
            security: [{bearerAuth: []}]
            parameters:
              - name: useStatus
                in: query
                required: false
                description: 优惠券筛选类型 0->未使用；1->已使用；2->已过期
                schema: {type: integer, enum: [0, 1, 2]}
            responses:
              '200':
                description: 优惠券列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfSmsCoupon'}}
        /member/coupon/list/cart/{type}:
          get:
            tags: [MemberCoupon]
            summary: 获取登录会员购物车的相关优惠券
            operationId: couponListCart
            security: [{bearerAuth: []}]
            parameters:
              - name: type
                in: path
                required: true
                description: 使用可用 0->不可用；1->可用
                schema: {type: integer, default: 1, enum: [0, 1]}
            responses:
              '200':
                description: 购物车可用优惠券列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfSmsCouponHistoryDetail'}}
        /member/coupon/listByProduct/{productId}:
          get:
            tags: [MemberCoupon]
            summary: 获取当前商品相关优惠券
            operationId: couponListByProduct
            security: [{bearerAuth: []}]
            parameters:
              - {name: productId, in: path, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 商品相关优惠券
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfSmsCoupon'}}
        /member/address/add:
          post:
            tags: [MemberAddress]
            summary: 添加收货地址
            operationId: addressAdd
            security: [{bearerAuth: []}]
            requestBody:
              required: true
              content:
                application/json: {schema: {$ref: '#/components/schemas/UmsMemberReceiveAddress'}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/address/delete/{id}:
          post:
            tags: [MemberAddress]
            summary: 删除收货地址
            operationId: addressDelete
            security: [{bearerAuth: []}]
            parameters:
              - {name: id, in: path, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/address/update/{id}:
          post:
            tags: [MemberAddress]
            summary: 修改收货地址
            operationId: addressUpdate
            security: [{bearerAuth: []}]
            parameters:
              - {name: id, in: path, required: true, schema: {type: integer, format: int64}}
            requestBody:
              required: true
              content:
                application/json: {schema: {$ref: '#/components/schemas/UmsMemberReceiveAddress'}}
            responses:
              '200':
                description: 返回受影响行数
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultInteger'}}
        /member/address/list:
          get:
            tags: [MemberAddress]
            summary: 获取所有收货地址
            operationId: addressList
            security: [{bearerAuth: []}]
            responses:
              '200':
                description: 地址列表
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultArrayOfUmsMemberReceiveAddress'}}
        /member/address/{id}:
          get:
            tags: [MemberAddress]
            summary: 获取收货地址详情
            operationId: addressGetItem
            security: [{bearerAuth: []}]
            parameters:
              - {name: id, in: path, required: true, schema: {type: integer, format: int64}}
            responses:
              '200':
                description: 地址详情
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultUmsMemberReceiveAddress'}}
        /alipay/pay:
          get:
            tags: [Alipay]
            summary: 支付宝电脑网站支付
            operationId: alipayPay
            parameters:
              - {name: outTradeNo, in: query, required: false, schema: {type: string}}
              - {name: subject, in: query, required: false, schema: {type: string}}
              - {name: totalAmount, in: query, required: false, schema: {type: number}}
            responses:
              '200':
                description: 返回支付宝支付页面 HTML
                content:
                  text/html: {schema: {type: string}}
        /alipay/webPay:
          get:
            tags: [Alipay]
            summary: 支付宝手机网站支付
            operationId: alipayWebPay
            parameters:
              - {name: outTradeNo, in: query, required: false, schema: {type: string}}
              - {name: subject, in: query, required: false, schema: {type: string}}
              - {name: totalAmount, in: query, required: false, schema: {type: number}}
            responses:
              '200':
                description: 返回支付宝支付页面 HTML
                content:
                  text/html: {schema: {type: string}}
        /alipay/notify:
          post:
            tags: [Alipay]
            summary: 支付宝异步回调
            description: 必须为 POST 请求，执行成功返回 success，执行失败返回 failure
            operationId: alipayNotify
            requestBody:
              content:
                application/x-www-form-urlencoded:
                  schema: {type: object, additionalProperties: {type: string}}
            responses:
              '200':
                description: success / failure
                content:
                  text/plain: {schema: {type: string}}
        /alipay/query:
          get:
            tags: [Alipay]
            summary: 支付宝统一收单线下交易查询
            description: 订单支付成功返回交易状态 TRADE_SUCCESS
            operationId: alipayQuery
            parameters:
              - {name: outTradeNo, in: query, required: false, schema: {type: string}}
              - {name: tradeNo, in: query, required: false, schema: {type: string}}
            responses:
              '200':
                description: 交易状态
                content:
                  application/json: {schema: {$ref: '#/components/schemas/CommonResultString'}}

      components:
        securitySchemes:
          bearerAuth:
            type: http
            scheme: bearer
            bearerFormat: JWT
            description: Header 名与前缀取自 jwt.tokenHeader / jwt.tokenHead（默认 Authorization / "Bearer "）

        schemas:
          CommonResult:
            type: object
            description: 通用返回结果封装 com.macro.mall.common.api.CommonResult
            properties:
              code: {type: integer, format: int64, description: 状态码，成功 200}
              message: {type: string, description: 提示信息}
              data: {nullable: true, description: 数据封装}
          CommonResultString:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: string}}
          CommonResultInteger:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: integer}}
          CommonResultMap:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: object, additionalProperties: true}}
          CommonResultToken:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties:
                  data:
                    type: object
                    properties:
                      token: {type: string, description: JWT token}
                      tokenHead: {type: string, description: token 前缀，默认 "Bearer "}
          CommonResultUmsMember:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/UmsMember'}}
          CommonResultHomeContentResult:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/HomeContentResult'}}
          CommonResultPmsPortalProductDetail:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/PmsPortalProductDetail'}}
          CommonResultPmsBrand:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/PmsBrand'}}
          CommonResultCartProduct:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/CartProduct'}}
          CommonResultConfirmOrderResult:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/ConfirmOrderResult'}}
          CommonResultOmsOrderDetail:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/OmsOrderDetail'}}
          CommonResultMemberBrandAttention:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/MemberBrandAttention'}}
          CommonResultMemberProductCollection:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/MemberProductCollection'}}
          CommonResultUmsMemberReceiveAddress:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/UmsMemberReceiveAddress'}}
          CommonResultArrayOfPmsProduct:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/PmsProduct'}}}
          CommonResultArrayOfPmsBrand:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/PmsBrand'}}}
          CommonResultArrayOfPmsProductCategory:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/PmsProductCategory'}}}
          CommonResultArrayOfPmsProductCategoryNode:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/PmsProductCategoryNode'}}}
          CommonResultArrayOfCmsSubject:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/CmsSubject'}}}
          CommonResultArrayOfOmsCartItem:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/OmsCartItem'}}}
          CommonResultArrayOfCartPromotionItem:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/CartPromotionItem'}}}
          CommonResultArrayOfSmsCoupon:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/SmsCoupon'}}}
          CommonResultArrayOfSmsCouponHistory:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/SmsCouponHistory'}}}
          CommonResultArrayOfSmsCouponHistoryDetail:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/SmsCouponHistoryDetail'}}}
          CommonResultArrayOfUmsMemberReceiveAddress:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {type: array, items: {$ref: '#/components/schemas/UmsMemberReceiveAddress'}}}
          CommonResultCommonPagePmsProduct:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/CommonPagePmsProduct'}}
          CommonResultCommonPageOmsOrderDetail:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/CommonPageOmsOrderDetail'}}
          CommonResultCommonPageMemberBrandAttention:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/CommonPageMemberBrandAttention'}}
          CommonResultCommonPageMemberProductCollection:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/CommonPageMemberProductCollection'}}
          CommonResultCommonPageMemberReadHistory:
            allOf:
              - $ref: '#/components/schemas/CommonResult'
              - type: object
                properties: {data: {$ref: '#/components/schemas/CommonPageMemberReadHistory'}}

          CommonPage:
            type: object
            description: 通用分页数据封装 com.macro.mall.common.api.CommonPage
            properties:
              pageNum: {type: integer, description: 当前页码}
              pageSize: {type: integer, description: 每页数量}
              totalPage: {type: integer, description: 总页数}
              total: {type: integer, format: int64, description: 总条数}
              list: {type: array, items: {}}
          CommonPagePmsProduct:
            allOf:
              - $ref: '#/components/schemas/CommonPage'
              - type: object
                properties: {list: {type: array, items: {$ref: '#/components/schemas/PmsProduct'}}}
          CommonPageOmsOrderDetail:
            allOf:
              - $ref: '#/components/schemas/CommonPage'
              - type: object
                properties: {list: {type: array, items: {$ref: '#/components/schemas/OmsOrderDetail'}}}
          CommonPageMemberBrandAttention:
            allOf:
              - $ref: '#/components/schemas/CommonPage'
              - type: object
                properties: {list: {type: array, items: {$ref: '#/components/schemas/MemberBrandAttention'}}}
          CommonPageMemberProductCollection:
            allOf:
              - $ref: '#/components/schemas/CommonPage'
              - type: object
                properties: {list: {type: array, items: {$ref: '#/components/schemas/MemberProductCollection'}}}
          CommonPageMemberReadHistory:
            allOf:
              - $ref: '#/components/schemas/CommonPage'
              - type: object
                properties: {list: {type: array, items: {$ref: '#/components/schemas/MemberReadHistory'}}}

          UmsMember:
            type: object
            properties:
              id: {type: integer, format: int64}
              memberLevelId: {type: integer, format: int64}
              username: {type: string}
              password: {type: string}
              nickname: {type: string}
              phone: {type: string}
              status: {type: integer, description: 帐号启用状态 0->禁用；1->启用}
              createTime: {type: string, format: date-time}
              icon: {type: string}
              gender: {type: integer, description: 性别 0->未知；1->男；2->女}
              birthday: {type: string, format: date-time}
              city: {type: string}
              job: {type: string}
              personalizedSignature: {type: string}
              sourceType: {type: integer}
              integration: {type: integer, description: 积分}
              growth: {type: integer, description: 成长值}
              luckeyCount: {type: integer}
              historyIntegration: {type: integer}
          UmsMemberReceiveAddress:
            type: object
            properties:
              id: {type: integer, format: int64}
              memberId: {type: integer, format: int64}
              name: {type: string, description: 收货人名称}
              phoneNumber: {type: string}
              defaultStatus: {type: integer, description: 是否为默认}
              postCode: {type: string}
              province: {type: string}
              city: {type: string}
              region: {type: string}
              detailAddress: {type: string, description: 详细地址(街道)}

          PmsProduct:
            type: object
            properties:
              id: {type: integer, format: int64}
              brandId: {type: integer, format: int64}
              productCategoryId: {type: integer, format: int64}
              feightTemplateId: {type: integer, format: int64}
              productAttributeCategoryId: {type: integer, format: int64}
              name: {type: string}
              pic: {type: string}
              productSn: {type: string, description: 货号}
              deleteStatus: {type: integer}
              publishStatus: {type: integer, description: 上架状态 0->下架；1->上架}
              newStatus: {type: integer, description: 新品状态 0->不是新品；1->新品}
              recommandStatus: {type: integer, description: 推荐状态 0->不推荐；1->推荐}
              verifyStatus: {type: integer, description: 审核状态 0->未审核；1->审核通过}
              sort: {type: integer}
              sale: {type: integer, description: 销量}
              price: {type: number}
              promotionPrice: {type: number}
              giftGrowth: {type: integer}
              giftPoint: {type: integer}
              usePointLimit: {type: integer}
              subTitle: {type: string}
              originalPrice: {type: number}
              stock: {type: integer}
              lowStock: {type: integer}
              unit: {type: string}
              weight: {type: number}
              previewStatus: {type: integer}
              serviceIds: {type: string}
              keywords: {type: string}
              note: {type: string}
              albumPics: {type: string}
              detailTitle: {type: string}
              promotionStartTime: {type: string, format: date-time}
              promotionEndTime: {type: string, format: date-time}
              promotionPerLimit: {type: integer}
              promotionType: {type: integer}
              brandName: {type: string}
              productCategoryName: {type: string}
              description: {type: string}
              detailDesc: {type: string}
              detailHtml: {type: string}
              detailMobileHtml: {type: string}
          PmsBrand:
            type: object
            properties:
              id: {type: integer, format: int64}
              name: {type: string}
              firstLetter: {type: string}
              sort: {type: integer}
              factoryStatus: {type: integer, description: 是否为品牌制造商 0->不是；1->是}
              showStatus: {type: integer}
              productCount: {type: integer}
              productCommentCount: {type: integer}
              logo: {type: string}
              bigPic: {type: string}
              brandStory: {type: string}
          PmsProductCategory:
            type: object
            properties:
              id: {type: integer, format: int64}
              parentId: {type: integer, format: int64, description: 上级分类编号，0 表示一级分类}
              name: {type: string}
              level: {type: integer, description: 分类级别 0->1级；1->2级}
              productCount: {type: integer}
              productUnit: {type: string}
              navStatus: {type: integer}
              showStatus: {type: integer}
              sort: {type: integer}
              icon: {type: string}
              keywords: {type: string}
              description: {type: string}
          PmsProductCategoryNode:
            allOf:
              - $ref: '#/components/schemas/PmsProductCategory'
              - type: object
                properties:
                  children: {type: array, items: {$ref: '#/components/schemas/PmsProductCategoryNode'}}
          CmsSubject:
            type: object
            properties:
              id: {type: integer, format: int64}
              categoryId: {type: integer, format: int64}
              title: {type: string}
              pic: {type: string}
              productCount: {type: integer}
              recommendStatus: {type: integer}
              createTime: {type: string, format: date-time}
              collectCount: {type: integer}
              readCount: {type: integer}
              commentCount: {type: integer}
              albumPics: {type: string}
              description: {type: string}
              showStatus: {type: integer}
              forwardCount: {type: integer}
              categoryName: {type: string}
              content: {type: string}

          OmsCartItem:
            type: object
            description: com.macro.mall.model.OmsCartItem
            properties:
              id: {type: integer, format: int64}
              productId: {type: integer, format: int64}
              productSkuId: {type: integer, format: int64}
              memberId: {type: integer, format: int64}
              quantity: {type: integer, description: 购买数量}
              price: {type: number}
              productPic: {type: string}
              productName: {type: string}
              productSubTitle: {type: string}
              productSkuCode: {type: string}
              memberNickname: {type: string}
              createDate: {type: string, format: date-time}
              modifyDate: {type: string, format: date-time}
              deleteStatus: {type: integer}
              productCategoryId: {type: integer, format: int64}
              productBrand: {type: string}
              productSn: {type: string}
              productAttr: {type: string, description: 商品销售属性}
          CartPromotionItem:
            allOf:
              - $ref: '#/components/schemas/OmsCartItem'
              - type: object
                properties:
                  promotionMessage: {type: string}
                  reduceAmount: {type: number}
                  realStock: {type: integer}
                  integration: {type: integer}
                  growth: {type: integer}
          CartProduct:
            allOf:
              - $ref: '#/components/schemas/PmsProduct'
              - type: object
                properties:
                  productAttributeList: {type: array, items: {$ref: '#/components/schemas/JavaModelPmsProductAttribute'}}
                  skuStockList: {type: array, items: {$ref: '#/components/schemas/JavaModelPmsSkuStock'}}
          OrderParam:
            type: object
            properties:
              memberReceiveAddressId: {type: integer, format: int64, description: 收货地址ID}
              couponId: {type: integer, format: int64, description: 优惠券ID}
              useIntegration: {type: integer, description: 使用的积分数}
              payType: {type: integer, description: 支付方式}
              cartIds: {type: array, items: {type: integer, format: int64}, description: 被选中的购物车商品ID}
          ConfirmOrderResult:
            type: object
            properties:
              cartPromotionItemList: {type: array, items: {$ref: '#/components/schemas/CartPromotionItem'}}
              memberReceiveAddressList: {type: array, items: {$ref: '#/components/schemas/UmsMemberReceiveAddress'}}
              couponHistoryDetailList: {type: array, items: {$ref: '#/components/schemas/SmsCouponHistoryDetail'}}
              integrationConsumeSetting: {$ref: '#/components/schemas/JavaModelUmsIntegrationConsumeSetting'}
              memberIntegration: {type: integer, description: 会员持有的积分}
              calcAmount: {$ref: '#/components/schemas/CalcAmount'}
          CalcAmount:
            type: object
            properties:
              totalAmount: {type: number, description: 订单商品总金额}
              freightAmount: {type: number, description: 运费}
              promotionAmount: {type: number, description: 活动优惠}
              payAmount: {type: number, description: 应付金额}
          OmsOrderDetail:
            allOf:
              - $ref: '#/components/schemas/JavaModelOmsOrder'
              - type: object
                properties:
                  orderItemList: {type: array, items: {$ref: '#/components/schemas/JavaModelOmsOrderItem'}}
          OmsOrderReturnApplyParam:
            type: object
            properties:
              orderId: {type: integer, format: int64}
              productId: {type: integer, format: int64}
              orderSn: {type: string}
              memberUsername: {type: string}
              returnName: {type: string}
              returnPhone: {type: string}
              productPic: {type: string}
              productName: {type: string}
              productBrand: {type: string}
              productAttr: {type: string}
              productCount: {type: integer}
              productPrice: {type: number}
              productRealPrice: {type: number}
              reason: {type: string}
              description: {type: string}
              proofPics: {type: string, description: 凭证图片，以逗号隔开}
          MemberBrandAttention:
            type: object
            properties:
              id: {type: string}
              memberId: {type: integer, format: int64}
              memberNickname: {type: string}
              memberIcon: {type: string}
              brandId: {type: integer, format: int64}
              brandName: {type: string}
              brandLogo: {type: string}
              brandCity: {type: string}
              createTime: {type: string, format: date-time}
          MemberProductCollection:
            type: object
            properties:
              id: {type: string}
              memberId: {type: integer, format: int64}
              memberNickname: {type: string}
              memberIcon: {type: string}
              productId: {type: integer, format: int64}
              productName: {type: string}
              productPic: {type: string}
              productSubTitle: {type: string}
              productPrice: {type: string}
              createTime: {type: string, format: date-time}
          MemberReadHistory:
            type: object
            properties:
              id: {type: string}
              memberId: {type: integer, format: int64}
              memberNickname: {type: string}
              memberIcon: {type: string}
              productId: {type: integer, format: int64}
              productName: {type: string}
              productPic: {type: string}
              productSubTitle: {type: string}
              productPrice: {type: string}
              createTime: {type: string, format: date-time}
          SmsCoupon:
            type: object
            properties:
              id: {type: integer, format: int64}
              type: {type: integer, description: 0->全场赠券；1->会员赠券；2->购物赠券；3->注册赠券}
              name: {type: string}
              platform: {type: integer, description: 0->全部；1->移动；2->PC}
              count: {type: integer}
              amount: {type: number}
              perLimit: {type: integer}
              minPoint: {type: number, description: 使用门槛，0 表示无门槛}
              startTime: {type: string, format: date-time}
              endTime: {type: string, format: date-time}
              useType: {type: integer, description: 0->全场通用；1->指定分类；2->指定商品}
              note: {type: string}
              publishCount: {type: integer}
              useCount: {type: integer}
              receiveCount: {type: integer}
              enableTime: {type: string, format: date-time}
              code: {type: string}
              memberLevel: {type: integer}
          SmsCouponHistory:
            type: object
            description: 字段未逐字段展开，结构见 com.macro.mall.model.SmsCouponHistory
            additionalProperties: true
          SmsCouponHistoryDetail:
            allOf:
              - $ref: '#/components/schemas/SmsCouponHistory'
              - type: object
                properties:
                  coupon: {$ref: '#/components/schemas/SmsCoupon'}
                  productRelationList: {type: array, items: {$ref: '#/components/schemas/JavaModelSmsCouponProductRelation'}}
                  categoryRelationList: {type: array, items: {$ref: '#/components/schemas/JavaModelSmsCouponProductCategoryRelation'}}
          HomeContentResult:
            type: object
            properties:
              advertiseList: {type: array, items: {$ref: '#/components/schemas/JavaModelSmsHomeAdvertise'}}
              brandList: {type: array, items: {$ref: '#/components/schemas/PmsBrand'}}
              homeFlashPromotion: {$ref: '#/components/schemas/HomeFlashPromotion'}
              newProductList: {type: array, items: {$ref: '#/components/schemas/PmsProduct'}}
              hotProductList: {type: array, items: {$ref: '#/components/schemas/PmsProduct'}}
              subjectList: {type: array, items: {$ref: '#/components/schemas/CmsSubject'}}
          HomeFlashPromotion:
            type: object
            properties:
              startTime: {type: string, format: date-time}
              endTime: {type: string, format: date-time}
              nextStartTime: {type: string, format: date-time}
              nextEndTime: {type: string, format: date-time}
              productList: {type: array, items: {$ref: '#/components/schemas/JavaModelFlashPromotionProduct'}}
          PmsPortalProductDetail:
            type: object
            properties:
              product: {$ref: '#/components/schemas/PmsProduct'}
              brand: {$ref: '#/components/schemas/PmsBrand'}
              productAttributeList: {type: array, items: {$ref: '#/components/schemas/JavaModelPmsProductAttribute'}}
              productAttributeValueList: {type: array, items: {$ref: '#/components/schemas/JavaModelPmsProductAttributeValue'}}
              skuStockList: {type: array, items: {$ref: '#/components/schemas/JavaModelPmsSkuStock'}}
              productLadderList: {type: array, items: {$ref: '#/components/schemas/JavaModelPmsProductLadder'}}
              productFullReductionList: {type: array, items: {$ref: '#/components/schemas/JavaModelPmsProductFullReduction'}}
              couponList: {type: array, items: {$ref: '#/components/schemas/SmsCoupon'}}
          JavaModelPmsProductAttribute: {type: object, additionalProperties: true, description: com.macro.mall.model.PmsProductAttribute}
          JavaModelPmsProductAttributeValue: {type: object, additionalProperties: true, description: com.macro.mall.model.PmsProductAttributeValue}
          JavaModelPmsSkuStock: {type: object, additionalProperties: true, description: com.macro.mall.model.PmsSkuStock}
          JavaModelPmsProductLadder: {type: object, additionalProperties: true, description: com.macro.mall.model.PmsProductLadder}
          JavaModelPmsProductFullReduction: {type: object, additionalProperties: true, description: com.macro.mall.model.PmsProductFullReduction}
          JavaModelSmsHomeAdvertise: {type: object, additionalProperties: true, description: com.macro.mall.model.SmsHomeAdvertise}
          JavaModelFlashPromotionProduct: {type: object, additionalProperties: true, description: com.macro.mall.portal.domain.FlashPromotionProduct}
          JavaModelUmsIntegrationConsumeSetting: {type: object, additionalProperties: true, description: com.macro.mall.model.UmsIntegrationConsumeSetting}
          JavaModelSmsCouponProductRelation: {type: object, additionalProperties: true, description: com.macro.mall.model.SmsCouponProductRelation}
          JavaModelSmsCouponProductCategoryRelation: {type: object, additionalProperties: true, description: com.macro.mall.model.SmsCouponProductCategoryRelation}
          JavaModelOmsOrder: {type: object, additionalProperties: true, description: com.macro.mall.model.OmsOrder}
          JavaModelOmsOrderItem: {type: object, additionalProperties: true, description: com.macro.mall.model.OmsOrderItem}
      """
    那么 该文档应可通过 OpenAPI 3.0.3 解析
    而且 文档中的接口路径应至少包含 50 个
    而且 文档中应包含安全方案 "bearerAuth"