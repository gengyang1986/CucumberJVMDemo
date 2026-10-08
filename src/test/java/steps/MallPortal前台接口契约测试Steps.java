package com.macro.mall.portal.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * mall-portal 前台接口契约测试步骤实现。
 * 接口清单与字段结构来源于 back 仓库 mall-portal 模块源码分析结果
 * （src/test/resources/openapi/mall-portal.yaml）。
 */
public class PortalApiSteps {

    private String baseUrl = "http://localhost:8085";
    private String token;
    private String lastPath;
    private Response response;

    private RequestSpecification request() {
        RequestSpecification spec = RestAssured.given().baseUri(baseUrl).log().ifValidationFails();
        if (StringUtils.isNotBlank(token)) {
            spec.header("Authorization", token);
        }
        return spec;
    }

    private Map<String, String> parseParams(String paramText) {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isBlank(paramText)) {
            return params;
        }
        for (String kv : paramText.split(",")) {
            int idx = kv.indexOf('=');
            if (idx > 0) {
                params.put(kv.substring(0, idx).trim(), kv.substring(idx + 1).trim());
            }
        }
        return params;
    }

    @Given("前台服务地址为 {string}")
    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    @Given("会员账号 {string} 密码 {string} 已存在")
    public void memberExists(String username, String password) {
        // 数据准备：优先使用已存在账号；不存在时调用 POST /sso/getAuthCode 与 POST /sso/register 创建
        Response resp = request()
                .queryParam("username", username)
                .queryParam("password", password)
                .post("/sso/login");
        if (resp.jsonPath().getInt("code") != 200) {
            throw new IllegalStateException("前置数据准备失败：会员 " + username + " 不存在且无法自动注册，请检查环境");
        }
    }

    @Given("存在已上架商品 {string}")
    public void productExists(String productId) {
        Response resp = request().get("/product/detail/" + productId);
        assertThat(resp.jsonPath().getInt("code")).as("商品 %s 应存在", productId).isEqualTo(200);
    }

    @Given("购物车中存在商品 {string}")
    public void cartItemExists(String productId) {
        request().header("Content-Type", ContentType.JSON)
                .body("{\"productId\":" + productId + ",\"quantity\":1}")
                .post("/cart/add");
    }

    @When("调用 {string} 并传入参数 {string}")
    public void callWithParams(String api, String paramText) {
        String[] parts = api.split(" ");
        String method = parts[0];
        String path = parts[1];
        this.lastPath = path;
        RequestSpecification spec = request().queryParams(parseParams(paramText));
        this.response = "GET".equalsIgnoreCase(method) ? spec.get(path) : spec.post(path);
    }

    @When("调用 {string} 并传入 JSON {string}")
    public void callWithJson(String api, String json) {
        String[] parts = api.split(" ");
        String method = parts[0];
        String path = parts[1];
        this.lastPath = path;
        RequestSpecification spec = request().contentType(ContentType.JSON).body(json);
        this.response = "GET".equalsIgnoreCase(method) ? spec.get(path) : spec.post(path);
    }

    @When("携带 token 调用 {string} 并传入参数 {string}")
    public void callWithTokenAndParams(String api, String paramText) {
        assertThat(token).as("需先执行登录场景获取 token").isNotBlank();
        callWithParams(api, paramText);
    }

    @When("携带 token 调用 {string} 并传入 JSON {string}")
    public void callWithTokenAndJson(String api, String json) {
        assertThat(token).as("需先执行登录场景获取 token").isNotBlank();
        callWithJson(api, json);
    }

    @When("不带 token 调用 {string}")
    public void callWithoutToken(String api) {
        this.token = null;
        String[] parts = api.split(" ");
        this.lastPath = parts[1];
        this.response = request().get(parts[1]);
    }

    @When("携带 token 调用 {string}")
    public void callWithToken(String api) {
        assertThat(token).as("需先执行登录场景获取 token").isNotBlank();
        String[] parts = api.split(" ");
        this.lastPath = parts[1];
        this.response = request().get(parts[1]);
    }

    @Then("响应体 code 应为 {int}")
    public void checkCode(int expectCode) {
        assertThat(response.statusCode()).as("HTTP 状态码").isLessThan(500);
        assertThat(response.jsonPath().getInt("code")).as("%s 返回 code", lastPath).isEqualTo(expectCode);
    }

    @Then("响应体 message 应为 {string}")
    public void checkMessage(String expected) {
        assertThat(response.jsonPath().getString("message")).isEqualTo(expected);
    }

    @Then("响应体 data 应包含字段 {string}")
    public void checkDataFields(String fields) {
        for (String field : fields.split(",")) {
            assertThat(response.jsonPath().get(field.trim()))
                    .as("%s 响应 data 缺少字段 %s", lastPath, field)
                    .isNotNull();
        }
    }

    @Then("响应体 data 应包含分页字段 {string}")
    public void checkPageFields(String fields) {
        for (String field : fields.split(",")) {
            assertThat(response.jsonPath().get("data." + field.trim()))
                    .as("%s 响应 data 缺少分页字段 %s", lastPath, field)
                    .isNotNull();
        }
    }

    @Then("响应体 data 应为数组")
    public void checkDataIsArray() {
        assertThat(response.jsonPath().getList("data")).as("%s 响应 data 应为数组", lastPath).isNotNull();
    }

    @Then("响应体 data 中 list 长度应不大于 {int}")
    public void checkListSize(int max) {
        assertThat(response.jsonPath().getList("data.list").size()).isLessThanOrEqualTo(max);
    }

    @Then("从本次响应中记录 token")
    public void saveToken() {
        String head = response.jsonPath().getString("data.tokenHead");
        String body = response.jsonPath().getString("data.token");
        this.token = StringUtils.defaultString(head) + StringUtils.defaultString(body);
        assertThat(token).isNotBlank();
    }
}
