package com.macro.mall.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台管理端（mall-admin，默认 http://localhost:8080）认证与鉴权步骤定义。
 *
 * 依据 back（master）源码重新生成，行为断言对应的实现位置：
 * - 登录：UmsAdminController#login + UmsAdminServiceImpl#login（密码错误抛 ApiException → 业务码 500「密码不正确」；
 *   用户名不存在被捕获后返回 404「用户名或密码错误」）
 * - 参数校验：UmsAdminLoginParam + GlobalExceptionHandler（业务码 404，message = 字段名 + 校验信息）
 * - 鉴权：SecurityConfig + DynamicAuthorizationManager（401 未登录 / 403 无资源权限）
 * - 令牌：JwtAuthenticationTokenFilter + JwtTokenUtil（refreshToken 需先通过认证）
 *
 * 说明：Cfg / Ctx / Api / ApiAssert 是本套 BDD 用例共用的最小支撑类，位于同包，
 * 供会员、商品、购物车、下单等其它 step 文件复用；后续可整体抽取到 support 包。
 */
public class AdminAuthSteps {

    // ==================== 登录 ====================

    @Given("管理端服务地址为 {string}")
    public void setAdminBaseUrl(String baseUrl) {
        Api.adminBase = baseUrl;
    }

    @When("使用环境配置的管理端账号和密码登录")
    public void loginWithConfiguredAccount() {
        Ctx.adminToken = null;
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Api.postJson(Api.adminBase, "/admin/login", Cfg.adminLoginBody(Cfg.ADMIN_USERNAME, Cfg.ADMIN_PASSWORD));
    }

    @When("使用环境配置的管理端账号和错误密码登录")
    public void loginWithWrongPassword() {
        Ctx.adminToken = null;
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Api.postJson(Api.adminBase, "/admin/login", Cfg.adminLoginBody(Cfg.ADMIN_USERNAME, Cfg.ADMIN_WRONG_PASSWORD));
    }

    @When("使用不存在的管理端账号登录")
    public void loginWithUnknownAccount() {
        Ctx.adminToken = null;
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        String username = "bdd_absent_" + System.currentTimeMillis();
        Api.postJson(Api.adminBase, "/admin/login", Cfg.adminLoginBody(username, Cfg.ADMIN_PASSWORD));
    }

    @When("提交缺少密码的管理端登录请求")
    public void loginWithoutPassword() {
        Ctx.adminToken = null;
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Map<String, Object> body = new HashMap<>();
        body.put("username", Cfg.ADMIN_USERNAME);
        Api.postJson(Api.adminBase, "/admin/login", body);
    }

    @Given("已使用环境配置的管理端账号登录并取得 token")
    public void loginAndKeepToken() {
        Ctx.adminToken = null;
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Api.postJson(Api.adminBase, "/admin/login", Cfg.adminLoginBody(Cfg.ADMIN_USERNAME, Cfg.ADMIN_PASSWORD));
        ApiAssert.codeIs(200);
        Ctx.adminToken = Api.last().jsonPath().getString("data.token");
        assertThat(Ctx.adminToken).as("管理端 token").isNotBlank();
        Ctx.adminUsername = Cfg.ADMIN_USERNAME;
    }

    @When("注册一个无角色的管理端账号并登录取得 token")
    public void registerRandomAdminAndLogin() {
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        String username = "bdd_admin_" + System.currentTimeMillis();
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", Cfg.ADMIN_PASSWORD);
        Api.postJson(Api.adminBase, "/admin/register", body);
        assertThat(ApiAssert.code()).as("注册无角色管理员, body=" + Api.last().asString()).isEqualTo(200);

        Ctx.adminToken = null;
        Api.postJson(Api.adminBase, "/admin/login", Cfg.adminLoginBody(username, Cfg.ADMIN_PASSWORD));
        Ctx.adminToken = Api.last().jsonPath().getString("data.token");
        Ctx.adminUsername = username;
        assertThat(Ctx.adminToken).as("无角色管理员 token").isNotBlank();
    }

    // ==================== 受保护接口访问 ====================

    @When("携带管理端 token 以 GET 请求 {string}")
    public void adminGetWithToken(String path) {
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Api.get(Api.adminBase, path);
    }

    @When("不携带 token 以 GET 请求管理端 {string}")
    public void adminGetAnonymous(String path) {
        Api.getAnonymous(Api.adminBase, path);
    }

    @When("携带无效 token 以 GET 请求管理端 {string}")
    public void adminGetWithInvalidToken(String path) {
        Api.getWithToken(Api.adminBase, "invalid.token.value", path);
    }

    @When("携带管理端 token 以 POST 请求 {string}")
    public void adminPostWithToken(String path) {
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Api.postWithoutParam(Api.adminBase, path);
    }

    // ==================== 断言 ====================

    @Then("后台响应 HTTP 状态码应为 {int}")
    public void adminHttpStatus(int expected) {
        ApiAssert.http(expected);
    }

    @Then("后台响应业务码应为 {int}")
    public void adminBusinessCode(int expected) {
        ApiAssert.codeIs(expected);
    }

    @Then("后台响应 message 应为 {string}")
    public void adminMessage(String expected) {
        ApiAssert.message(expected);
    }

    @Then("后台响应 message 应包含 {string}")
    public void adminMessageContains(String fragment) {
        ApiAssert.messageContains(fragment);
    }

    @Then("后台响应 data 应非空")
    public void adminDataNotBlank() {
        ApiAssert.dataNotBlank("");
    }

    @Then("后台响应 data 应为空")
    public void adminDataEmpty() {
        ApiAssert.dataEmpty("");
    }

    @Then("后台响应 data 字段 {string} 应非空")
    public void adminDataFieldNotBlank(String path) {
        ApiAssert.dataNotBlank(path);
    }

    @Then("后台响应 data 字段 {string} 应等于 {string}")
    public void adminDataFieldEquals(String path, String expected) {
        ApiAssert.dataEquals(path, expected);
    }

    @Then("后台响应 data 字段 {string} 应等于环境配置的管理端用户名")
    public void adminDataFieldEqualsConfiguredUsername(String path) {
        String expected = Ctx.adminUsername == null ? Cfg.ADMIN_USERNAME : Ctx.adminUsername;
        ApiAssert.dataEquals(path, expected);
    }

    @Then("后台响应 data 中应包含非空数组 {string}")
    public void adminDataArrayNotBlank(String path) {
        ApiAssert.dataNotBlank(path);
    }

    @Then("后台响应 data 中应包含数组 {string}")
    public void adminDataIsArray(String path) {
        ApiAssert.dataIsArray(path);
    }

    @Then("后台响应 data 字段 {string} 应等于刚下单的订单号")
    public void adminDataFieldEqualsLastOrderId(String path) {
        assertThat(Ctx.orderId).as("刚下单的订单号").isNotNull();
        ApiAssert.dataEquals(path, String.valueOf(Ctx.orderId));
    }
}

/** 配置读取：优先级 system property > 环境变量 > 默认值 */
class Cfg {
    static String p(String key, String def) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        return (value == null || value.isBlank()) ? def : value;
    }

    static final String ADMIN_BASE = p("admin.baseUrl", "http://localhost:8080");
    static final String PORTAL_BASE = p("portal.baseUrl", "http://localhost:8085");
    static final String ADMIN_USERNAME = p("admin.username", "admin");
    static final String ADMIN_PASSWORD = p("admin.password", "macro123");
    static final String ADMIN_WRONG_PASSWORD = p("admin.wrongPassword", "wrong-password");
    static final String MEMBER_PASSWORD = p("member.password", "123456");
    /** 测试环境中已上架且有库存的商品与 SKU，供购物车 / 下单用例使用 */
    static final Long PRODUCT_ID = Long.valueOf(p("cart.productId", "1"));
    static final Long SKU_ID = Long.valueOf(p("cart.productSkuId", "1"));
    /** 兼容旧命名 */
    static final String ADMIN_BASE_URL = ADMIN_BASE;
    static final String PORTAL_BASE_URL = PORTAL_BASE;
    static final Long CART_PRODUCT_ID = PRODUCT_ID;
    static final Long CART_SKU_ID = SKU_ID;

    static Map<String, Object> adminLoginBody(String username, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        return body;
    }
}

/** 场景间共享上下文，会员相关场景由 Ctx.reset() 隔离数据 */
class Ctx {

    enum TokenMode { ADMIN, MEMBER }

    static TokenMode tokenMode = TokenMode.ADMIN;
    static Response lastResponse;

    static String adminToken;
    static String adminUsername;
    static String memberToken;
    static String memberUsername;
    static String memberTelephone;
    static Long productId;
    static Long addressId;
    static Long cartItemId;
    static Long orderId;

    static void reset() {
        memberToken = null;
        memberUsername = null;
        memberTelephone = null;
        productId = null;
        addressId = null;
        cartItemId = null;
        orderId = null;
    }
}

/** 极简 HTTP 客户端：统一 baseUrl / token / JSON 头；token 默认按 Ctx.tokenMode 自动选择 */
class Api {
    static String adminBase = Cfg.ADMIN_BASE;
    static String portalBase = Cfg.PORTAL_BASE;

    static Response last() {
        return Ctx.lastResponse;
    }

    private static RequestSpecification req(String baseUrl, String token) {
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        return (token == null || token.isBlank()) ? spec : spec.header("Authorization", "Bearer " + token);
    }

    private static String autoToken() {
        return Ctx.tokenMode == Ctx.TokenMode.MEMBER ? Ctx.memberToken : Ctx.adminToken;
    }

    static Response get(String baseUrl, String path) {
        return getWithToken(baseUrl, autoToken(), path);
    }

    static Response get(String baseUrl, String path, Map<String, ?> params) {
        Ctx.lastResponse = req(baseUrl, autoToken()).queryParams(params).get(path);
        return Ctx.lastResponse;
    }

    static Response getWithToken(String baseUrl, String token, String path) {
        Ctx.lastResponse = req(baseUrl, token).get(path);
        return Ctx.lastResponse;
    }

    static Response getAnonymous(String baseUrl, String path) {
        Ctx.lastResponse = req(baseUrl, null).get(path);
        return Ctx.lastResponse;
    }

    static Response postJson(String baseUrl, String path, Object body) {
        Ctx.lastResponse = req(baseUrl, autoToken()).body(body).post(path);
        return Ctx.lastResponse;
    }

    static Response postForm(String baseUrl, String path, Map<String, ?> form) {
        Ctx.lastResponse = req(baseUrl, autoToken()).queryParams(form).post(path);
        return Ctx.lastResponse;
    }

    static Response postWithoutParam(String baseUrl, String path) {
        Ctx.lastResponse = req(baseUrl, autoToken()).post(path);
        return Ctx.lastResponse;
    }
}

/** 统一断言：HTTP 码 / 业务码 / message / data 字段 */
class ApiAssert {
    private static String dp(String path) {
        return (path == null || path.isBlank()) ? "data" : "data." + path;
    }

    static long code() {
        return Ctx.lastResponse.jsonPath().getLong("code");
    }

    static void codeIs(long expected) {
        assertThat(code()).as("业务码, body=" + Ctx.lastResponse.asString()).isEqualTo(expected);
    }

    static void http(int expected) {
        assertThat(Ctx.lastResponse.statusCode()).as("HTTP 状态码").isEqualTo(expected);
    }

    static void message(String expected) {
        assertThat(Ctx.lastResponse.jsonPath().getString("message")).as("message").isEqualTo(expected);
    }

    static void messageContains(String fragment) {
        String message = Ctx.lastResponse.jsonPath().getString("message");
        assertThat(message).as("message").isNotNull().contains(fragment);
    }

    static Object data(String path) {
        return Ctx.lastResponse.jsonPath().get(dp(path));
    }

    static void dataNotBlank(String path) {
        Object value = data(path);
        assertThat(value).as(dp(path)).isNotNull();
        assertThat(String.valueOf(value)).as(dp(path)).isNotBlank().isNotEqualTo("[]");
    }

    static void dataEmpty(String path) {
        assertThat(data(path)).as(dp(path)).isNull();
    }

    static void dataEquals(String path, String expected) {
        assertThat(Ctx.lastResponse.jsonPath().getString(dp(path))).as(dp(path)).isEqualTo(expected);
    }

    static void dataIsArray(String path) {
        assertThat(data(path)).as(dp(path)).isInstanceOf(List.class);
    }
}
