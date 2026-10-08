package com.macro.mall.portal.contract.steps;

import io.cucumber.java.zh_cn.假如;
import io.cucumber.java.zh_cn.而且;
import io.cucumber.java.zh_cn.当;
import io.cucumber.java.zh_cn.那么;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.Assert;
import org.junit.Assume;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * mall-portal 前台接口契约测试 Step 实现（依据 back 仓 master 分支 mall-portal 模块 Controller 源码生成）。
 *
 * <p>本类为修正版：
 * <ol>
 *   <li>package 与本文件所在目录一致（src/test/java/com/macro/mall/portal/contract/steps）；</li>
 *   <li>公开类名与文件名一致（MallPortalApiContractSteps.java）；</li>
 *   <li>移除原实现中 @而且/@那么 重复注册同一表达式导致的 DuplicateStepDefinitionException 隐患。</li>
 * </ol>
 *
 * <p>服务地址与端口 8085 来自 back 仓 mall-portal 模块 application-dev.yml（未配置 context-path）；
 * 需登录接口的 token 由 POST /sso/login 获取，账号通过 -Dmall.member.username / -Dmall.member.password 传入。
 */
public class MallPortalApiContractSteps {

    private static final String DEFAULT_USERNAME = System.getProperty("mall.member.username", "member");
    private static final String DEFAULT_PASSWORD = System.getProperty("mall.member.password", "123456");

    private String baseUrl = "http://localhost:8085";
    private String token;
    private String requestBody;
    private Response response;

    // ==================== 契约测试步骤（供 mall-portal-api-contract.feature 使用） ====================

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

    /** 注意：同一表达式只在 @而且 注册一次，避免 DuplicateStepDefinitionException。 */
    @而且("响应体字段 {string} 不应为 {int}")
    public void assertJsonFieldNotEquals(String jsonPath, int unexpected) {
        Assert.assertNotEquals("响应体字段 " + jsonPath + " 取值异常", unexpected, response.jsonPath().getInt(jsonPath));
    }

    @而且("响应体字段 {string} 不应为空")
    public void assertJsonFieldNotNull(String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        Assert.assertNotNull("响应体字段 " + jsonPath + " 为空", value);
    }

    // ==================== 结构自检步骤（不依赖被测服务） ====================

    @那么("契约步骤类的包名应为 {string}")
    public void assertStepClassPackage(String expectedPackage) {
        Package pkg = MallPortalApiContractSteps.class.getPackage();
        Assert.assertNotNull("无法获取步骤类的包信息", pkg);
        Assert.assertEquals("步骤类 package 声明与所在目录不一致", expectedPackage, pkg.getName());
    }

    @而且("契约步骤类的简单名应为 {string}")
    public void assertStepClassSimpleName(String expectedSimpleName) {
        Assert.assertEquals("步骤类类名与文件名不一致", expectedSimpleName,
                MallPortalApiContractSteps.class.getSimpleName());
    }

    @那么("文件 {string} 应不存在")
    public void assertFileAbsent(String path) {
        Assert.assertFalse("文件仍存在，需删除：" + path, new File(path).exists());
    }

    /**
     * 校验 OpenAPI 契约文件可被 swagger-parser 解析。
     * 文件尚未落入测试仓时跳过（Assume），避免在未归档 YAML 前把流水线打红。
     */
    @那么("OpenAPI 契约文件 {string} 可被解析为 openapi {string}")
    public void assertOpenApiFileParsable(String path, String expectedVersion) {
        File file = new File(path);
        Assume.assumeTrue("OpenAPI 契约文件尚未落入测试仓，跳过校验：" + path, file.exists());
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(file.getAbsolutePath(), null, null);
        Assert.assertNotNull("OpenAPI 契约文件解析失败：" + path, result.getOpenAPI());
        Assert.assertEquals("openapi 版本不符", expectedVersion, result.getOpenAPI().getOpenapi());
    }

    // ==================== 内部实现 ====================

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
