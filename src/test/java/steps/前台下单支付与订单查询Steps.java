package com.macro.mall.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 前台下单、支付回调与订单查询 step。
 * Background 中的加购、地址、下单 step 会被后台订单 feature 复用。
 */
public class OrderSteps {

    private static final String ADDRESS_NAME = "BDD自动化收货人";

    private static final String ADDRESS_JSON = "{"
            + "\"name\":\"" + ADDRESS_NAME + "\","
            + "\"phoneNumber\":\"13800138000\","
            + "\"defaultStatus\":0,"
            + "\"postCode\":\"518000\","
            + "\"province\":\"广东省\","
            + "\"city\":\"深圳市\","
            + "\"region\":\"南山区\","
            + "\"detailAddress\":\"BDD自动化测试地址\""
            + "}";

    // ==================== 收货地址 ====================

    @When("新增一条默认收货地址并记录其 id")
    public void addAddressAndRecordId() {
        doAddAddress();
    }

    @Given("已新增收货地址并记录其 id")
    public void addAddressAsPrecondition() {
        doAddAddress();
        ApiAssert.codeIs(200);
    }

    private void doAddAddress() {
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Response addResponse = Api.postJson(Cfg.PORTAL_BASE, "/member/address/add", ADDRESS_JSON);
        Ctx.addressId = findLatestAddressId();
        Ctx.lastResponse = addResponse;
    }

    private Long findLatestAddressId() {
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Response response = Api.get(Cfg.PORTAL_BASE, "/member/address/list");
        List<Map<String, Object>> addresses = response.jsonPath().getList("data");
        assertThat(addresses).as("收货地址列表").isNotNull().isNotEmpty();
        return addresses.stream()
                .filter(address -> ADDRESS_NAME.equals(String.valueOf(address.get("name"))))
                .map(address -> Long.valueOf(String.valueOf(address.get("id"))))
                .max(Long::compareTo)
                .orElseThrow(() -> new IllegalStateException("未找到新增的收货地址"));
    }

    private Long ensureAddressId() {
        if (Ctx.addressId == null) {
            doAddAddress();
        }
        return Ctx.addressId;
    }

    // ==================== 确认单与下单 ====================

    @When("生成订单确认单")
    public void generateConfirmOrder() {
        Long cartItemId = Cart.ensureItemId();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Ctx.lastResponse = Api.postJson(Cfg.PORTAL_BASE, "/order/generateConfirmOrder",
                "[" + cartItemId + "]");
    }

    @When("提交订单")
    public void submitOrder() {
        Long cartItemId = Cart.ensureItemId();
        Long addressId = ensureAddressId();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        String body = "{\"memberReceiveAddressId\":" + addressId
                + ",\"couponId\":null"
                + ",\"useIntegration\":0"
                + ",\"payType\":1"
                + ",\"cartIds\":[" + cartItemId + "]}";
        Ctx.lastResponse = Api.postJson(Cfg.PORTAL_BASE, "/order/generateOrder", body);
    }

    @When("提交缺少收货地址的订单")
    public void submitOrderWithoutAddress() {
        Long cartItemId = Cart.ensureItemId();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        String body = "{\"couponId\":null,\"useIntegration\":0,\"payType\":1"
                + ",\"cartIds\":[" + cartItemId + "]}";
        Ctx.lastResponse = Api.postJson(Cfg.PORTAL_BASE, "/order/generateOrder", body);
    }

    @Given("已提交订单并记录订单 id")
    public void submitOrderAsPrecondition() {
        submitOrder();
        ApiAssert.codeIs(200);
        recordOrderId();
    }

    @Then("下单响应中应记录订单 id")
    public void recordOrderIdFromResponse() {
        recordOrderId();
    }

    private void recordOrderId() {
        Long orderId = Ctx.lastResponse.jsonPath().getLong("data.order.id");
        assertThat(orderId).as("订单 id").isNotNull();
        Ctx.orderId = orderId;
    }

    // ==================== 支付与订单查询 ====================

    @When("以 payType {int} 回报订单支付成功")
    public void reportPaySuccess(int payType) {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Ctx.lastResponse = Api.postForm(Cfg.PORTAL_BASE, "/order/paySuccess",
                Map.of("orderId", String.valueOf(Ctx.orderId), "payType", String.valueOf(payType)));
    }

    @When("以状态 {int} 分页查询订单列表")
    public void queryOrderList(int status) {
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Map<String, String> query = new LinkedHashMap<>();
        query.put("status", String.valueOf(status));
        query.put("pageNum", "1");
        query.put("pageSize", "5");
        Ctx.lastResponse = Api.get(Cfg.PORTAL_BASE, "/order/list", query);
    }

    @When("查询当前订单详情")
    public void queryCurrentOrderDetail() {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Ctx.lastResponse = Api.get(Cfg.PORTAL_BASE, "/order/detail/" + Ctx.orderId);
    }

    @When("取消当前订单")
    public void cancelCurrentOrder() {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.MEMBER;
        Ctx.lastResponse = Api.postForm(Cfg.PORTAL_BASE, "/order/cancelUserOrder",
                Map.of("orderId", String.valueOf(Ctx.orderId)));
    }

    @Then("订单列表中应包含已取消的订单")
    public void orderListShouldContainCurrentOrder() {
        List<Map<String, Object>> orders = Ctx.lastResponse.jsonPath().getList("data.list");
        assertThat(orders).as("订单列表").isNotNull();
        boolean found = orders.stream().anyMatch(order ->
                String.valueOf(Ctx.orderId).equals(String.valueOf(order.get("id"))));
        assertThat(found).as("已取消订单 " + Ctx.orderId + " 应出现在状态 4 的列表中").isTrue();
    }
}
