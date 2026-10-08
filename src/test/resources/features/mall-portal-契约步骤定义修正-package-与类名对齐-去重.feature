# language: zh-CN
功能: mall-portal 契约步骤定义位置与命名对齐校验
  修正原步骤实现的编译与运行隐患：package 声明与所在目录不一致、公开类名与文件名不一致、
  以及 @而且/@那么 重复注册同一表达式。以下场景不依赖被测服务，可在 CI 直接绿灯。

  场景: 契约步骤类与其 package 声明一致
    那么 契约步骤类的包名应为 "com.macro.mall.portal.contract.steps"
    而且 契约步骤类的简单名应为 "MallPortalApiContractSteps"

  场景: 旧的两处错位步骤文件已移除
    那么 文件 "src/test/java/steps/MallPortalApiContractSteps.java" 应不存在
    而且 文件 "src/test/java/steps/MallPortalApi契约校验契约由BackMaster的MallPortal模块源码生成Steps.java" 应不存在

  场景: OpenAPI 契约文件落地后可被解析
    那么 OpenAPI 契约文件 "src/test/resources/openapi/mall-portal.yaml" 可被解析为 openapi "3.0.3"