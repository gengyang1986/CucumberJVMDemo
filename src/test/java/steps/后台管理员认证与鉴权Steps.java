package com.macro.mall.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台管理端（mall-admin，默认 http://localhost:8080）认证与鉴权步骤定义。
 *
 * 说明：Cfg / Ctx / Api / ApiAssert 是本套 BDD 用例共用的最小支撑类。
 * 为保持本次提交“一个 feature + 一个配套 step 文件”的形式，暂随本文件一起提交（同包可见），
 * 后续会统一抽取到 com.macro.mall.bdd.support 包。
 */
public class AdminAuthSteps {

    @Given("管理端服务地址为 {string}")
    public void setAdminBaseUrl(String baseUrl) {
        Api.adminBase = baseUrl;
    }

    @Given("已使用管理端账号 {string} 登录并取得 token")
    public void loginAndKeepToken(String username) {
        Ctx.adminToken = null;
        Api.postJson(Api.adminBase, null, "/admin/login", Cfg.adminLoginBody(username, Cfg.ADMIN_PASSWORD));
        ApiAssert.code(200);
        Ctx.adminToken = Api.last().jsonPath().getString("data.token");
        assertThat(Ctx.adminToken).as("管理端 token").isNotBlank();
    }

    @When("使用管理端账号 {string} 和密码 {string} 登录")
    public void login(String username, String password) {
        Api.postJson(Api.adminBase, null, "/admin/login", Cfg.adminLoginBody(username, password));
    }

    @When("使用管理端账号 {string} 和错误密码 {string} 尝试登录")
    public void loginWithWrongPassword(String username, String password) {
        Api.postJson(Api.adminBase, null, "/admin/login", Cfg.adminLoginBody(username, password));
    }

    @When("携带管理端 token 以 GET 请求 {string}")
    public void adminGetWithToken(String path) {
        Api.get(Api.adminBase, Ctx.adminToken, path);
    }

    @When("不携带 token 以 GET 请求管理端 {string}")
    public void adminGetAnonymous(String path) {
        Api.get(Api.adminBase, null, path);
    }

    @When("携带无效 token 以 GET 请求管理端 {string}")
    public void adminGetWithInvalidToken(String path) {
        Api.get(Api.adminBase, "invalid.token.value", path);
    }

    @When("携带管理端 token 以 POST 请求 {string}")
    public void adminPostWithToken(String path) {
        Api.postForm(Api.adminBase, Ctx.adminToken, path, new HashMap<>());
    }

    @Then("后台响应 HTTP 状态码应为 {int}")
    public void adminHttpStatus(int expected) {
        ApiAssert.http(expected);
    }

    @Then("后台响应业务码应为 {int}")
    public void adminBusinessCode(int expected) {
        ApiAssert.code(expected);
    }

    @Then("后台响应 message 应为 {string}")
    public void adminMessage(String expected) {
        ApiAssert.message(expected);
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

    @Then("后台响应 data 字段 {string} 应等于刚下单的订单号")
    public void adminDataFieldEqualsLastOrderId(String path) {
        assertThat(Ctx.orderId).as("刚下单的订单号").isNotNull();
        ApiAssert.dataEquals(path, String.valueOf(Ctx.orderId));
    }

    @Then("后台响应 data 中应包含非空数组 {string}")
    public void adminDataArrayNotBlank(String path) {
        ApiAssert.dataNotBlank(path);
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

    static final String ADMIN_BASE_URL = p("admin.baseUrl", "http://localhost:8080");
    static final String PORTAL_BASE_URL = p("portal.baseUrl", "http://localhost:8085");
    static final String ADMIN_USERNAME = p("admin.username", "admin");
    static final String ADMIN_PASSWORD = p("admin.password", "macro123");
    static final String MEMBER_PASSWORD = p("member.password", "123456");
    /** 测试环境中已上架且有库存的商品与 SKU，供购物车/下单用例使用 */
    static final Long CART_PRODUCT_ID = Long.valueOf(p("cart.productId", "1"));
    static final Long CART_SKU_ID = Long.valueOf(p("cart.productSkuId", "1"));

    static Map<String, Object> adminLoginBody(String username, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        return body;
    }
}

/** 场景间共享上下文，每个场景由会员注册步骤调用 reset() 隔离数据 */
class Ctx {
    static String adminToken;
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

/** 极简 HTTP 客户端：统一 baseUrl / token / JSON 头 */
class Api {
    static String adminBase = Cfg.ADMIN_BASE_URL;
    static String portalBase = Cfg.PORTAL_BASE_URL;

    private static Response last;

    static Response last() {
        return last;
    }

    private static RequestSpecification req(String baseUrl, String token) {
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        return token == null ? spec : spec.header("Authorization", "Bearer " + token);
    }

    static Response get(String baseUrl, String token, String path) {
        last = req(baseUrl, token).get(path);
        return last;
    }

    static Response get(String baseUrl, String token, String path, Map<String, ?> params) {
        last = req(baseUrl, token).queryParams(params).get(path);
        return last;
    }

    static Response postJson(String baseUrl, String token, String path, Object body) {
        last = req(baseUrl, token).body(body).post(path);
        return last;
    }

    static Response postForm(String baseUrl, String token, String path, Map<String, ?> form) {
        last = req(baseUrl, token).queryParams(form).post(path);
        return last;
    }
}

/** 统一断言：HTTP 码 / 业务码 / message / data 字段 */
class ApiAssert {
    private static String dp(String path) {
        return (path == null || path.isBlank()) ? "data" : "data." + path;
    }

    static long code() {
        return Api.last().jsonPath().getLong("code");
    }

    static void http(int expected) {
        assertThat(Api.last().statusCode()).as("HTTP 状态码").isEqualTo(expected);
    }

    static void code(long expected) {
        assertThat(code()).as("业务码, body=" + Api.last().asString()).isEqualTo(expected);
    }

    static void message(String expected) {
        assertThat(Api.last().jsonPath().getString("message")).as("message").isEqualTo(expected);
    }

    static Object data(String path) {
        return Api.last().jsonPath().get(dp(path));
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
        assertThat(Api.last().jsonPath().getString(dp(path))).as(dp(path)).isEqualTo(expected);
    }
}
