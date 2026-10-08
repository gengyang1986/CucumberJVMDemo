package com.macro.mall.portal.contract.steps;

import io.cucumber.java.zh_cn.假如;
import io.cucumber.java.zh_cn.那么;
import io.cucumber.java.zh_cn.而且;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * mall-portal OpenAPI 契约文档归档 Step 实现（依据 back 仓 master 分支 com.macro.mall.portal.controller 源码生成）
 *
 * 作用：把 feature 中以 docstring 形式内嵌的 OpenAPI 3.0.3 文档，写出为独立文件
 *      src/test/resources/openapi/mall-portal.yaml，并校验其可被解析、路径与安全方案完整。
 *
 * 依赖（测试仓 pom 需引入）：
 *   io.cucumber:cucumber-java、io.cucumber:cucumber-junit、
 *   io.swagger.parser.v3:swagger-parser、org.yaml:snakeyaml、junit:junit
 */
public class MallPortalOpenApiDocSteps {

    private Path openApiFile;

    @假如("将下列 OpenAPI 文档写入 {string}")
    public void writeOpenApiDocument(String relativePath, String content) throws IOException {
        this.openApiFile = Paths.get(relativePath);
        Path parent = this.openApiFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(this.openApiFile, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertTrue("OpenAPI 文档未成功写入: " + this.openApiFile, Files.exists(this.openApiFile));
    }

    @那么("该文档应可通过 OpenAPI 3.0.3 解析")
    public void assertParsableByOpenApi303() throws IOException {
        Assert.assertNotNull("尚未写入 OpenAPI 文档", this.openApiFile);
        String content = new String(Files.readAllBytes(this.openApiFile), StandardCharsets.UTF_8);
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(content, null, null);
        Assert.assertNotNull("OpenAPI 文档解析失败，未返回模型", result.getOpenAPI());
        Assert.assertTrue("OpenAPI 文档存在解析告警: " + result.getMessages(),
                result.getMessages() == null || result.getMessages().isEmpty());
        Assert.assertEquals("OpenAPI 版本号不符", "3.0.3", result.getOpenAPI().getOpenapi());
    }

    @而且("文档中的接口路径应至少包含 {int} 个")
    public void assertPathCountAtLeast(int expected) throws IOException {
        OpenAPI openAPI = parse();
        Assert.assertNotNull("OpenAPI 文档缺少 paths 定义", openAPI.getPaths());
        int actual = openAPI.getPaths().size();
        Assert.assertTrue("接口路径数量不足，期望至少 " + expected + " 个，实际 " + actual, actual >= expected);
    }

    @而且("文档中应包含安全方案 {string}")
    public void assertSecuritySchemeExists(String schemeName) throws IOException {
        OpenAPI openAPI = parse();
        Assert.assertNotNull("OpenAPI 文档未定义 components.securitySchemes", openAPI.getComponents());
        Assert.assertNotNull("OpenAPI 文档未定义 components.securitySchemes", openAPI.getComponents().getSecuritySchemes());
        Assert.assertTrue("缺少安全方案: " + schemeName,
                openAPI.getComponents().getSecuritySchemes().containsKey(schemeName));
    }

    private OpenAPI parse() throws IOException {
        Assert.assertNotNull("尚未写入 OpenAPI 文档", this.openApiFile);
        String content = new String(Files.readAllBytes(this.openApiFile), StandardCharsets.UTF_8);
        return new OpenAPIV3Parser().readContents(content, null, null).getOpenAPI();
    }
}
