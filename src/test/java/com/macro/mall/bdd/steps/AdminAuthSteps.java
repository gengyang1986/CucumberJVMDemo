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
 * 后台管理端（mall-admin）认证与鉴权步骤定义。
 *
 * <p>三条硬约束，均为此前批次踩过的坑：
 * <ol>
 *   <li>类名必须与文件名一致（AdminAuthSteps / AdminAuthSteps.java），否则 javac 报
 *       "class X is public, should be declared in a file named X.java"；</li>
 *   <li>自包含，不引用 Ctx / Cfg / Api / ApiAssert 等共享支撑类，避免支撑类被删后整批场景
 *       变成 UndefinedStepException；</li>
 *   <li>地址与账号只从环境配置读取，取不到直接失败，不再回落 localhost。</li>
 * </ol>
 *
 * <p>环境配置读取优先级：System property &gt; 环境变量（环境管理页面下发的变量）。
 */
public class AdminAuthSteps {

    /** 管理端登录路径。 */
    private static final String LOGIN_PATH = "/admin/login";

    /** 登录服务端返回的 tokenHead，默认 "Bearer "。 */
    private static String tokenHead = "Bearer ";

    /** 当前场景的管理端 token。 */
    private static String token;

    /** 最近一次响应。 */
    private static Response last;

    // ------------------------------------------------------------------ 环境配置

    private static String baseUrl() {
        return trimTrailingSlash(firstNonBlank(
                System.getProperty("admin.base.url"),
                System.getenv("ADMIN_BASE_URL"),
                System.getProperty("api.base.url"),
                System.getenv("API_BASE_URL")));
    }

    private static String adminUsername() {
        return firstNonBlank(
                System.getProperty("admin.username"),
                System.getenv("ADMIN_USERNAME"),
                System.getenv("username"));
    }

    private static String adminPassword() {
        return firstNonBlank(
                System.getProperty("admin.password"),
                System.getenv("ADMIN_PASSWORD"),
                System.getenv("password"));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String trimTrailingSlash(String url) {
        return url == null ? null : url.replaceAll("/+$", "");
    }

    // ------------------------------------------------------------------ Given

    @Given("管理端测试环境配置已就绪")
    public void adminEnvironmentReady() {
        assertThat(baseUrl())
                .as("管理端服务地址未配置：请配置 admin.base.url 或环境变量 ADMIN_BASE_URL（取自环境管理「管理面」环境的 baseUrl）")
                .isNotBlank();
        assertThat(adminUsername())
                .as("管理端用户名未配置：请配置 admin.username 或环境变量 ADMIN_USERNAME")
                .isNotBlank();
        assertThat(adminPassword())
                .as("管理端密码未配置：请配置 admin.password 或环境变量 ADMIN_PASSWORD")
                .isNotBlank();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Given("已使用环境配置的管理端账号登录并取得 token")
    public void loginAndKeepToken() {
        doLogin(adminUsername(), adminPassword(), true);
        assertThat(businessCode()).as("登录业务码，响应体=%s", body()).isEqualTo(200L);
        String newToken = last.jsonPath().getString("data.token");
        assertThat(newToken).as("登录返回的 token，响应体=%s", body()).isNotBlank();
        String head = last.jsonPath().getString("data.tokenHead");
        if (head != null && !head.trim().isEmpty()) {
            tokenHead = head;
        }
        token = newToken;
    }

    // ------------------------------------------------------------------ When

    @When("使用环境配置的管理端账号登录")
    public void loginWithConfiguredAccount() {
        doLogin(adminUsername(), adminPassword(), true);
    }

    @When("使用环境配置的管理端账号搭配错误密码 {string} 登录")
    public void loginWithWrongPassword(String wrongPassword) {
        doLogin(adminUsername(), wrongPassword, true);
    }

    @When("使用不存在的管理端账号登录")
    public void loginWithUnknownAccount() {
        doLogin("bdd-no-such-admin-" + System.currentTimeMillis(), "wrong-password", true);
    }

    @When("使用环境配置的管理端账号但不提交密码字段登录")
    public void loginWithoutPasswordField() {
        doLogin(adminUsername(), null, false);
    }

    @When("不携带 token 以 GET 请求管理端接口 {string}")
    public void getWithoutToken(String path) {
        last = jsonSpec().get(path);
    }

    @When("携带无效 token 以 GET 请求管理端接口 {string}")
    public void getWithInvalidToken(String path) {
        last = jsonSpec().header("Authorization", "Bearer invalid.token.value").get(path);
    }

    @When("携带管理端 token 以 GET 请求管理端接口 {string}")
    public void getWithToken(String path) {
        last = specWithToken().get(path);
    }

    @When("携带管理端 token 以 POST 请求管理端接口 {string}")
    public void postWithToken(String path) {
        last = specWithToken().post(path);
    }

    // ------------------------------------------------------------------ Then

    @Then("后台响应 HTTP 状态码应为 {int}")
    public void httpStatusShouldBe(int expected) {
        assertThat(last.statusCode()).as("HTTP 状态码，响应体=%s", body()).isEqualTo(expected);
    }

    @Then("后台响应业务码应为 {int}")
    public void businessCodeShouldBe(int expected) {
        assertThat(businessCode()).as("业务码，响应体=%s", body()).isEqualTo((long) expected);
    }

    @Then("后台响应 message 应为 {string}")
    public void messageShouldBe(String expected) {
        assertThat(last.jsonPath().getString("message"))
                .as("message，响应体=%s", body()).isEqualTo(expected);
    }

    @Then("后台响应 message 应非空")
    public void messageShouldNotBeBlank() {
        assertThat(last.jsonPath().getString("message"))
                .as("message，响应体=%s", body()).isNotBlank();
    }

    @Then("后台响应 data 字段 {string} 应非空")
    public void dataFieldShouldNotBeBlank(String field) {
        Object value = last.jsonPath().get("data." + field);
        assertThat(value).as("data.%s，响应体=%s", field, body()).isNotNull();
        assertThat(String.valueOf(value)).as("data.%s，响应体=%s", field, body()).isNotBlank();
    }

    @Then("后台响应 data 字段 {string} 应以 {string} 开头")
    public void dataFieldShouldStartWith(String field, String prefix) {
        String value = last.jsonPath().getString("data." + field);
        assertThat(value).as("data.%s，响应体=%s", field, body()).isNotNull().startsWith(prefix);
    }

    @Then("后台响应 data 字段 {string} 应等于环境配置的管理端用户名")
    public void dataFieldShouldEqualAdminUsername(String field) {
        assertThat(last.jsonPath().getString("data." + field))
                .as("data.%s，响应体=%s", field, body())
                .isEqualTo(adminUsername());
    }

    @Then("后台响应 data 字段 {string} 应为数组")
    public void dataFieldShouldBeArray(String field) {
        Object value = last.jsonPath().get("data." + field);
        assertThat(value).as("data.%s，响应体=%s", field, body()).isNotNull();
        assertThat(value).as("data.%s，响应体=%s", field, body()).isInstanceOf(List.class);
    }

    // ------------------------------------------------------------------ 内部实现

    private static void doLogin(String username, String password, boolean withPasswordField) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        if (withPasswordField) {
            requestBody.put("password", password);
        }
        last = jsonSpec().body(requestBody).post(LOGIN_PATH);
    }

    private static RequestSpecification jsonSpec() {
        return RestAssured.given()
                .baseUri(baseUrl())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    private static RequestSpecification specWithToken() {
        assertThat(token).as("尚未取得管理端 token，请先执行登录步骤").isNotBlank();
        return jsonSpec().header("Authorization", tokenHead + token);
    }

    private static long businessCode() {
        Long code = last.jsonPath().getLong("code");
        return code == null ? Long.MIN_VALUE : code;
    }

    private static String body() {
        return last == null ? "<无响应>" : last.asString();
    }
}
