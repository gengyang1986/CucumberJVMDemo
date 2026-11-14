package com.macro.mall.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台管理员认证与鉴权 step 定义（自包含，不依赖 Cfg/Ctx/Api/ApiAssert 等其它产物）。
 *
 * 设计约束：
 * 1) 类声明为包私有（非 public），文件名由平台按特性标题生成时不会触发
 *    "public class should be declared in a file named xxx.java" 编译错误；
 * 2) 管理端地址与账号全部由环境管理页面「管理面」环境注入，取不到直接失败，不做 localhost 兜底：
 *      admin.base.url / ADMIN_BASE_URL、admin.username / ADMIN_USERNAME、admin.password / ADMIN_PASSWORD
 * 3) 断言只用 AssertJ（与仓内其它 step 一致），不使用 Java 15 文本块，兼容 maven.compiler.release=17。
 */
class AdminAuthSteps {

    private static final String LOGIN_PATH = "/admin/login";
    private static final String TOKEN_HEADER = "Authorization";

    private Response lastResponse;
    private String adminToken;

    // ---------------------------------------------------------------- Given

    @Given("管理端测试环境配置已就绪")
    public void adminEnvironmentReady() {
        baseUrl();
        username();
        password();
    }

    @Given("已使用环境配置的管理端账号登录并取得 token")
    public void loginAndKeepToken() {
        lastResponse = request().body(loginBody(username(), password())).post(LOGIN_PATH);
        assertThat(businessCode())
                .as("登录业务码，响应体：%s", body())
                .isEqualTo(200L);
        adminToken = lastResponse.jsonPath().getString("data.token");
        assertThat(adminToken).as("登录返回的 token").isNotBlank();
    }

    // ----------------------------------------------------------------- When

    @When("使用环境配置的管理端账号登录")
    public void loginWithConfiguredAccount() {
        lastResponse = request().body(loginBody(username(), password())).post(LOGIN_PATH);
    }

    @When("使用环境配置的管理端账号搭配错误密码 {string} 登录")
    public void loginWithWrongPassword(String wrongPassword) {
        lastResponse = request().body(loginBody(username(), wrongPassword)).post(LOGIN_PATH);
    }

    @When("使用不存在的管理端账号登录")
    public void loginWithUnknownAccount() {
        String unknown = "no_such_admin_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        lastResponse = request().body(loginBody(unknown, password())).post(LOGIN_PATH);
    }

    @When("使用环境配置的管理端账号但不提交密码字段登录")
    public void loginWithoutPasswordField() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username());
        lastResponse = request().body(payload).post(LOGIN_PATH);
    }

    @When("不携带 token 以 GET 请求管理端接口 {string}")
    public void getWithoutToken(String path) {
        lastResponse = request().get(path);
    }

    @When("携带无效 token 以 GET 请求管理端接口 {string}")
    public void getWithInvalidToken(String path) {
        lastResponse = request().header(TOKEN_HEADER, "Bearer invalid.token.value").get(path);
    }

    @When("携带管理端 token 以 GET 请求管理端接口 {string}")
    public void getWithAdminToken(String path) {
        lastResponse = request().header(TOKEN_HEADER, bearerToken()).get(path);
    }

    @When("携带管理端 token 以 POST 请求管理端接口 {string}")
    public void postWithAdminToken(String path) {
        lastResponse = request().header(TOKEN_HEADER, bearerToken()).post(path);
    }

    // ----------------------------------------------------------------- Then

    @Then("后台响应 HTTP 状态码应为 {int}")
    public void httpStatusShouldBe(int expected) {
        assertThat(lastResponse.statusCode())
                .as("HTTP 状态码，响应体：%s", body())
                .isEqualTo(expected);
    }

    @Then("后台响应业务码应为 {int}")
    public void businessCodeShouldBe(int expected) {
        assertThat(businessCode())
                .as("业务码 code，响应体：%s", body())
                .isEqualTo((long) expected);
    }

    @Then("后台响应 message 应为 {string}")
    public void messageShouldBe(String expected) {
        assertThat(lastResponse.jsonPath().getString("message"))
                .as("响应 message，响应体：%s", body())
                .isEqualTo(expected);
    }

    @Then("后台响应 message 应非空")
    public void messageShouldNotBeBlank() {
        assertThat(lastResponse.jsonPath().getString("message"))
                .as("响应 message，响应体：%s", body())
                .isNotBlank();
    }

    @Then("后台响应 data 字段 {string} 应非空")
    public void dataFieldShouldNotBeBlank(String field) {
        Object value = dataField(field);
        assertThat(value).as("data.%s 不应为 null，响应体：%s", field, body()).isNotNull();
        assertThat(String.valueOf(value)).as("data.%s 不应为空串", field).isNotBlank();
    }

    @Then("后台响应 data 字段 {string} 应等于环境配置的管理端用户名")
    public void dataFieldShouldEqualConfiguredUsername(String field) {
        assertThat(dataField(field)).as("data.%s", field).isNotNull();
        assertThat(String.valueOf(dataField(field))).as("data.%s", field).isEqualTo(username());
    }

    @Then("后台响应 data 字段 {string} 应以 {string} 开头")
    public void dataFieldShouldStartWith(String field, String prefix) {
        Object value = dataField(field);
        assertThat(value).as("data.%s 不应为 null，响应体：%s", field, body()).isNotNull();
        assertThat(String.valueOf(value)).as("data.%s", field).startsWith(prefix);
    }

    @Then("后台响应 data 字段 {string} 应为数组")
    public void dataFieldShouldBeArray(String field) {
        assertThat(dataField(field))
                .as("data.%s 应为数组（无角色账号返回空数组），响应体：%s", field, body())
                .isInstanceOf(List.class);
    }

    // -------------------------------------------------------------- 内部实现

    private RequestSpecification request() {
        return given()
                .baseUri(baseUrl())
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    private Map<String, Object> loginBody(String user, String pwd) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", user);
        payload.put("password", pwd);
        return payload;
    }

    private String bearerToken() {
        assertThat(adminToken)
                .as("前置步骤未取得管理端 token，请检查「已使用环境配置的管理端账号登录并取得 token」")
                .isNotBlank();
        return "Bearer " + adminToken;
    }

    private long businessCode() {
        Object code = lastResponse.jsonPath().get("code");
        assertThat(code)
                .as("响应体不是预期的 CommonResult JSON，http=%s，原文：%s", lastResponse.statusCode(), body())
                .isNotNull();
        return ((Number) code).longValue();
    }

    private Object dataField(String field) {
        return lastResponse.jsonPath().get("data." + field);
    }

    private String body() {
        if (lastResponse == null) {
            return "<无响应>";
        }
        String raw = lastResponse.asString();
        if (raw == null) {
            return "<空>";
        }
        return raw.length() > 500 ? raw.substring(0, 500) + "…" : raw;
    }

    private static String baseUrl() {
        String url = require("admin.base.url", "ADMIN_BASE_URL", "管理端服务地址");
        return url.replaceAll("/+$", "");
    }

    private static String username() {
        return require("admin.username", "ADMIN_USERNAME", "管理端账号");
    }

    private static String password() {
        return require("admin.password", "ADMIN_PASSWORD", "管理端密码");
    }

    /**
     * 优先系统属性，其次环境变量；两者都没有时直接失败，避免回落到 localhost 之类与实际环境无关的地址。
     */
    private static String require(String sysKey, String envKey, String label) {
        String value = System.getProperty(sysKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(envKey);
        }
        if (value != null) {
            value = value.trim();
        }
        assertThat(value)
                .as("%s 未配置：请从环境管理页面「管理面」环境注入（-D%s 或环境变量 %s）", label, sysKey, envKey)
                .isNotBlank();
        return value;
    }
}