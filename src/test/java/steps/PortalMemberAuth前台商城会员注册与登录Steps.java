package com.macro.mall.bdd.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 前台商城-会员注册与登录（对应 portal_member_auth.feature）
 *
 * 说明：
 * 1. 网关地址默认 http://localhost:88888，可用 -Dapi.baseUrl=... 覆盖；
 * 2. 场景 2/3/4 依赖测试环境已存在会员账号，默认 bdd_member/123456，
 *    可用 -Dmember.username / -Dmember.password 覆盖；
 * 3. 注册场景使用随机手机号 + 随机用户名，可重复执行；验证码直接取 /sso/getAuthCode 的响应 data
 *    （若环境关闭了验证码回显，可改为从 Redis key ums:authCode 读取后在步骤中注入）；
 * 4. 本类步骤文案全局唯一，防止与其他 feature 的 step 定义重复。
 */
public class PortalMemberAuthSteps {

    private static final String BASE_URL = System.getProperty("api.baseUrl",
            System.getenv().getOrDefault("API_BASE_URL", "http://localhost:88888"));
    private static final String MEMBER_USERNAME = System.getProperty("member.username", "bdd_member");
    private static final String MEMBER_PASSWORD = System.getProperty("member.password", "123456");

    private Response response;
    private String token;
    private String randomTelephone;
    private String randomUsername;
    private String authCode;

    @Before(order = 20)
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    @Given("前台商城服务地址使用测试配置")
    public void portalApiBaseUrlIsConfigured() {
        assertThat(BASE_URL).as("前台商城服务地址不能为空").isNotBlank();
    }

    private void loginMember(String username, String password) {
        response = given()
                .contentType(ContentType.URLENC)
                .formParam("username", username)
                .formParam("password", password)
                .when()
                .post("/sso/login");
        token = response.jsonPath().getString("data.token");
    }

    @When("会员为随机生成的手机号获取验证码")
    public void memberGetAuthCodeWithRandomTelephone() {
        randomTelephone = "138" + String.format("%08d", new Random().nextInt(100_000_000));
        randomUsername = "bdd_" + System.currentTimeMillis();
        response = given()
                .queryParam("telephone", randomTelephone)
                .when()
                .get("/sso/getAuthCode");
        authCode = response.jsonPath().getString("data");
    }

    @When("会员使用该手机号、随机生成的用户名和密码 {string} 完成注册")
    public void memberRegister(String password) {
        assertThat(authCode)
                .as("未获取到验证码，请确认 /sso/getAuthCode 是否回显验证码，或改从 Redis key ums:authCode 读取")
                .isNotBlank();
        response = given()
                .contentType(ContentType.URLENC)
                .formParam("username", randomUsername)
                .formParam("password", password)
                .formParam("telephone", randomTelephone)
                .formParam("authCode", authCode)
                .when()
                .post("/sso/register");
    }

    @When("会员使用该随机生成的用户名和密码 {string} 登录")
    public void memberLoginWithRandomUsername(String password) {
        loginMember(randomUsername, password);
    }

    @When("会员使用配置中的会员账号和密码登录")
    public void memberLoginWithConfiguredAccount() {
        loginMember(MEMBER_USERNAME, MEMBER_PASSWORD);
    }

    @When("会员使用配置中的会员账号和错误密码 {string} 登录")
    public void memberLoginWithWrongPassword(String wrongPassword) {
        loginMember(MEMBER_USERNAME, wrongPassword);
    }

    @Given("会员已使用配置中的会员账号登录并获取访问令牌")
    public void memberHasLoggedIn() {
        loginMember(MEMBER_USERNAME, MEMBER_PASSWORD);
        assertThat(token)
                .as("会员登录未获取到 token，请确认测试环境已存在会员 %s（可用 -Dmember.username / -Dmember.password 覆盖）",
                        MEMBER_USERNAME)
                .isNotBlank();
    }

    @When("携带会员令牌请求会员信息接口")
    public void requestMemberInfoWithToken() {
        response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/sso/info");
    }

    @Then("会员接口返回码为 {int}")
    public void memberApiCodeShouldBe(int code) {
        assertThat(response.jsonPath().getInt("code")).as("会员接口返回码，响应体：%s", response.asString()).isEqualTo(code);
    }

    @Then("验证码响应消息为 {string}")
    public void authCodeMessageShouldBe(String message) {
        assertThat(response.jsonPath().getString("message")).as("验证码接口返回消息").isEqualTo(message);
    }

    @Then("会员注册响应消息为 {string}")
    public void registerMessageShouldBe(String message) {
        assertThat(response.jsonPath().getString("message")).as("注册接口返回消息").isEqualTo(message);
    }

    @Then("会员登录响应中的 token 不为空")
    public void memberLoginTokenShouldNotBeEmpty() {
        assertThat(response.jsonPath().getString("data.token")).as("会员登录响应 data.token").isNotBlank();
    }

    @Then("会员登录响应消息为 {string}")
    public void memberLoginMessageShouldBe(String message) {
        assertThat(response.jsonPath().getString("message")).as("会员登录接口返回消息").isEqualTo(message);
    }

    @Then("会员信息接口返回的会员名等于配置中的会员账号")
    public void memberInfoUsernameShouldEqualConfigured() {
        assertThat(response.jsonPath().getString("data.username")).as("会员信息接口 data.username").isEqualTo(MEMBER_USERNAME);
    }
}
