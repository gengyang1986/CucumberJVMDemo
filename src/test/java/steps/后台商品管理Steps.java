package com.macro.mall.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台商品管理 step。
 * 注意：上下架用例会真实修改商品 publishStatus，用例内已做还原，执行时请避免与人工操作并发。
 */
public class ProductAdminSteps {

    @Given("记录后台商品 {long} 的当前上架状态")
    public void recordCurrentPublishStatus(long productId) {
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Response response = Api.get(Cfg.ADMIN_BASE, "/product/updateInfo/" + productId);
        ApiAssert.codeIs(200);
        Integer publishStatus = response.jsonPath().getInt("data.publishStatus");
        assertThat(publishStatus).as("商品 " + productId + " 的上架状态").isNotNull();
        Ctx.originalPublishStatus = publishStatus;
    }

    @When("将后台商品 {long} 的上架状态置为 {string}")
    public void setPublishStatus(long productId, String publishStatus) {
        updatePublishStatus(productId, publishStatus);
    }

    @When("将后台商品 {long} 的上架状态还原")
    public void restorePublishStatus(long productId) {
        assertThat(Ctx.originalPublishStatus).as("已记录的原上架状态").isNotNull();
        updatePublishStatus(productId, String.valueOf(Ctx.originalPublishStatus));
    }

    @Then("后台商品 {long} 的上架状态应为 {string}")
    public void publishStatusShouldBe(long productId, String expected) {
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Ctx.lastResponse = Api.get(Cfg.ADMIN_BASE, "/product/updateInfo/" + productId);
        ApiAssert.codeIs(200);
        assertThat(Ctx.lastResponse.jsonPath().getString("data.publishStatus"))
                .as("商品 " + productId + " 的上架状态").isEqualTo(expected);
    }

    private void updatePublishStatus(long productId, String publishStatus) {
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Ctx.lastResponse = Api.postForm(Cfg.ADMIN_BASE, "/product/update/publishStatus",
                Map.of("ids", String.valueOf(productId), "publishStatus", publishStatus));
    }
}
