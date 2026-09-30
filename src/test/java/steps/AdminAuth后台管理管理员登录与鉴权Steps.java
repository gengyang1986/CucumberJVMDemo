package com.macro.mall.bdd.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台管理-管理员登录与鉴权（对应 admin_auth.feature）
 *
 * 约定：
 * 1. 网关地址默认 http://localhost:88888，可用 -Dapi.baseUrl=... 覆盖；
 * 2. 管理员账号默认 admin/123456，可用 -Dadmin.username / -Dadmin.password 覆盖；
 * 3. 本类步骤文案全局唯一，防止与其他 feature 的 step 定义重复（重复会导致 Cucumber 启动失败）。
 */
public class AdminAuthSteps {

    private static final String BASE_URL = System.getProperty("api.baseUrl",
            System.getenv().getOrDefault("API_BASE_URL", "http://localhost:88888"));
    private static final String ADMIN_USERNAME = System.getProperty("admin.username", "admin");
    private static final String ADMIN_PASSWORD = System.getProperty("admin.password", "123456");

    private Response response;
    private String token;

    @Before(order = 0)
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    @Given("后台管理服务地址使用测试配置")
    public void adminApiBaseUrlIsConfigured() {
        assertThat(BASE_URL).as("后台管理服务地址不能为空").isNotBlank();
    }

    @When("管理员使用用户名 {string} 和密码 {string} 登录后台")
    public void loginAdmin(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/admin/login");
    }

    @Given("后台管理员 {string} 已登录并成功获取访问令牌")
    public void adminHasLoggedIn(String username) {
        loginAdmin(username, ADMIN_PASSWORD);
        token = response.jsonPath().getString("data.token");
        assertThat(token)
                .as("管理员登录未获取到 token，请检查后台账号数据与 admin.username/admin.password 配置")
                .isNotBlank();
    }

    @When("携带已获取的访问令牌请求当前管理员信息接口")
    public void requestAdminInfoWithToken() {
        response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/admin/info");
    }

    @When("不携带访问令牌请求后台接口 {string}")
    public void requestWithoutToken(String path) {
        response = given().when().get(path);
    }

    @Then("后台登录接口返回码为 {int}")
    public void loginApiCodeShouldBe(int code) {
        assertThat(response.jsonPath().getInt("code")).as("登录接口返回码").isEqualTo(code);
    }

    @Then("后台登录接口返回消息为 {string}")
    public void loginApiMessageShouldBe(String message) {
        assertThat(response.jsonPath().getString("message")).as("登录接口返回消息").isEqualTo(message);
    }

    @Then("后台登录响应中的 token 不为空")
    public void loginTokenShouldNotBeEmpty() {
        assertThat(response.jsonPath().getString("data.token")).as("登录响应 data.token").isNotBlank();
    }

    @Then("后台登录响应中的 tokenHead 包含 {string}")
    public void loginTokenHeadShouldContain(String tokenHead) {
        assertThat(response.jsonPath().getString("data.tokenHead")).as("登录响应 data.tokenHead").contains(tokenHead);
    }

    @Then("后台信息接口返回码为 {int}")
    public void adminInfoApiCodeShouldBe(int code) {
        assertThat(response.jsonPath().getInt("code")).as("管理员信息接口返回码").isEqualTo(code);
    }

    @Then("后台信息接口返回的管理员用户名不为空")
    public void adminInfoUsernameShouldNotBeEmpty() {
        assertThat(response.jsonPath().getString("data.username")).as("管理员信息接口 data.username").isNotBlank();
    }

    @Then("后台受保护接口返回码为 {int}")
    public void protectedApiCodeShouldBe(int code) {
        // 未携带令牌时由 mall-security 的 RestAuthenticationEntryPoint 输出 CommonResult.unauthorized
        assertThat(response.jsonPath().getInt("code")).as("受保护接口返回码").isEqualTo(code);
    }
}
