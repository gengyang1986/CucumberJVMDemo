Feature: 前台商城 H5 登录后购物车结算下单
  前台商城 H5（mall-app-web，uni-app Vue3）真实用户主流程：登录 → 点击底部「购物车」标签页 → 去结算 → 提交订单。

  页面依据（MallApp 仓 mall-app-web 源码）：
  - pages/public/login.vue：用户名 input[placeholder='请输入用户名']、密码 input[placeholder^='8-18位']、按钮 .confirm-btn「登录」；
    登录成功后 1s 内 uni.switchTab 回首页（#/pages/index/index）
  - pages.json tabBar 第 3 项「购物车」→ pages/cart/cart；H5 渲染为 .uni-tabbar__item
  - pages/cart/cart.vue：底部 .action-section .confirm-btn「去结算」；购物车为空时渲染 .empty（无法结算）
  - pages/order/createOrder.vue：底部 .footer .submit「提交订单」→ POST /order/generateOrder，
    成功后 uni.showModal 弹出「订单创建成功，是否要立即支付？」

  执行环境：app-portal。
  - WEB_BASE_URL：mall-app-web 的 H5 访问地址（必需，环境里只有接口 baseUrl）
  - PORTAL_BASE_URL（或 BASE_URL）：mall-portal 接口地址，仅用于通过接口准备购物车与收货地址
  - MEMBER_USERNAME / MEMBER_PASSWORD：可登录的会员账号（必需）
  - UI_HEADLESS：默认 true；本地想看到浏览器可设 false

  Background:
    Given H5 商城站点地址已配置
    And 会员账号的购物车与收货地址数据已准备

  @P0 @ui @smoke @checkout
  Scenario: 登录后从购物车结算并提交订单
    When 打开商城登录页并输入会员账号密码登录
    Then 应登录成功并跳转到首页
    When 点击底部「购物车」标签页
    Then 购物车页应显示可结算商品
    When 在购物车页点击「去结算」
    Then 创建订单页应显示「提交订单」按钮且展示收货地址与商品
    When 在创建订单页点击「提交订单」
    Then 应弹出「订单创建成功」提示