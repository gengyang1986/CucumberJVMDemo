package com.macro.mall.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台管理员认证与鉴权 step。
 *
 * 本文件同时承载全模块通用的请求 / 断言 step（以 GET|POST 请求后台|前台 ..., 响应业务码应为 ...），
 * 其余 step 文件复用这些通用 step，因此不要在其它文件里重复定义同名 step，否则 Cucumber 会报 AmbiguousStepDefinitions。
 *
 * Cfg / Ctx / Api / ApiAssert 目前以同文件包级类提供支撑，建议后续统一抽到 com.macro.mall.bdd.support 包。
 * 依赖：JDK17 + cucumber-jvm 7.x + rest-assured 5.x + assertj
 */
public class AdminAuthSteps {

    @Before
    public void resetScenarioContext() {
        Ctx.tokenMode = Ctx.TokenMode.NONE;
        Ctx.lastResponse = null;
        Ctx.orderId = null;
        Ctx.addressId = null;
        Ctx.originalPublishStatus = null;
        Cart.itemId = null;
    }

    // ==================== 通用：token 与请求头 ====================

    @Given("后台管理端服务可用")
    public void adminServiceAvailable() {
        Ctx.tokenMode = Ctx.TokenMode.NONE;
    }

    @Given("前台商城端服务可用")
    public void portalServiceAvailable() {
        Ctx.tokenMode = Ctx.TokenMode.NONE;
    }

    @Given("请求头不携带 token")
    public void requestWithoutToken() {
        Ctx.tokenMode = Ctx.TokenMode.NONE;
    }

    @Given("请求头携带有效的后台 token")
    public void requestWithAdminToken() {
        assertThat(Ctx.adminToken).as("后台 token").isNotBlank();
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
    }

    @Given("请求头携带有效的会员 token")
    public void requestWithMemberToken() {
        assertThat(Ctx.memberToken).as("会员 token").isNotBlank();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
    }

    // ==================== 通用：发送请求 ====================

    @When("以 GET 请求后台 {string}")
    public void getAdmin(String path) {
        Ctx.lastResponse = Api.get(Cfg.ADMIN_BASE, path);
    }

    @When("以 GET 请求后台 {string} 参数为：")
    public void getAdminWithParams(String path, DataTable table) {
        Ctx.lastResponse = Api.get(Cfg.ADMIN_BASE, path, toParamMap(table));
    }

    @When("以 GET 请求前台 {string}")
    public void getPortal(String path) {
        Ctx.lastResponse = Api.get(Cfg.PORTAL_BASE, path);
    }

    @When("以 GET 请求前台 {string} 参数为：")
    public void getPortalWithParams(String path, DataTable table) {
        Ctx.lastResponse = Api.get(Cfg.PORTAL_BASE, path, toParamMap(table));
    }

    @When("以 POST 无参请求后台 {string}")
    public void postAdminWithoutParam(String path) {
        Ctx.lastResponse = Api.postWithoutParam(Cfg.ADMIN_BASE, path);
    }

    @When("以 POST 无参请求前台 {string}")
    public void postPortalWithoutParam(String path) {
        Ctx.lastResponse = Api.postWithoutParam(Cfg.PORTAL_BASE, path);
    }

    @When("以 POST 表单请求后台 {string} 参数为：")
    public void postFormAdmin(String path, DataTable table) {
        Ctx.lastResponse = Api.postForm(Cfg.ADMIN_BASE, path, toParamMap(table));
    }

    @When("以 POST 表单请求前台 {string} 参数为：")
    public void postFormPortal(String path, DataTable table) {
        Ctx.lastResponse = Api.postForm(Cfg.PORTAL_BASE, path, toParamMap(table));
    }

    @When("以 POST JSON 请求后台 {string} 内容为：")
    public void postJsonAdmin(String path, String body) {
        Ctx.lastResponse = Api.postJson(Cfg.ADMIN_BASE, path, body);
    }

    @When("以 POST JSON 请求前台 {string} 内容为：")
    public void postJsonPortal(String path, String body) {
        Ctx.lastResponse = Api.postJson(Cfg.PORTAL_BASE, path, body);
    }

    @When("使用伪造 token {string} 以 GET 请求后台 {string}")
    public void getAdminWithRawToken(String rawToken, String path) {
        Ctx.lastResponse = RestAssured.given()
                .baseUri(Cfg.ADMIN_BASE)
                .header("Accept", "application/json")
                .header("Authorization", rawToken)
                .get(path);
    }

    // ==================== 通用：断言 ====================

    @Then("响应业务码应为 {int}")
    public void responseCodeShouldBe(int expected) {
        ApiAssert.codeIs(expected);
    }

    @Then("响应业务码不应为 {int}")
    public void responseCodeShouldNotBe(int unexpected) {
        assertThat(Ctx.lastResponse.jsonPath().getLong("code")).as("业务码").isNotEqualTo(unexpected);
    }

    @Then("响应体 {string} 不应为空")
    public void responseFieldShouldNotBeEmpty(String path) {
        ApiAssert.fieldNotEmpty(path);
    }

    @Then("响应体 {string} 应存在")
    public void responseFieldShouldExist(String path) {
        ApiAssert.fieldExists(path);
    }

    @Then("响应体 {string} 应等于 {string}")
    public void responseFieldShouldEqual(String path, String expected) {
        ApiAssert.fieldEquals(path, expected);
    }

    @Then("响应体 {string} 应为 {int} 位数字")
    public void responseFieldShouldBeDigits(String path, int length) {
        ApiAssert.fieldIsDigits(path, length);
    }

    @Then("HTTP 状态码应为 {int}")
    public void httpStatusShouldBe(int expected) {
        ApiAssert.httpStatusIs(expected);
    }

    // ==================== 后台管理员认证 ====================

    @When("管理员以用户名 {string} 和密码 {string} 登录")
    public void adminLogin(String username, String password) {
        Ctx.tokenMode = Ctx.TokenMode.NONE;
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        Ctx.lastResponse = Api.postJson(Cfg.ADMIN_BASE, "/admin/login", body);
        if (Ctx.lastResponse.jsonPath().getLong("code") == 200) {
            Ctx.adminToken = Ctx.lastResponse.jsonPath().getString("data.token");
        }
    }

    @When("管理员以默认账号登录")
    public void adminLoginWithDefaultAccount() {
        adminLogin(Cfg.ADMIN_USERNAME, Cfg.ADMIN_PASSWORD);
    }

    @Given("管理员已登录并持有有效 token")
    public void adminShouldBeLoggedIn() {
        adminLoginWithDefaultAccount();
        ApiAssert.codeIs(200);
        assertThat(Ctx.adminToken).as("后台 token").isNotBlank();
        Ctx.tokenMode = Ctx.TokenMode.NONE;
    }

    // ==================== 工具方法 ====================

    private static Map<String, String> toParamMap(DataTable table) {
        Map<String, String> result = new LinkedHashMap<>();
        for (List<String> row : table.asLists()) {
            if (row.size() >= 2 && row.get(0) != null && !row.get(0).trim().isEmpty()) {
                result.put(row.get(0).trim(), row.get(1) == null ? "" : row.get(1).trim());
            }
        }
        return result;
    }
}

/**
 * 运行参数：全部可用 -D 覆盖，默认值与 back 仓配置保持一致。
 */
final class Cfg {

    static final String ADMIN_BASE = System.getProperty("admin.baseUrl", "http://localhost:8080");
    static final String PORTAL_BASE = System.getProperty("portal.baseUrl", "http://localhost:8085");
    static final String ADMIN_USERNAME = System.getProperty("admin.username", "admin");
    static final String ADMIN_PASSWORD = System.getProperty("admin.password", "macro123");
    static final String MEMBER_PASSWORD = System.getProperty("member.password", "bdd123456");
    static final String TOKEN_HEAD = "Bearer ";
    static final long PRODUCT_ID = Long.getLong("cart.productId", 1L);
    static final long PRODUCT_SKU_ID = Long.getLong("cart.productSkuId", 1L);

    private Cfg() {
    }
}

/**
 * 单个场景的上下文（场景间由 @Before 重置）。
 */
final class Ctx {

    enum TokenMode {
        NONE, ADMIN, MEMBER
    }

    static TokenMode tokenMode = TokenMode.NONE;
    static Response lastResponse;

    static String adminToken;
    static String memberToken;
    static String memberUsername;
    static String memberPassword;
    static String memberTelephone;
    static String authCode;

    static Long orderId;
    static Long addressId;
    static Integer originalPublishStatus;

    private Ctx() {
    }
}

/**
 * 极简 HTTP 客户端：按当前 tokenMode 自动附加 Authorization 头。
 */
final class Api {

    static Response get(String baseUri, String path) {
        return spec(baseUri).get(path);
    }

    static Response get(String baseUri, String path, Map<String, String> queryParams) {
        return spec(baseUri).queryParams(queryParams).get(path);
    }

    static Response postWithoutParam(String baseUri, String path) {
        return spec(baseUri).post(path);
    }

    static Response postForm(String baseUri, String path, Map<String, String> formParams) {
        return spec(baseUri).contentType(ContentType.URLENC).formParams(formParams).post(path);
    }

    static Response postJson(String baseUri, String path, String body) {
        return spec(baseUri).contentType(ContentType.JSON).body(body).post(path);
    }

    private static RequestSpecification spec(String baseUri) {
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUri)
                .header("Accept", "application/json");
        if (Ctx.tokenMode == Ctx.TokenMode.ADMIN && Ctx.adminToken != null) {
            spec.header("Authorization", Cfg.TOKEN_HEAD + Ctx.adminToken);
        } else if (Ctx.tokenMode == Ctx.TokenMode.MEMBER && Ctx.memberToken != null) {
            spec.header("Authorization", Cfg.TOKEN_HEAD + Ctx.memberToken);
        }
        return spec;
    }

    private Api() {
    }
}

/**
 * 断言工具：统一以「响应体 code」判断业务结果（HTTP 状态码恒为 200）。
 */
final class ApiAssert {

    static void codeIs(int expected) {
        assertThat(Ctx.lastResponse).as("响应对象").isNotNull();
        assertThat(Ctx.lastResponse.jsonPath().getLong("code")).as("业务码").isEqualTo(expected);
    }

    static void fieldNotEmpty(String path) {
        Object value = Ctx.lastResponse.jsonPath().get(path);
        assertThat(value).as("字段 " + path).isNotNull();
        if (value instanceof List) {
            assertThat((List<?>) value).as("字段 " + path).isNotEmpty();
        } else if (value instanceof String) {
            assertThat((String) value).as("字段 " + path).isNotBlank();
        }
    }

    static void fieldExists(String path) {
        assertThat(Ctx.lastResponse.jsonPath().get(path)).as("字段 " + path).isNotNull();
    }

    static void fieldEquals(String path, String expected) {
        Object actual = Ctx.lastResponse.jsonPath().get(path);
        assertThat(actual).as("字段 " + path).isNotNull();
        assertThat(String.valueOf(actual)).as("字段 " + path).isEqualTo(expected);
    }

    static void fieldIsDigits(String path, int length) {
        String value = Ctx.lastResponse.jsonPath().getString(path);
        assertThat(value).as("字段 " + path).isNotBlank().hasSize(length).containsOnlyDigits();
    }

    static void httpStatusIs(int expected) {
        assertThat(Ctx.lastResponse.statusCode()).as("HTTP 状态码").isEqualTo(expected);
    }

    private ApiAssert() {
    }
}
