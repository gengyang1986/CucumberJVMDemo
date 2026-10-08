package com.macro.mall.contract;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.Map;

/**
 * mall-portal API 契约校验步骤定义。
 *
 * <p>契约来源：back 仓 master 分支 mall-portal 模块的 Controller 源码
 * （@Tag/@Operation/@RequestMapping/@RequestParam/@RequestBody），
 * 生成的 OpenAPI 3.0.3 原文以常量 {@link #OPENAPI_YAML} 形式内嵌在本类中，
 * 与用例同分支维护，便于评审与 diff。
 *
 * <p>需要登录的接口 token 由 POST /sso/login 获取；
 * 账号通过 -Dmall.member.username / -Dmall.member.password 传入，不硬编码在仓库里。
 */
public class MallPortalApiContractSteps {

    /** 由 back@master 的 mall-portal 模块源码生成的 OpenAPI 3.0.3 契约原文。 */
    public static final String OPENAPI_YAML = """
openapi: 3.0.3
info:
  title: mall-portal 前台接口（由 back@master 源码生成）
  description: |
    依据 back 仓 master 分支 mall-portal 模块 Controller 源码解析生成（@Tag/@Operation/@RequestMapping/@RequestParam/@RequestBody）。
    统一响应体 CommonResult{ code, message, data }；业务码：200 成功、404 参数校验失败或登录失败、401 未登录/token 过期、403 无权限、500 操作失败。
    鉴权：需登录接口在请求头携带 Authorization: Bearer {token}，token 取自 POST /sso/login 返回的 data.token。
    服务端口 8085 来自 mall-portal/src/main/resources/application-dev.yml（未配置 context-path）。
  version: 1.0.0
servers:
  - url: http://localhost:8085
    description: 本地开发环境（mall-portal）
security:
  - bearerAuth: []
tags:
  - { name: HomeController, description: 首页内容管理（/home，免登录） }
  - { name: UmsMemberController, description: 会员登录注册管理（/sso） }
  - { name: UmsMemberReceiveAddressController, description: 会员收货地址管理（/member/address） }
  - { name: UmsMemberCouponController, description: 用户优惠券管理（/member/coupon） }
  - { name: MemberAttentionController, description: 会员关注品牌管理（/member/attention） }
  - { name: MemberProductCollectionController, description: 会员商品收藏管理（/member/productCollection） }
  - { name: MemberReadHistoryController, description: 会员商品浏览记录管理（/member/readHistory） }
  - { name: OmsCartItemController, description: 购物车管理（/cart） }
  - { name: OmsPortalOrderController, description: 订单管理（/order） }
  - { name: OmsPortalOrderReturnApplyController, description: 退货申请管理（/returnApply） }
  - { name: PmsPortalBrandController, description: 前台品牌管理（/brand，免登录） }
  - { name: PmsPortalProductController, description: 前台商品管理（/product，免登录） }
  - { name: AlipayController, description: 支付宝支付相关接口（/alipay，免登录） }
paths:
  /home/content:
    get:
      tags: [HomeController]
      summary: 首页内容信息展示
      security: []
      responses:
        '200':
          description: 成功，聚合轮播广告、推荐品牌、秒杀场次、新品、人气、专题
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/HomeContentResult' }
  /home/recommendProductList:
    get:
      tags: [HomeController]
      summary: 分页获取推荐商品
      security: []
      parameters:
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 4 } }
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/PmsProduct' } }
  /home/productCateList/{parentId}:
    get:
      tags: [HomeController]
      summary: 获取首页商品分类
      security: []
      parameters:
        - { name: parentId, in: path, required: true, description: '父级分类 ID，0 表示一级分类', schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/PmsProductCategory' } }
  /home/subjectList:
    get:
      tags: [HomeController]
      summary: 根据分类获取专题
      security: []
      parameters:
        - { name: cateId, in: query, required: false, description: '专题分类 ID，不传表示全部', schema: { type: integer, format: int64 } }
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 4 } }
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/CmsSubject' } }
  /home/hotProductList:
    get:
      tags: [HomeController]
      summary: 分页获取人气推荐商品
      security: []
      parameters:
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 6 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/PmsProduct' } }
  /home/newProductList:
    get:
      tags: [HomeController]
      summary: 分页获取新品推荐商品
      security: []
      parameters:
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 6 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/PmsProduct' } }
  /sso/register:
    post:
      tags: [UmsMemberController]
      summary: Member register
      description: 参数为 @RequestParam，可使用 query 或 application/x-www-form-urlencoded 传入。
      security: []
      parameters:
        - { name: username, in: query, required: true, schema: { type: string } }
        - { name: password, in: query, required: true, schema: { type: string } }
        - { name: telephone, in: query, required: true, schema: { type: string } }
        - { name: authCode, in: query, required: true, description: '短信验证码（/sso/getAuthCode 获取）', schema: { type: string } }
      responses:
        '200':
          description: 成功时 message 为“注册成功”，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /sso/login:
    post:
      tags: [UmsMemberController]
      summary: Member login
      description: 参数为 @RequestParam，可使用 query 或 form-urlencoded 传入；用户名或密码错误时返回 code=404、message=“用户名或密码错误”。
      security: []
      parameters:
        - { name: username, in: query, required: true, schema: { type: string } }
        - { name: password, in: query, required: true, schema: { type: string } }
      responses:
        '200':
          description: 成功返回 token 与 tokenHead
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data:
                        type: object
                        properties:
                          token: { type: string }
                          tokenHead: { type: string, example: 'Bearer ' }
  /sso/info:
    get:
      tags: [UmsMemberController]
      summary: 获取会员信息
      responses:
        '200':
          description: 成功；未登录（Principal 为空）时返回 CommonResult.code=401、data 为空
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/UmsMember' }
  /sso/getAuthCode:
    get:
      tags: [UmsMemberController]
      summary: 获取验证码
      security: []
      parameters:
        - { name: telephone, in: query, required: true, schema: { type: string } }
      responses:
        '200':
          description: 成功，data 为短信验证码
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: string }
  /sso/updatePassword:
    post:
      tags: [UmsMemberController]
      summary: 会员修改密码
      security: []
      parameters:
        - { name: telephone, in: query, required: true, schema: { type: string } }
        - { name: password, in: query, required: true, schema: { type: string } }
        - { name: authCode, in: query, required: true, schema: { type: string } }
      responses:
        '200':
          description: 成功时 message 为“密码修改成功”，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /sso/refreshToken:
    get:
      tags: [UmsMemberController]
      summary: 刷新 token
      description: 从请求头 Authorization（jwt.tokenHeader）中读取旧 token；token 过期返回 code=500、message=“token已经过期！”。
      parameters:
        - { name: Authorization, in: header, required: true, description: '旧 token（含 tokenHead 前缀）', schema: { type: string } }
      responses:
        '200':
          description: 成功返回新 token 与 tokenHead
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data:
                        type: object
                        properties:
                          token: { type: string }
                          tokenHead: { type: string, example: 'Bearer ' }
  /member/address/add:
    post:
      tags: [UmsMemberReceiveAddressController]
      summary: 添加收货地址
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/UmsMemberReceiveAddress' }
      responses:
        '200':
          description: 成功返回影响行数，失败返回 code=500
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /member/address/delete/{id}:
    post:
      tags: [UmsMemberReceiveAddressController]
      summary: 删除收货地址
      parameters:
        - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功返回影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /member/address/update/{id}:
    post:
      tags: [UmsMemberReceiveAddressController]
      summary: 修改收货地址
      parameters:
        - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/UmsMemberReceiveAddress' }
      responses:
        '200':
          description: 成功返回影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /member/address/list:
    get:
      tags: [UmsMemberReceiveAddressController]
      summary: 获取所有收货地址
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/UmsMemberReceiveAddress' } }
  /member/address/{id}:
    get:
      tags: [UmsMemberReceiveAddressController]
      summary: 获取收货地址详情
      parameters:
        - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/UmsMemberReceiveAddress' }
  /member/coupon/add/{couponId}:
    post:
      tags: [UmsMemberCouponController]
      summary: 领取指定优惠券
      parameters:
        - { name: couponId, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功时 message 为“领取成功”，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /member/coupon/listHistory:
    get:
      tags: [UmsMemberCouponController]
      summary: 获取会员优惠券历史列表
      parameters:
        - name: useStatus
          in: query
          required: false
          description: '优惠券筛选类型：0->未使用；1->已使用；2->已过期'
          schema: { type: integer, format: int32, enum: [0, 1, 2] }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/SmsCouponHistory' } }
  /member/coupon/list:
    get:
      tags: [UmsMemberCouponController]
      summary: 获取会员优惠券列表
      parameters:
        - name: useStatus
          in: query
          required: false
          description: '优惠券筛选类型：0->未使用；1->已使用；2->已过期'
          schema: { type: integer, format: int32, enum: [0, 1, 2] }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/SmsCoupon' } }
  /member/coupon/list/cart/{type}:
    get:
      tags: [UmsMemberCouponController]
      summary: 获取登录会员购物车的相关优惠券
      parameters:
        - name: type
          in: path
          required: true
          description: '使用可用：0->不可用；1->可用'
          schema: { type: integer, format: int32, default: 1, enum: [0, 1] }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/SmsCouponHistoryDetail' } }
  /member/coupon/listByProduct/{productId}:
    get:
      tags: [UmsMemberCouponController]
      summary: 获取当前商品相关优惠券
      parameters:
        - { name: productId, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/SmsCoupon' } }
  /member/attention/add:
    post:
      tags: [MemberAttentionController]
      summary: 添加品牌关注
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/MemberBrandAttention' }
      responses:
        '200':
          description: 成功返回影响行数，失败返回 code=500
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /member/attention/delete:
    post:
      tags: [MemberAttentionController]
      summary: 取消品牌关注
      parameters:
        - { name: brandId, in: query, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功返回影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /member/attention/list:
    get:
      tags: [MemberAttentionController]
      summary: 分页查询当前用户品牌关注列表
      parameters:
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 5 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data:
                        allOf:
                          - $ref: '#/components/schemas/CommonPage'
                          - type: object
                            properties:
                              list: { type: array, items: { $ref: '#/components/schemas/MemberBrandAttention' } }
  /member/attention/detail:
    get:
      tags: [MemberAttentionController]
      summary: 根据品牌ID获取品牌关注详情
      parameters:
        - { name: brandId, in: query, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/MemberBrandAttention' }
  /member/attention/clear:
    post:
      tags: [MemberAttentionController]
      summary: 清空当前用户品牌关注列表
      responses:
        '200':
          description: 成功，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /member/productCollection/add:
    post:
      tags: [MemberProductCollectionController]
      summary: 添加商品收藏
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/MemberProductCollection' }
      responses:
        '200':
          description: 成功返回影响行数，失败返回 code=500
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /member/productCollection/delete:
    post:
      tags: [MemberProductCollectionController]
      summary: 删除商品收藏
      parameters:
        - { name: productId, in: query, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功返回影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /member/productCollection/list:
    get:
      tags: [MemberProductCollectionController]
      summary: 显示当前用户商品收藏列表
      parameters:
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 5 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data:
                        allOf:
                          - $ref: '#/components/schemas/CommonPage'
                          - type: object
                            properties:
                              list: { type: array, items: { $ref: '#/components/schemas/MemberProductCollection' } }
  /member/productCollection/detail:
    get:
      tags: [MemberProductCollectionController]
      summary: 显示商品收藏详情
      parameters:
        - { name: productId, in: query, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/MemberProductCollection' }
  /member/productCollection/clear:
    post:
      tags: [MemberProductCollectionController]
      summary: 清空当前用户商品收藏列表
      responses:
        '200':
          description: 成功，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /member/readHistory/create:
    post:
      tags: [MemberReadHistoryController]
      summary: 创建浏览记录
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/MemberReadHistory' }
      responses:
        '200':
          description: 成功返回影响行数，失败返回 code=500
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /member/readHistory/delete:
    post:
      tags: [MemberReadHistoryController]
      summary: 删除浏览记录
      parameters:
        - name: ids
          in: query
          required: true
          description: '浏览记录 ID 列表，可重复传参（ids=1&ids=2）'
          schema: { type: array, items: { type: string } }
          style: form
          explode: true
      responses:
        '200':
          description: 成功返回影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /member/readHistory/clear:
    post:
      tags: [MemberReadHistoryController]
      summary: 清空浏览记录
      responses:
        '200':
          description: 成功，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /member/readHistory/list:
    get:
      tags: [MemberReadHistoryController]
      summary: 分页获取浏览记录
      parameters:
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 5 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data:
                        allOf:
                          - $ref: '#/components/schemas/CommonPage'
                          - type: object
                            properties:
                              list: { type: array, items: { $ref: '#/components/schemas/MemberReadHistory' } }
  /cart/add:
    post:
      tags: [OmsCartItemController]
      summary: 添加商品到购物车
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/OmsCartItem' }
      responses:
        '200':
          description: 成功返回影响行数，失败返回 code=500
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /cart/list:
    get:
      tags: [OmsCartItemController]
      summary: 获取当前会员的购物车列表
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/OmsCartItem' } }
  /cart/list/promotion:
    get:
      tags: [OmsCartItemController]
      summary: 获取当前会员的购物车列表，包括促销信息
      parameters:
        - name: cartIds
          in: query
          required: false
          description: '指定的购物车商品 ID 列表，不传表示全部'
          schema: { type: array, items: { type: integer, format: int64 } }
          style: form
          explode: true
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/CartPromotionItem' } }
  /cart/update/quantity:
    get:
      tags: [OmsCartItemController]
      summary: 修改购物车中指定商品的数量
      parameters:
        - { name: id, in: query, required: true, description: '购物车商品 ID', schema: { type: integer, format: int64 } }
        - { name: quantity, in: query, required: true, schema: { type: integer, format: int32 } }
      responses:
        '200':
          description: 成功返回影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /cart/getProduct/{productId}:
    get:
      tags: [OmsCartItemController]
      summary: 获取购物车中指定商品的规格，用于重选规格
      parameters:
        - { name: productId, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/CartProduct' }
  /cart/update/attr:
    post:
      tags: [OmsCartItemController]
      summary: 修改购物车中商品的规格
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/OmsCartItem' }
      responses:
        '200':
          description: 成功返回影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /cart/delete:
    post:
      tags: [OmsCartItemController]
      summary: 删除购物车中的指定商品
      parameters:
        - name: ids
          in: query
          required: true
          schema: { type: array, items: { type: integer, format: int64 } }
          style: form
          explode: true
      responses:
        '200':
          description: 成功返回影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /cart/clear:
    post:
      tags: [OmsCartItemController]
      summary: 清空当前会员的购物车
      responses:
        '200':
          description: 成功返回影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /order/generateConfirmOrder:
    post:
      tags: [OmsPortalOrderController]
      summary: 根据购物车信息生成确认单
      requestBody:
        required: true
        description: 被选中的购物车商品 ID 列表
        content:
          application/json:
            schema: { type: array, items: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/ConfirmOrderResult' }
  /order/generateOrder:
    post:
      tags: [OmsPortalOrderController]
      summary: 根据购物车信息生成订单
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/OrderParam' }
      responses:
        '200':
          description: 成功时 message 为“下单成功”，data 为下单结果 Map（OmsPortalOrderService.generateOrder 返回）
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: object, additionalProperties: true }
  /order/paySuccess:
    post:
      tags: [OmsPortalOrderController]
      summary: 用户支付成功的回调
      parameters:
        - { name: orderId, in: query, required: true, schema: { type: integer, format: int64 } }
        - { name: payType, in: query, required: true, schema: { type: integer, format: int32 } }
      responses:
        '200':
          description: 成功时 message 为“支付成功”，data 为影响行数
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /order/cancelTimeOutOrder:
    post:
      tags: [OmsPortalOrderController]
      summary: 自动取消超时订单
      responses:
        '200':
          description: 成功，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /order/cancelOrder:
    post:
      tags: [OmsPortalOrderController]
      summary: 取消单个超时订单
      parameters:
        - { name: orderId, in: query, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /order/list:
    get:
      tags: [OmsPortalOrderController]
      summary: 按状态分页获取用户订单列表
      parameters:
        - name: status
          in: query
          required: true
          description: '订单状态：-1->全部；0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭'
          schema: { type: integer, format: int32, default: -1, enum: [-1, 0, 1, 2, 3, 4] }
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 5 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data:
                        allOf:
                          - $ref: '#/components/schemas/CommonPage'
                          - type: object
                            properties:
                              list: { type: array, items: { $ref: '#/components/schemas/OmsOrderDetail' } }
  /order/detail/{orderId}:
    get:
      tags: [OmsPortalOrderController]
      summary: 根据ID获取订单详情
      parameters:
        - { name: orderId, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/OmsOrderDetail' }
  /order/cancelUserOrder:
    post:
      tags: [OmsPortalOrderController]
      summary: 用户取消订单
      parameters:
        - { name: orderId, in: query, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /order/confirmReceiveOrder:
    post:
      tags: [OmsPortalOrderController]
      summary: 用户确认收货
      parameters:
        - { name: orderId, in: query, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /order/deleteOrder:
    post:
      tags: [OmsPortalOrderController]
      summary: 用户删除订单
      parameters:
        - { name: orderId, in: query, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功，data 为 null
          content:
            application/json:
              schema: { $ref: '#/components/schemas/CommonResult' }
  /returnApply/create:
    post:
      tags: [OmsPortalOrderReturnApplyController]
      summary: 申请退货
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/OmsOrderReturnApplyParam' }
      responses:
        '200':
          description: 成功返回影响行数，失败返回 code=500
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: integer, format: int32 }
  /brand/recommendList:
    get:
      tags: [PmsPortalBrandController]
      summary: 分页获取推荐品牌
      security: []
      parameters:
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 6 } }
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/PmsBrand' } }
  /brand/detail/{brandId}:
    get:
      tags: [PmsPortalBrandController]
      summary: 获取品牌详情
      security: []
      parameters:
        - { name: brandId, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/PmsBrand' }
  /brand/productList:
    get:
      tags: [PmsPortalBrandController]
      summary: 分页获取品牌相关商品
      security: []
      parameters:
        - { name: brandId, in: query, required: true, schema: { type: integer, format: int64 } }
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 1 } }
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 6 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data:
                        allOf:
                          - $ref: '#/components/schemas/CommonPage'
                          - type: object
                            properties:
                              list: { type: array, items: { $ref: '#/components/schemas/PmsProduct' } }
  /product/search:
    get:
      tags: [PmsPortalProductController]
      summary: 综合搜索、筛选、排序
      security: []
      parameters:
        - { name: keyword, in: query, required: false, schema: { type: string } }
        - { name: brandId, in: query, required: false, schema: { type: integer, format: int64 } }
        - { name: productCategoryId, in: query, required: false, schema: { type: integer, format: int64 } }
        - { name: pageNum, in: query, schema: { type: integer, format: int32, default: 0 } }
        - { name: pageSize, in: query, schema: { type: integer, format: int32, default: 5 } }
        - name: sort
          in: query
          description: '排序字段：0->按相关度；1->按新品；2->按销量；3->价格从低到高；4->价格从高到低'
          schema: { type: integer, format: int32, default: 0, enum: [0, 1, 2, 3, 4] }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data:
                        allOf:
                          - $ref: '#/components/schemas/CommonPage'
                          - type: object
                            properties:
                              list: { type: array, items: { $ref: '#/components/schemas/PmsProduct' } }
  /product/categoryTreeList:
    get:
      tags: [PmsPortalProductController]
      summary: 以树形结构获取所有商品分类
      security: []
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: array, items: { $ref: '#/components/schemas/PmsProductCategoryNode' } }
  /product/detail/{id}:
    get:
      tags: [PmsPortalProductController]
      summary: 获取前台商品详情
      security: []
      parameters:
        - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: 成功
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { $ref: '#/components/schemas/PmsPortalProductDetail' }
  /alipay/pay:
    get:
      tags: [AlipayController]
      summary: 支付宝电脑网站支付
      security: []
      parameters:
        - { name: outTradeNo, in: query, required: true, description: '商户订单号，商家自定义，保持唯一性', schema: { type: string } }
        - { name: subject, in: query, required: true, description: '订单标题/关键字', schema: { type: string } }
        - { name: totalAmount, in: query, required: true, description: '订单总金额，单位元，精确到小数点后两位', schema: { type: number } }
      responses:
        '200':
          description: 返回支付宝支付表单 HTML 页面
          content:
            text/html:
              schema: { type: string }
  /alipay/webPay:
    get:
      tags: [AlipayController]
      summary: 支付宝手机网站支付
      security: []
      parameters:
        - { name: outTradeNo, in: query, required: true, schema: { type: string } }
        - { name: subject, in: query, required: true, schema: { type: string } }
        - { name: totalAmount, in: query, required: true, schema: { type: number } }
      responses:
        '200':
          description: 返回支付宝支付表单 HTML 页面
          content:
            text/html:
              schema: { type: string }
  /alipay/notify:
    post:
      tags: [AlipayController]
      summary: 支付宝异步回调
      description: 必须为 POST 请求；执行成功返回 success，执行失败返回 failure。
      security: []
      requestBody:
        required: true
        content:
          application/x-www-form-urlencoded:
            schema:
              type: object
              additionalProperties: { type: string }
      responses:
        '200':
          description: 处理结果标识
          content:
            text/plain:
              schema: { type: string, enum: [success, failure] }
  /alipay/query:
    get:
      tags: [AlipayController]
      summary: 支付宝统一收单线下交易查询
      description: 订单支付成功返回交易状态：TRADE_SUCCESS。
      security: []
      parameters:
        - { name: outTradeNo, in: query, required: false, description: '商户订单号', schema: { type: string } }
        - { name: tradeNo, in: query, required: false, description: '支付宝交易号', schema: { type: string } }
      responses:
        '200':
          description: 成功，data 为交易状态
          content:
            application/json:
              schema:
                allOf:
                  - $ref: '#/components/schemas/CommonResult'
                  - type: object
                    properties:
                      data: { type: string }
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: '请求头 Authorization: Bearer {token}，token 由 POST /sso/login 返回'
  schemas:
    CommonResult:
      type: object
      description: 通用返回结果封装（com.macro.mall.common.api.CommonResult）
      properties:
        code: { type: integer, format: int64, description: '业务状态码，200 表示成功' }
        message: { type: string, description: '提示信息' }
        data: { nullable: true, description: '数据封装，具体类型见各接口响应说明' }
    CommonPage:
      type: object
      description: 通用分页封装（com.macro.mall.common.api.CommonPage）
      properties:
        pageNum: { type: integer, format: int32, description: '当前页码' }
        pageSize: { type: integer, format: int32, description: '每页数量' }
        totalPage: { type: integer, format: int32, description: '总页数' }
        total: { type: integer, format: int64, description: '总条数' }
        list: { type: array, items: { type: object } }
    HomeContentResult:
      type: object
      description: 首页内容返回信息封装（com.macro.mall.portal.domain.HomeContentResult）
      properties:
        advertiseList: { type: array, description: '轮播广告', items: { $ref: '#/components/schemas/SmsHomeAdvertise' } }
        brandList: { type: array, description: '推荐品牌', items: { $ref: '#/components/schemas/PmsBrand' } }
        homeFlashPromotion: { $ref: '#/components/schemas/HomeFlashPromotion' }
        newProductList: { type: array, description: '新品推荐', items: { $ref: '#/components/schemas/PmsProduct' } }
        hotProductList: { type: array, description: '人气推荐', items: { $ref: '#/components/schemas/PmsProduct' } }
        subjectList: { type: array, description: '推荐专题', items: { $ref: '#/components/schemas/CmsSubject' } }
    HomeFlashPromotion:
      type: object
      description: 首页秒杀场次信息封装（com.macro.mall.portal.domain.HomeFlashPromotion）
      properties:
        startTime: { type: string, format: date-time, description: '本场开始时间' }
        endTime: { type: string, format: date-time, description: '本场结束时间' }
        nextStartTime: { type: string, format: date-time, description: '下场开始时间' }
        nextEndTime: { type: string, format: date-time, description: '下场结束时间' }
        productList: { type: array, description: '属于该秒杀活动的商品', items: { $ref: '#/components/schemas/FlashPromotionProduct' } }
    FlashPromotionProduct:
      description: 秒杀信息和商品对象封装（com.macro.mall.portal.domain.FlashPromotionProduct extends PmsProduct）
      allOf:
        - $ref: '#/components/schemas/PmsProduct'
        - type: object
          properties:
            flashPromotionPrice: { type: number, description: '秒杀价格' }
            flashPromotionCount: { type: integer, format: int32, description: '用于秒杀的数量' }
            flashPromotionLimit: { type: integer, format: int32, description: '秒杀限购数量' }
    CartProduct:
      description: 购物车中带规格和 SKU 的商品信息（com.macro.mall.portal.domain.CartProduct extends PmsProduct）
      allOf:
        - $ref: '#/components/schemas/PmsProduct'
        - type: object
          properties:
            productAttributeList: { type: array, description: '商品属性列表', items: { $ref: '#/components/schemas/PmsProductAttribute' } }
            skuStockList: { type: array, description: '商品SKU库存列表', items: { $ref: '#/components/schemas/PmsSkuStock' } }
    CartPromotionItem:
      description: 购物车中促销信息的封装（com.macro.mall.portal.domain.CartPromotionItem extends OmsCartItem）
      allOf:
        - $ref: '#/components/schemas/OmsCartItem'
        - type: object
          properties:
            promotionMessage: { type: string, description: '促销活动信息' }
            reduceAmount: { type: number, description: '促销活动减去的金额（针对每个商品）' }
            realStock: { type: integer, format: int32, description: '剩余库存-锁定库存' }
            integration: { type: integer, format: int32, description: '购买商品赠送积分' }
            growth: { type: integer, format: int32, description: '购买商品赠送成长值' }
    ConfirmOrderResult:
      type: object
      description: 确认单信息封装（com.macro.mall.portal.domain.ConfirmOrderResult）
      properties:
        cartPromotionItemList: { type: array, description: '包含优惠信息的购物车信息', items: { $ref: '#/components/schemas/CartPromotionItem' } }
        memberReceiveAddressList: { type: array, description: '用户收货地址列表', items: { $ref: '#/components/schemas/UmsMemberReceiveAddress' } }
        couponHistoryDetailList: { type: array, description: '用户可用优惠券列表', items: { $ref: '#/components/schemas/SmsCouponHistoryDetail' } }
        integrationConsumeSetting: { $ref: '#/components/schemas/UmsIntegrationConsumeSetting' }
        memberIntegration: { type: integer, format: int32, description: '会员持有的积分' }
        calcAmount: { $ref: '#/components/schemas/ConfirmOrderResultCalcAmount' }
    ConfirmOrderResultCalcAmount:
      type: object
      description: 确认单计算的金额（ConfirmOrderResult.CalcAmount）
      properties:
        totalAmount: { type: number, description: '订单商品总金额' }
        freightAmount: { type: number, description: '运费' }
        promotionAmount: { type: number, description: '活动优惠' }
        payAmount: { type: number, description: '应付金额' }
    OmsOrderDetail:
      description: 包含商品信息的订单详情（com.macro.mall.portal.domain.OmsOrderDetail extends OmsOrder）
      allOf:
        - $ref: '#/components/schemas/OmsOrder'
        - type: object
          properties:
            orderItemList: { type: array, description: '订单商品列表', items: { $ref: '#/components/schemas/OmsOrderItem' } }
    OrderParam:
      type: object
      description: 生成订单时传入的参数（com.macro.mall.portal.domain.OrderParam）
      properties:
        memberReceiveAddressId: { type: integer, format: int64, description: '收货地址ID' }
        couponId: { type: integer, format: int64, description: '优惠券ID' }
        useIntegration: { type: integer, format: int32, description: '使用的积分数' }
        payType: { type: integer, format: int32, description: '支付方式' }
        cartIds: { type: array, description: '被选中的购物车商品ID', items: { type: integer, format: int64 } }
    OmsOrderReturnApplyParam:
      type: object
      description: 退货申请请求参数（com.macro.mall.portal.domain.OmsOrderReturnApplyParam）
      properties:
        orderId: { type: integer, format: int64, description: '订单id' }
        productId: { type: integer, format: int64, description: '退货商品id' }
        orderSn: { type: string, description: '订单编号' }
        memberUsername: { type: string, description: '会员用户名' }
        returnName: { type: string, description: '退货人姓名' }
        returnPhone: { type: string, description: '退货人电话' }
        productPic: { type: string, description: '商品图片' }
        productName: { type: string, description: '商品名称' }
        productBrand: { type: string, description: '商品品牌' }
        productAttr: { type: string, description: '商品销售属性：颜色：红色；尺码：xl' }
        productCount: { type: integer, format: int32, description: '退货数量' }
        productPrice: { type: number, description: '商品单价' }
        productRealPrice: { type: number, description: '商品实际支付单价' }
        reason: { type: string, description: '原因' }
        description: { type: string, description: '描述' }
        proofPics: { type: string, description: '凭证图片，以逗号隔开' }
    AliPayParam:
      type: object
      description: 支付宝支付请求参数（com.macro.mall.portal.domain.AliPayParam）
      properties:
        outTradeNo: { type: string, description: '商户订单号，商家自定义，保持唯一性' }
        subject: { type: string, description: '商品的标题/交易标题/订单标题/订单关键字' }
        totalAmount: { type: number, description: '订单总金额，单位为元，精确到小数点后两位' }
    PmsPortalProductDetail:
      type: object
      description: 前台商品详情（com.macro.mall.portal.domain.PmsPortalProductDetail）
      properties:
        product: { $ref: '#/components/schemas/PmsProduct' }
        brand: { $ref: '#/components/schemas/PmsBrand' }
        productAttributeList: { type: array, items: { $ref: '#/components/schemas/PmsProductAttribute' } }
        productAttributeValueList: { type: array, items: { $ref: '#/components/schemas/PmsProductAttributeValue' } }
        skuStockList: { type: array, items: { $ref: '#/components/schemas/PmsSkuStock' } }
        productLadderList: { type: array, items: { $ref: '#/components/schemas/PmsProductLadder' } }
        productFullReductionList: { type: array, items: { $ref: '#/components/schemas/PmsProductFullReduction' } }
        couponList: { type: array, items: { $ref: '#/components/schemas/SmsCoupon' } }
    PmsProductCategoryNode:
      description: 包含子分类的商品分类（com.macro.mall.portal.domain.PmsProductCategoryNode extends PmsProductCategory）
      allOf:
        - $ref: '#/components/schemas/PmsProductCategory'
        - type: object
          properties:
            children: { type: array, description: '子分类集合', items: { $ref: '#/components/schemas/PmsProductCategoryNode' } }
    SmsCouponHistoryDetail:
      description: 优惠券领取历史详情（com.macro.mall.portal.domain.SmsCouponHistoryDetail extends SmsCouponHistory）
      allOf:
        - $ref: '#/components/schemas/SmsCouponHistory'
        - type: object
          properties:
            coupon: { $ref: '#/components/schemas/SmsCoupon' }
            productRelationList: { type: array, description: '优惠券关联商品', items: { $ref: '#/components/schemas/SmsCouponProductRelation' } }
            categoryRelationList: { type: array, description: '优惠券关联商品分类', items: { $ref: '#/components/schemas/SmsCouponProductCategoryRelation' } }
    MemberBrandAttention:
      type: object
      description: 会员品牌关注（MongoDB 文档，com.macro.mall.portal.domain.MemberBrandAttention）
      properties:
        id: { type: string }
        memberId: { type: integer, format: int64 }
        memberNickname: { type: string }
        memberIcon: { type: string }
        brandId: { type: integer, format: int64 }
        brandName: { type: string }
        brandLogo: { type: string }
        brandCity: { type: string }
        createTime: { type: string, format: date-time }
    MemberProductCollection:
      type: object
      description: 会员商品收藏（MongoDB 文档，com.macro.mall.portal.domain.MemberProductCollection）
      properties:
        id: { type: string }
        memberId: { type: integer, format: int64 }
        memberNickname: { type: string }
        memberIcon: { type: string }
        productId: { type: integer, format: int64 }
        productName: { type: string }
        productPic: { type: string }
        productSubTitle: { type: string }
        productPrice: { type: string }
        createTime: { type: string, format: date-time }
    MemberReadHistory:
      type: object
      description: 会员商品浏览历史记录（MongoDB 文档，com.macro.mall.portal.domain.MemberReadHistory）
      properties:
        id: { type: string }
        memberId: { type: integer, format: int64 }
        memberNickname: { type: string }
        memberIcon: { type: string }
        productId: { type: integer, format: int64 }
        productName: { type: string }
        productPic: { type: string }
        productSubTitle: { type: string }
        productPrice: { type: string }
        createTime: { type: string, format: date-time }
    OmsCartItem:
      type: object
      description: 购物车商品（com.macro.mall.model.OmsCartItem）
      properties:
        id: { type: integer, format: int64 }
        productId: { type: integer, format: int64 }
        productSkuId: { type: integer, format: int64 }
        memberId: { type: integer, format: int64 }
        quantity: { type: integer, format: int32, description: '购买数量' }
        price: { type: number, description: '添加到购物车的价格' }
        productPic: { type: string, description: '商品主图' }
        productName: { type: string, description: '商品名称' }
        productSubTitle: { type: string, description: '商品副标题（卖点）' }
        productSkuCode: { type: string, description: '商品sku条码' }
        memberNickname: { type: string, description: '会员昵称' }
        createDate: { type: string, format: date-time, description: '创建时间' }
        modifyDate: { type: string, format: date-time, description: '修改时间' }
        deleteStatus: { type: integer, format: int32, description: '是否删除' }
        productCategoryId: { type: integer, format: int64, description: '商品分类' }
        productBrand: { type: string }
        productSn: { type: string }
        productAttr: { type: string, description: "商品销售属性：[{'key':'颜色','value':'黑色'},{'key':'容量','value':'4G'}]" }
    UmsMemberReceiveAddress:
      type: object
      description: 会员收货地址（com.macro.mall.model.UmsMemberReceiveAddress）
      properties:
        id: { type: integer, format: int64 }
        memberId: { type: integer, format: int64 }
        name: { type: string, description: '收货人名称' }
        phoneNumber: { type: string }
        defaultStatus: { type: integer, format: int32, description: '是否为默认' }
        postCode: { type: string, description: '邮政编码' }
        province: { type: string, description: '省份/直辖市' }
        city: { type: string, description: '城市' }
        region: { type: string, description: '区' }
        detailAddress: { type: string, description: '详细地址(街道)' }
    PmsBrand:
      type: object
      description: 品牌（com.macro.mall.model.PmsBrand）
      properties:
        id: { type: integer, format: int64 }
        name: { type: string }
        firstLetter: { type: string, description: '首字母' }
        sort: { type: integer, format: int32 }
        factoryStatus: { type: integer, format: int32, description: '是否为品牌制造商：0->不是；1->是' }
        showStatus: { type: integer, format: int32 }
        productCount: { type: integer, format: int32, description: '产品数量' }
        productCommentCount: { type: integer, format: int32, description: '产品评论数量' }
        logo: { type: string, description: '品牌logo' }
        bigPic: { type: string, description: '专区大图' }
        brandStory: { type: string, description: '品牌故事' }
    UmsMember:
      type: object
      description: 会员（com.macro.mall.model.UmsMember）
      properties:
        id: { type: integer, format: int64 }
        memberLevelId: { type: integer, format: int64 }
        username: { type: string, description: '用户名' }
        password: { type: string, description: '密码' }
        nickname: { type: string, description: '昵称' }
        phone: { type: string, description: '手机号码' }
        status: { type: integer, format: int32, description: '帐号启用状态：0->禁用；1->启用' }
        createTime: { type: string, format: date-time, description: '注册时间' }
        icon: { type: string, description: '头像' }
        gender: { type: integer, format: int32, description: '性别：0->未知；1->男；2->女' }
        birthday: { type: string, format: date-time, description: '生日' }
        city: { type: string, description: '所在城市' }
        job: { type: string, description: '职业' }
        personalizedSignature: { type: string, description: '个性签名' }
        sourceType: { type: integer, format: int32, description: '用户来源' }
        integration: { type: integer, format: int32, description: '积分' }
        growth: { type: integer, format: int32, description: '成长值' }
        luckeyCount: { type: integer, format: int32, description: '剩余抽奖次数' }
        historyIntegration: { type: integer, format: int32, description: '历史积分数量' }
    SmsCoupon:
      type: object
      description: 优惠券（com.macro.mall.model.SmsCoupon）
      properties:
        id: { type: integer, format: int64 }
        type: { type: integer, format: int32, description: '优惠券类型：0->全场赠券；1->会员赠券；2->购物赠券；3->注册赠券' }
        name: { type: string }
        platform: { type: integer, format: int32, description: '使用平台：0->全部；1->移动；2->PC' }
        count: { type: integer, format: int32, description: '数量' }
        amount: { type: number, description: '金额' }
        perLimit: { type: integer, format: int32, description: '每人限领张数' }
        minPoint: { type: number, description: '使用门槛；0表示无门槛' }
        startTime: { type: string, format: date-time }
        endTime: { type: string, format: date-time }
        useType: { type: integer, format: int32, description: '使用类型：0->全场通用；1->指定分类；2->指定商品' }
        note: { type: string, description: '备注' }
        publishCount: { type: integer, format: int32, description: '发行数量' }
        useCount: { type: integer, format: int32, description: '已使用数量' }
        receiveCount: { type: integer, format: int32, description: '领取数量' }
        enableTime: { type: string, format: date-time, description: '可以领取的日期' }
        code: { type: string, description: '优惠码' }
        memberLevel: { type: integer, format: int32, description: '可领取的会员类型：0->无限时' }
    SmsCouponHistory:
      type: object
      description: 优惠券领取历史（com.macro.mall.model.SmsCouponHistory）
      properties:
        id: { type: integer, format: int64 }
        couponId: { type: integer, format: int64 }
        memberId: { type: integer, format: int64 }
        couponCode: { type: string }
        memberNickname: { type: string, description: '领取人昵称' }
        getType: { type: integer, format: int32, description: '获取类型：0->后台赠送；1->主动获取' }
        createTime: { type: string, format: date-time }
        useStatus: { type: integer, format: int32, description: '使用状态：0->未使用；1->已使用；2->已过期' }
        useTime: { type: string, format: date-time, description: '使用时间' }
        orderId: { type: integer, format: int64, description: '订单编号' }
        orderSn: { type: string, description: '订单号码' }
    PmsProduct:
      type: object
      additionalProperties: true
      description: 商品持久化模型 com.macro.mall.model.PmsProduct（MBG 生成，字段以该类为准）
    PmsProductCategory:
      type: object
      additionalProperties: true
      description: 商品分类持久化模型 com.macro.mall.model.PmsProductCategory（MBG 生成，字段以该类为准）
    CmsSubject:
      type: object
      additionalProperties: true
      description: 专题持久化模型 com.macro.mall.model.CmsSubject（MBG 生成，字段以该类为准）
    SmsHomeAdvertise:
      type: object
      additionalProperties: true
      description: 首页广告持久化模型 com.macro.mall.model.SmsHomeAdvertise（MBG 生成，字段以该类为准）
    PmsProductAttribute:
      type: object
      additionalProperties: true
      description: 商品属性持久化模型 com.macro.mall.model.PmsProductAttribute（MBG 生成，字段以该类为准）
    PmsProductAttributeValue:
      type: object
      additionalProperties: true
      description: 商品属性值持久化模型 com.macro.mall.model.PmsProductAttributeValue（MBG 生成，字段以该类为准）
    PmsSkuStock:
      type: object
      additionalProperties: true
      description: 商品SKU库存持久化模型 com.macro.mall.model.PmsSkuStock（MBG 生成，字段以该类为准）
    PmsProductLadder:
      type: object
      additionalProperties: true
      description: 商品阶梯价格持久化模型 com.macro.mall.model.PmsProductLadder（MBG 生成，字段以该类为准）
    PmsProductFullReduction:
      type: object
      additionalProperties: true
      description: 商品满减持久化模型 com.macro.mall.model.PmsProductFullReduction（MBG 生成，字段以该类为准）
    SmsCouponProductRelation:
      type: object
      additionalProperties: true
      description: 优惠券与商品关联持久化模型 com.macro.mall.model.SmsCouponProductRelation（MBG 生成，字段以该类为准）
    SmsCouponProductCategoryRelation:
      type: object
      additionalProperties: true
      description: 优惠券与商品分类关联持久化模型 com.macro.mall.model.SmsCouponProductCategoryRelation（MBG 生成，字段以该类为准）
    UmsIntegrationConsumeSetting:
      type: object
      additionalProperties: true
      description: 积分消费设置持久化模型 com.macro.mall.model.UmsIntegrationConsumeSetting（MBG 生成，字段以该类为准）
    OmsOrder:
      type: object
      additionalProperties: true
      description: 订单持久化模型 com.macro.mall.model.OmsOrder（MBG 生成，字段以该类为准）
    OmsOrderItem:
      type: object
      additionalProperties: true
      description: 订单商品持久化模型 com.macro.mall.model.OmsOrderItem（MBG 生成，字段以该类为准）
""";

    private static final String BASE_URL =
            System.getProperty("api.base.url", "http://localhost:8085");

    private Response response;
    private String token;

    @Given("the mall-portal service is running at {string}")
    public void theMallPortalServiceIsRunningAt(String baseUrl) {
        RestAssured.baseURI = baseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Given("I am logged in as the configured member")
    public void iAmLoggedInAsTheConfiguredMember() {
        String username = System.getProperty("mall.member.username");
        String password = System.getProperty("mall.member.password");
        Assertions.assertNotNull(username, "需通过 -Dmall.member.username 指定登录账号");
        Assertions.assertNotNull(password, "需通过 -Dmall.member.password 指定登录密码");
        Response login = RestAssured.given()
                .param("username", username)
                .param("password", password)
                .post("/sso/login");
        Assertions.assertEquals(200, login.jsonPath().getInt("code"), "登录失败：" + login.asString());
        this.token = login.jsonPath().getString("data.token");
        Assertions.assertNotNull(this.token, "登录响应未返回 data.token");
    }

    @When("I send GET {string} without login")
    public void iSendGetWithoutLogin(String path) {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .get(path);
    }

    @When("I send GET {string} with the current token")
    public void iSendGetWithTheCurrentToken(String path) {
        Assertions.assertNotNull(token, "当前没有可用 token，请先执行登录步骤");
        response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .get(path);
        Assertions.assertEquals(200, response.getStatusCode(), "HTTP 状态码非 200：" + response.asString());
    }

    @When("I send POST {string} with form params")
    public void iSendPostWithFormParams(String path, DataTable table) {
        Map<String, String> params = table.asMap(String.class, String.class);
        response = RestAssured.given()
                .contentType(ContentType.URLENC)
                .formParams(params)
                .post(path);
    }

    @Then("the response business code is {int}")
    public void theResponseBusinessCodeIs(int expectedCode) {
        Assertions.assertNotNull(response, "尚未发起请求");
        Integer actual = response.jsonPath().get("code");
        Assertions.assertEquals(expectedCode, actual,
                "业务码不符，响应体：" + response.asString());
    }

    @Then("the response data is not null")
    public void theResponseDataIsNotNull() {
        Assertions.assertNotNull(response.jsonPath().get("data"), "响应 data 为空");
    }

    @Then("the embedded OpenAPI contract declares the portal endpoints")
    public void theEmbeddedOpenApiContractDeclaresThePortalEndpoints() {
        Assertions.assertTrue(OPENAPI_YAML.contains("openapi: 3.0.3"), "契约缺少 openapi 版本声明");
        Assertions.assertTrue(OPENAPI_YAML.contains("/sso/login"), "契约缺少 /sso/login");
        Assertions.assertTrue(OPENAPI_YAML.contains("/home/content"), "契约缺少 /home/content");
        Assertions.assertTrue(OPENAPI_YAML.contains("/cart/list"), "契约缺少 /cart/list");
        Assertions.assertTrue(OPENAPI_YAML.contains("/order/generateOrder"), "契约缺少 /order/generateOrder");
        Assertions.assertTrue(OPENAPI_YAML.contains("#/components/schemas/CommonResult"),
                "契约缺少 CommonResult 响应封装定义");
    }
}
