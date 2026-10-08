package com.macro.mall.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 前台购物车管理 step。
 * 同时提供 Cart 工具类，供下单（F5）与后台订单（F7）场景复用：造购物车数据。
 */
public class CartSteps {

    @Given("已清空当前会员购物车")
    public void clearCartQuietly() {
        Cart.clearQuietly();
    }

    @Given("已添加商品 {long} 规格 {long} 数量 {int} 到购物车")
    public void addProductToCartAsPrecondition(long productId, long skuId, int quantity) {
        Cart.add(productId, skuId, quantity);
    }

    @When("添加商品 {long} 规格 {long} 数量 {int} 到购物车")
    public void addProductToCart(long productId, long skuId, int quantity) {
        Cart.add(productId, skuId, quantity);
    }

    @When("将购物车条目数量修改为 {int}")
    public void updateCartItemQuantity(int quantity) {
        Long itemId = Cart.ensureItemId();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Ctx.lastResponse = Api.get(Cfg.PORTAL_BASE, "/cart/update/quantity",
                Map.of("id", String.valueOf(itemId), "quantity", String.valueOf(quantity)));
    }

    @When("将购物车条目的规格改为当前商品的 {long} 号规格")
    public void updateCartItemAttr(long skuId) {
        Long itemId = Cart.ensureItemId();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Ctx.lastResponse = Api.postJson(Cfg.PORTAL_BASE, "/cart/update/attr",
                Cart.itemJson(Cfg.PRODUCT_ID, skuId, 1, itemId));
    }

    @When("删除购物车中已记录的商品条目")
    public void deleteRecordedCartItem() {
        Long itemId = Cart.ensureItemId();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Ctx.lastResponse = Api.postForm(Cfg.PORTAL_BASE, "/cart/delete",
                Map.of("ids", String.valueOf(itemId)));
    }

    @Then("购物车列表中应包含商品 {long} 并记录其条目 id")
    public void cartListShouldContainProduct(long productId) {
        List<Map<String, Object>> items = Ctx.lastResponse.jsonPath().getList("data");
        assertThat(items).as("购物车列表").isNotNull().isNotEmpty();
        Map<String, Object> target = items.stream()
                .filter(item -> String.valueOf(productId).equals(String.valueOf(item.get("productId"))))
                .findFirst()
                .orElse(null);
        assertThat(target).as("购物车中应包含商品 " + productId).isNotNull();
        Cart.itemId = Long.valueOf(String.valueOf(target.get("id")));
    }

    @Then("响应体 {string} 应为空数组")
    public void fieldShouldBeEmptyArray(String path) {
        Object value = Ctx.lastResponse.jsonPath().get(path);
        assertThat(value).as("字段 " + path).isInstanceOf(List.class);
        assertThat((List<?>) value).as("字段 " + path).isEmpty();
    }
}

/**
 * 购物车操作工具：F4 / F5 / F7 共用。
 */
final class Cart {

    static Long itemId;

    static void clearQuietly() {
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Api.postWithoutParam(Cfg.PORTAL_BASE, "/cart/clear");
    }

    static void add(long productId, long skuId, int quantity) {
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Ctx.lastResponse = Api.postJson(Cfg.PORTAL_BASE, "/cart/add",
                itemJson(productId, skuId, quantity, null));
        ApiAssert.codeIs(200);
    }

    /**
     * 返回当前会员购物车中第一条条目的 id，必要时先查询一次列表。
     */
    static Long ensureItemId() {
        if (itemId != null) {
            return itemId;
        }
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Response response = Api.get(Cfg.PORTAL_BASE, "/cart/list");
        List<Map<String, Object>> items = response.jsonPath().getList("data");
        assertThat(items).as("购物车列表").isNotNull().isNotEmpty();
        itemId = Long.valueOf(String.valueOf(items.get(0).get("id")));
        return itemId;
    }

    static String itemJson(long productId, long skuId, int quantity, Long cartItemId) {
        StringBuilder sb = new StringBuilder("{");
        if (cartItemId != null) {
            sb.append("\"id\":").append(cartItemId).append(',');
        }
        sb.append("\"productId\":").append(productId)
                .append(",\"productSkuId\":").append(skuId)
                .append(",\"productName\":\"BDD自动化测试商品\"")
                .append(",\"productPic\":\"\"")
                .append(",\"productSubTitle\":\"BDD\"")
                .append(",\"productPrice\":100.00")
                .append(",\"productQuantity\":").append(quantity)
                .append(",\"productAttr\":\"[]\"")
                .append(",\"productCategoryId\":1")
                .append('}');
        return sb.toString();
    }

    private Cart() {
    }
}
