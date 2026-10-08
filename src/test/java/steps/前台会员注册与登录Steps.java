package com.macro.mall.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 前台商城（mall-portal，默认 http://localhost:8085）会员注册、登录与令牌管理步骤定义。
 *
 * 依赖 AdminAuthSteps 所在文件中的公共支撑类：Cfg / Ctx / Api / ApiAssert（同包）。
 * 登录使用“注册时的用户名”；若环境改为手机号登录，只需调整 loginMember 的入参字段。
 */
public class MemberAuthSteps {

    @Given("商城端服务地址为 {string}")
    public void setPortalBaseUrl(String baseUrl) {
        Api.portalBase = baseUrl;
    }

    @Given("已注册并登录一个随机会员账号")
    public void registerAndLoginRandomMember() {
        Ctx.reset();
        registerRandomMember();
        ApiAssert.code(200);
        loginMember(Ctx.memberUsername, Cfg.MEMBER_PASSWORD);
        assertThat(Ctx.memberToken).as("会员 token").isNotBlank();
    }

    @When("使用随机手机号与验证码注册会员")
    public void registerRandomMember() {
        Ctx.memberTelephone = randomTelephone();
        Ctx.memberUsername = "bdd_" + System.currentTimeMillis();
        String authCode = fetchAuthCode(Ctx.memberTelephone);
        Map<String, Object> form = new HashMap<>();
        form.put("username", Ctx.memberUsername);
        form.put("password", Cfg.MEMBER_PASSWORD);
        form.put("telephone", Ctx.memberTelephone);
        form.put("authCode", authCode);
        Api.postForm(Api.portalBase, null, "/sso/register", form);
    }

    @When("使用随机手机号与错误验证码 {string} 注册会员")
    public void registerWithWrongAuthCode(String wrongAuthCode) {
        Ctx.memberTelephone = randomTelephone();
        Ctx.memberUsername = "bdd_" + System.currentTimeMillis();
        Api.get(Api.portalBase, null, "/sso/getAuthCode?telephone=" + Ctx.memberTelephone);
        Map<String, Object> form = new HashMap<>();
        form.put("username", Ctx.memberUsername);
        form.put("password", Cfg.MEMBER_PASSWORD);
        form.put("telephone", Ctx.memberTelephone);
        form.put("authCode", wrongAuthCode);
        Api.postForm(Api.portalBase, null, "/sso/register", form);
    }

    @When("为手机号 {string} 获取验证码")
    public void getAuthCode(String telephone) {
        Api.get(Api.portalBase, null, "/sso/getAuthCode?telephone=" + telephone);
    }

    @When("使用该随机会员账号与密码 {string} 登录商城端")
    public void loginWithRandomMember(String password) {
        loginMember(Ctx.memberUsername, password);
    }

    @When("使用该随机会员账号与错误密码 {string} 登录商城端")
    public void loginWithWrongPassword(String password) {
        loginMember(Ctx.memberUsername, password);
    }

    @When("携带会员 token 以 GET 请求商城端 {string}")
    public void portalGetWithToken(String path) {
        Api.get(Api.portalBase, Ctx.memberToken, path);
    }

    @When("不携带 token 以 GET 请求商城端 {string}")
    public void portalGetAnonymous(String path) {
        Api.get(Api.portalBase, null, path);
    }

    @When("携带会员 token 以 POST 请求商城端 {string}")
    public void portalPostWithToken(String path) {
        Api.postForm(Api.portalBase, Ctx.memberToken, path, new HashMap<>());
    }

    @Then("商城端响应业务码应为 {int}")
    public void portalBusinessCode(int expected) {
        ApiAssert.code(expected);
    }

    @Then("商城端响应 message 应为 {string}")
    public void portalMessage(String expected) {
        ApiAssert.message(expected);
    }

    @Then("商城端响应 data 应非空")
    public void portalDataNotBlank() {
        ApiAssert.dataNotBlank("");
    }

    @Then("商城端响应 data 应为空")
    public void portalDataEmpty() {
        ApiAssert.dataEmpty("");
    }

    @Then("商城端响应 data 应等于 {string}")
    public void portalDataEquals(String expected) {
        ApiAssert.dataEquals("", expected);
    }

    @Then("商城端响应 data 字段 {string} 应非空")
    public void portalDataFieldNotBlank(String path) {
        ApiAssert.dataNotBlank(path);
    }

    @Then("商城端响应 data 字段 {string} 应等于 {string}")
    public void portalDataFieldEquals(String path, String expected) {
        ApiAssert.dataEquals(path, expected);
    }

    @Then("商城端响应 data 字段 {string} 应等于刚下单的订单号")
    public void portalDataFieldEqualsLastOrderId(String path) {
        assertThat(Ctx.orderId).as("刚下单的订单号").isNotNull();
        ApiAssert.dataEquals(path, String.valueOf(Ctx.orderId));
    }

    @Then("商城端响应 data 应为 6 位数字")
    public void portalDataIsSixDigitNumber() {
        assertThat(Api.last().jsonPath().getString("data")).as("验证码").matches("\\d{6}");
    }

    @Then("商城端响应 data 数组长度应为 {int}")
    public void portalDataArraySize(int expected) {
        java.util.List<Object> list = Api.last().jsonPath().getList("data");
        assertThat(list == null ? 0 : list.size()).as("data 数组长度").isEqualTo(expected);
    }

    private String fetchAuthCode(String telephone) {
        Api.get(Api.portalBase, null, "/sso/getAuthCode?telephone=" + telephone);
        ApiAssert.code(200);
        String authCode = Api.last().jsonPath().getString("data");
        assertThat(authCode).as("注册验证码").isNotBlank();
        return authCode;
    }

    private void loginMember(String username, String password) {
        Ctx.memberToken = null;
        Map<String, Object> form = new HashMap<>();
        form.put("username", username);
        form.put("password", password);
        Api.postForm(Api.portalBase, null, "/sso/login", form);
        String token = Api.last().jsonPath().getString("data.token");
        if (token != null) {
            Ctx.memberToken = token;
        }
    }

    private String randomTelephone() {
        return "139" + String.format("%08d", ThreadLocalRandom.current().nextInt(100000000));
    }
}
