package com.macro.mall.portal.contract.scaffold;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 构建底座（pom 基线）自检步骤。
 *
 * <p>不依赖被测服务，可在 CI 中绿灯运行，用于保证内嵌的 pom 基线结构完整。
 *
 * <p>落地方式：把下方 {@link #POM_XML} 常量全文抽取为测试仓根目录的 pom.xml。
 * 源仓 back@master 使用 Spring Boot 3.5.14 / Java 17，本基线与之对齐。
 *
 * <p>注意：依赖版本号需按团队私服校正后再落地，当前版本为常见组合。
 */
public class MallPortalTestScaffoldSteps {

    /** 内嵌的 Maven 构建基线原文，供仓库 owner 抽取为根目录 pom.xml。 */
    public static final String POM_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>

                <groupId>com.macro.mall</groupId>
                <artifactId>mall-portal-contract-tests</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <packaging>jar</packaging>
                <name>mall-portal-contract-tests</name>
                <description>mall-portal 前台接口契约测试（源：back@master，Spring Boot 3.5.14 / Java 17）</description>

                <properties>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <maven.compiler.release>17</maven.compiler.release>
                    <!-- 版本需按团队私服校正 -->
                    <cucumber.version>7.20.1</cucumber.version>
                    <rest-assured.version>5.5.0</rest-assured.version>
                    <swagger-parser.version>2.1.24</swagger-parser.version>
                    <junit4.version>4.13.2</junit4.version>
                    <surefire.version>3.5.2</surefire.version>
                </properties>

                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.cucumber</groupId>
                            <artifactId>cucumber-bom</artifactId>
                            <version>${cucumber.version}</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>

                <dependencies>
                    <dependency>
                        <groupId>io.cucumber</groupId>
                        <artifactId>cucumber-java</artifactId>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.cucumber</groupId>
                        <artifactId>cucumber-junit-platform-engine</artifactId>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.junit.platform</groupId>
                        <artifactId>junit-platform-suite</artifactId>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter</artifactId>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                        <version>${junit4.version}</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.rest-assured</groupId>
                        <artifactId>rest-assured</artifactId>
                        <version>${rest-assured.version}</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.swagger.parser.v3</groupId>
                        <artifactId>swagger-parser</artifactId>
                        <version>${swagger-parser.version}</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>

                <build>
                    <testSourceDirectory>src/test/java</testSourceDirectory>
                    <testResources>
                        <testResource>
                            <directory>src/test/resources</directory>
                        </testResource>
                    </testResources>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-surefire-plugin</artifactId>
                            <version>${surefire.version}</version>
                            <configuration>
                                <includes>
                                    <include>**/*Test.java</include>
                                    <include>**/*Runner.java</include>
                                </includes>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """;

    private Document pom;

    @Given("内嵌的 pom 基线文本")
    public void embeddedPomText() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        this.pom = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(POM_XML.getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNotNull(this.pom.getDocumentElement(), "pom 基线不是合法 XML");
    }

    @When("将其解析为 Maven 配置")
    public void parsedAsMavenConfig() {
        Assertions.assertEquals("project", pom.getDocumentElement().getNodeName(),
                "pom 根节点应为 project");
    }

    @Then("应使用 Java 17 编译")
    public void compiledWithJava17() {
        Assertions.assertEquals("17", textOfFirst("maven.compiler.release"),
                "pom 基线未声明 Java 17 编译级别");
    }

    @Then("应包含依赖 {string}")
    public void containsDependency(String artifactId) {
        Assertions.assertTrue(allArtifactIds().contains(artifactId),
                "pom 基线缺少依赖: " + artifactId);
    }

    @Then("应包含插件 {string}")
    public void containsPlugin(String artifactId) {
        Assertions.assertTrue(allArtifactIds().contains(artifactId),
                "pom 基线缺少插件: " + artifactId);
    }

    @Then("应同时支持 JUnit4 与 JUnit5 断言")
    public void supportsBothJunit() {
        List<String> ids = allArtifactIds();
        Assertions.assertTrue(ids.contains("junit"), "缺少 JUnit4 依赖（现有 step 在用）");
        Assertions.assertTrue(ids.contains("junit-jupiter"), "缺少 JUnit5 依赖");
    }

    private List<String> allArtifactIds() {
        List<String> ids = new ArrayList<>();
        NodeList nodes = pom.getElementsByTagName("artifactId");
        for (int i = 0; i < nodes.getLength(); i++) {
            ids.add(nodes.item(i).getTextContent().trim());
        }
        return ids;
    }

    private String textOfFirst(String tag) {
        NodeList nodes = pom.getElementsByTagName(tag);
        Assertions.assertTrue(nodes.getLength() > 0, "pom 基线缺少 <" + tag + ">");
        return nodes.item(0).getTextContent().trim();
    }
}
