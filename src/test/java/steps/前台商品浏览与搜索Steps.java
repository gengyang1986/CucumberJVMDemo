package com.macro.mall.bdd.steps;

import io.cucumber.java.en.Then;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 前台商品浏览与搜索 step。
 * 复用 AdminAuthSteps 中的通用请求 / 断言 step。
 */
public class ProductPortalSteps {

    @Then("商品分类树中每个顶级分类都应包含 children 字段")
    public void everyTopCategoryShouldHaveChildren() {
        List<Map<String, Object>> tree = Ctx.lastResponse.jsonPath().getList("data");
        assertThat(tree).as("商品分类树").isNotNull().isNotEmpty();
        for (Map<String, Object> node : tree) {
            assertThat(node).as("分类节点").containsKey("children");
            assertThat(node.get("parentId")).as("顶级分类 parentId").isNotNull();
        }
    }
}
