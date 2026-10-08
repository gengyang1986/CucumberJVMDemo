package com.macro.mall.portal.contract.steps;

import io.cucumber.java.zh_cn.假如;
import io.cucumber.java.zh_cn.当;
import io.cucumber.java.zh_cn.那么;
import io.cucumber.java.zh_cn.而且;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * mall-portal 前台接口契约测试 Step 实现（依据 back 仓 master 分支源码生成）
 *
 * 依赖（测试仓 pom 需引入）：
 *   io.cucumber:cucumber-java、io.cucumber:cucumber-junit、io.rest-assured:rest-assured、junit:junit
 *
 * 可通过 -Dmall.member.username / -Dmall.member.password 指定登录账号，默认 member/123456。
 * 契约来源：back 仓 mall-portal 模块（Controller 中的 @Tag/@Operation 与 @RequestMapping）。
 */
public class PortalApiContractSteps {

    private static final String DEFAULT_USERNAME = System.getProperty("mall.member.username", "member");
    private static final String DEFAULT_PASSWORD = System.getProperty("mall.member.password", "123456");

    private String baseUrl = "http://localhost:8085";
    private String token;
    private String requestBody;
    private Response response;

    @假如("前台服务地址为 {string}")
    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    @假如("已使用会员账号登录并获取 token")
    public void loginWithMemberAccount() {
        this.requestBody = null;
        Response loginResponse = spec(null)
                .queryParam("username", DEFAULT_USERNAME)
                .queryParam("password", DEFAULT_PASSWORD)
                .request("POST", "/sso/login");
        Assert.assertEquals("登录接口未返回成功业务码", 200, loginResponse.jsonPath().getInt("code"));
        this.token = loginResponse.jsonPath().getString("data.token");
        Assert.assertNotNull("登录接口未返回 data.token", this.token);
    }

    @而且("请求体为 {string}")
    public void setRequestBody(String body) {
        this.requestBody = body;
    }

    @当("以 {string} 方法请求 {string}")
    public void request(String method, String path) {
        execute(method, path, null);
    }

    @当("携带 token 以 {string} 方法请求 {string}")
    public void requestWithToken(String method, String path) {
        Assert.assertNotNull("未获取到 token，无法发起受保护接口请求", this.token);
        execute(method, path, this.token);
    }

    @那么("响应状态码为 {int}")
    public void assertHttpStatus(int expectedStatus) {
        Assert.assertEquals("HTTP 状态码不符", expectedStatus, response.getStatusCode());
    }

    @而且("响应体字段 {string} 应为 {int}")
    public void assertJsonFieldEquals(String jsonPath, int expected) {
        Assert.assertEquals("响应体字段 " + jsonPath + " 不符", expected, response.jsonPath().getInt(jsonPath));
    }

    @而且("响应体字段 {string} 不应为 {int}")
    public void assertJsonFieldNotEquals(String jsonPath, int unexpected) {
        Assert.assertNotEquals("响应体字段 " + jsonPath + " 取值异常", unexpected, response.jsonPath().getInt(jsonPath));
    }

    @那么("响应体字段 {string} 不应为 {int}")
    public void assertJsonFieldNotEqualsThen(String jsonPath, int unexpected) {
        assertJsonFieldNotEquals(jsonPath, unexpected);
    }

    @而且("响应体字段 {string} 不应为空")
    public void assertJsonFieldNotNull(String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        Assert.assertNotNull("响应体字段 " + jsonPath + " 为空", value);
    }

    private void execute(String method, String path, String authToken) {
        String uri = path;
        Map<String, String> queryParams = new HashMap<>();
        int index = path.indexOf('?');
        if (index >= 0) {
            uri = path.substring(0, index);
            String query = path.substring(index + 1);
            for (String pair : query.split("&")) {
                if (pair.isEmpty()) {
                    continue;
                }
                String[] kv = pair.split("=", 2);
                queryParams.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }
        RequestSpecification spec = spec(authToken).queryParams(queryParams);
        if (requestBody != null) {
            spec.body(requestBody);
        }
        response = spec.request(method, uri);
        requestBody = null;
    }

    private RequestSpecification spec(String authToken) {
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        if (authToken != null) {
            spec.header("Authorization", "Bearer " + authToken);
        }
        return spec;
    }
}
