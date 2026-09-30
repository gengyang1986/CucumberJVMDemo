package com.macro.mall.bdd.steps;

import io.cucumber.java.en.When;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台订单管理 step。
 *
 * 文件位置约定：src/test/java/com/macro/mall/bdd/steps/OrderAdminSteps.java
 * （本分支上同名文件被放在 src/test/java/steps/ 下，但其 package 声明为 com.macro.mall.bdd.steps，
 *   目录与包名不一致，建议按包路径归位，避免 Cucumber glue / IDE 识别不一致）
 *
 * Background 复用 OrderSteps / CartSteps / MemberAuthSteps / AdminAuthSteps 中的 step 造单。
 * 依赖脚手架（由主干提供）：Ctx（上下文）、Api（HTTP 封装）、Cfg（服务地址配置）。
 */
public class OrderAdminSteps {

    /**
     * 前台（mall-portal）服务地址。
     * 脚手架若已提供 Cfg.PORTAL_BASE，请直接改用之；在脚手架合并前可用 -Dportal.base=... 覆盖默认值。
     */
    private static String portalBase() {
        String base = System.getProperty("portal.base");
        return (base != null && !base.isEmpty()) ? base : "http://localhost:8085";
    }

    @When("后台查询当前订单的详情")
    public void adminQueryCurrentOrderDetail() {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Ctx.lastResponse = Api.get(Cfg.ADMIN_BASE, "/order/" + Ctx.orderId);
    }

    /**
     * POST /order/update/note：@RequestParam id + note + status。
     * status 只是随备注写入操作记录（OmsOrderOperateHistory.orderStatus），不会改变订单状态，
     * 这里传 1（待发货）用于留痕。
     */
    @When("后台为当前订单添加备注 {string}")
    public void adminUpdateOrderNote(String note) {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Ctx.lastResponse = Api.postForm(Cfg.ADMIN_BASE, "/order/update/note",
                Map.of("id", String.valueOf(Ctx.orderId), "note", note, "status", "1"));
    }

    /** POST /order/update/close：@RequestParam ids（可多值）+ note，关闭后订单状态置 4。 */
    @When("后台关闭当前订单 备注为 {string}")
    public void adminCloseCurrentOrder(String note) {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Ctx.lastResponse = Api.postForm(Cfg.ADMIN_BASE, "/order/update/close",
                Map.of("ids", String.valueOf(Ctx.orderId), "note", note));
    }

    /**
     * 前台支付成功回调：POST /order/paySuccess?orderId={id}&payType={type}（两者均为 @RequestParam）。
     * 支付成功后订单状态由 0（待付款）变为 1（待发货），后台发货接口才可生效。
     * 调用前需由共享 step「请求头携带有效的会员 token」切换到会员身份。
     */
    @When("会员支付当前订单")
    public void memberPayCurrentOrder() {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.lastResponse = Api.postForm(portalBase(), "/order/paySuccess",
                Map.of("orderId", String.valueOf(Ctx.orderId), "payType", "2"));
    }

    /**
     * POST /order/update/delivery：@RequestBody List&lt;OmsOrderDeliveryParam&gt;，
     * 每个元素只有 orderId / deliveryCompany / deliverySn（不含收货信息）。
     * 该接口的 SQL 只在订单为待发货态（status=1）时命中，故前置必须已支付。
     */
    @When("后台批量发货当前订单 物流公司 {string} 物流单号 {string}")
    public void adminDeliveryCurrentOrder(String deliveryCompany, String deliverySn) {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        String payload = "[{\"orderId\":" + Ctx.orderId
                + ",\"deliveryCompany\":\"" + deliveryCompany
                + "\",\"deliverySn\":\"" + deliverySn + "\"}]";
        // TODO(脚手架对齐)：JSON POST 的方法名请与 scaffold 统一（如 post / postBody / postJson，入参为 JSON 字符串）
        Ctx.lastResponse = Api.postJson(Cfg.ADMIN_BASE, "/order/update/delivery", payload);
    }

    /*
     * 待脚手架补齐后可加强的断言（当前公共 step 仅提供“字段存在 / 等于”，无法做集合包含断言）：
     * 1) 列表筛选正确性：取 data.list[].id，断言包含 Ctx.orderId；
     * 2) 发货留痕：取 data.historyList[].note，断言包含“完成发货”。
     * 建议在脚手架中补充「保存响应体 {jsonPath} 到变量 {name}」这类 step，
     * 再在 step 层用 AssertJ 做集合断言。
     */
}
