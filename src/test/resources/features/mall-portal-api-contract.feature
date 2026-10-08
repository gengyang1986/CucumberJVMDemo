# language: zh-CN
功能: mall-portal 前台接口契约测试
  依据 back 仓 master 分支 mall-portal 模块 Controller 源码（服务端口 8085）生成的 OpenAPI 3.0.3 契约，
  校验公开接口、鉴权接口与核心购物车/订单链路的请求与响应约定。
  统一响应体为 CommonResult{code,message,data}，成功 code=200，登录失败/参数校验失败 code=404。

  背景:
    假如 前台服务地址为 "http://localhost:8085"

  场景大纲: 公开接口契约校验
    当 以 "<method>" 方法请求 "<path>"
    那么 响应状态码为 <httpStatus>
    而且 响应体字段 "code" 应为 <code>

    例子:
      | method | path                                                       | httpStatus | code |
      | GET    | /home/content                                              | 200        | 200  |
      | GET    | /home/recommendProductList?pageSize=4&pageNum=1            | 200        | 200  |
      | GET    | /home/productCateList/0                                    | 200        | 200  |
      | GET    | /home/subjectList?pageSize=4&pageNum=1                     | 200        | 200  |
      | GET    | /home/hotProductList?pageNum=1&pageSize=6                  | 200        | 200  |
      | GET    | /home/newProductList?pageNum=1&pageSize=6                  | 200        | 200  |
      | GET    | /product/categoryTreeList                                  | 200        | 200  |
      | GET    | /product/search?keyword=phone&pageNum=0&pageSize=5&sort=0  | 200        | 200  |
      | GET    | /product/detail/1                                          | 200        | 200  |
      | GET    | /brand/recommendList?pageSize=6&pageNum=1                  | 200        | 200  |
      | GET    | /brand/detail/1                                            | 200        | 200  |
      | GET    | /brand/productList?brandId=1&pageNum=1&pageSize=6          | 200        | 200  |
      | GET    | /alipay/query?outTradeNo=mall0001&tradeNo=mall0001         | 200        | 200  |

  场景: 会员使用正确密码登录
    当 以 "POST" 方法请求 "/sso/login?username=member&password=123456"
    那么 响应状态码为 200
    而且 响应体字段 "code" 应为 200
    而且 响应体字段 "data.token" 不应为空

  场景: 会员使用错误密码登录
    当 以 "POST" 方法请求 "/sso/login?username=member&password=wrong-password"
    那么 响应状态码为 200
    而且 响应体字段 "code" 应为 404

  场景大纲: 需登录接口契约校验
    假如 已使用会员账号登录并获取 token
    当 携带 token 以 "<method>" 方法请求 "<path>"
    那么 响应状态码为 <httpStatus>
    而且 响应体字段 "code" 不应为 401

    例子:
      | method | path                                                 | httpStatus |
      | GET    | /sso/info                                            | 200        |
      | GET    | /member/address/list                                 | 200        |
      | GET    | /member/coupon/list?useStatus=0                      | 200        |
      | GET    | /member/coupon/listHistory?useStatus=0               | 200        |
      | GET    | /member/attention/list?pageNum=1&pageSize=5           | 200        |
      | GET    | /member/productCollection/list?pageNum=1&pageSize=5   | 200        |
      | GET    | /member/readHistory/list?pageNum=1&pageSize=5         | 200        |
      | GET    | /cart/list                                           | 200        |
      | GET    | /cart/list/promotion                                 | 200        |
      | GET    | /cart/getProduct/1                                   | 200        |
      | GET    | /order/list?status=-1&pageNum=1&pageSize=5           | 200        |
      | GET    | /order/detail/1                                      | 200        |

  场景: 未登录访问受保护接口
    当 以 "GET" 方法请求 "/cart/list"
    那么 响应体字段 "code" 不应为 200

  场景: 添加商品到购物车参数缺失
    假如 已使用会员账号登录并获取 token
    而且 请求体为 "{}"
    当 携带 token 以 "POST" 方法请求 "/cart/add"
    那么 响应体字段 "code" 不应为 200

  场景: 修改购物车商品数量
    假如 已使用会员账号登录并获取 token
    当 携带 token 以 "GET" 方法请求 "/cart/update/quantity?id=1&quantity=2"
    那么 响应状态码为 200