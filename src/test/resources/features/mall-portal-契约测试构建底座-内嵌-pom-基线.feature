Feature: mall-portal 契约测试构建底座（内嵌 pom 基线）

  测试仓根目录当前没有 pom.xml / build.gradle，无法执行 mvn test。
  本 feature 以内嵌文本方式固化一份可用的 Maven 构建基线（源仓 back@master，
  Spring Boot 3.5.14 / Java 17），供仓库 owner 抽取落盘为根目录 pom.xml。

  说明：pom 全文以内嵌常量 MallPortalTestScaffoldSteps.POM_XML 形式随用例维护，
  便于评审与 diff；依赖版本需按团队私服校正后再落地。

  Scenario: 内嵌 pom 基线应为合法且使用 Java 17
    Given 内嵌的 pom 基线文本
    When 将其解析为 Maven 配置
    Then 应使用 Java 17 编译

  Scenario Outline: 内嵌 pom 基线应包含契约测试所需依赖
    Given 内嵌的 pom 基线文本
    Then 应包含依赖 "<artifactId>"

    Examples:
      | artifactId                     |
      | cucumber-java                  |
      | cucumber-junit-platform-engine |
      | junit-platform-suite           |
      | junit-jupiter                  |
      | junit                          |
      | rest-assured                   |
      | swagger-parser                 |

  Scenario: 内嵌 pom 基线应包含构建插件并兼容双断言框架
    Given 内嵌的 pom 基线文本
    Then 应包含插件 "maven-surefire-plugin"
    And 应同时支持 JUnit4 与 JUnit5 断言