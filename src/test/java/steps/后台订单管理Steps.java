package com.macro.mall.bdd.steps;

import io.cucumber.java.en.When;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台订单管理 step。
 * Background 复用 OrderSteps / CartSteps / MemberAuthSteps / AdminAuthSteps 中的 step 造单。
 */
public class OrderAdminSteps {

    @When("后台查询当前订单的详情")
    public void adminQueryCurrentOrderDetail() {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Ctx.lastResponse = Api.get(Cfg.ADMIN_BASE, "/order/" + Ctx.orderId);
    }

    @When("后台为当前订单添加备注 {string}")
    public void adminUpdateOrderNote(String note) {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Ctx.lastResponse = Api.postForm(Cfg.ADMIN_BASE, "/order/update/note",
                Map.of("id", String.valueOf(Ctx.orderId), "note", note, "status", "1"));
    }

    @When("后台关闭当前订单 备注为 {string}")
    public void adminCloseCurrentOrder(String note) {
        assertThat(Ctx.orderId).as("订单 id").isNotNull();
        Ctx.tokenMode = Ctx.TokenMode.ADMIN;
        Ctx.lastResponse = Api.postForm(Cfg.ADMIN_BASE, "/order/update/close",
                Map.of("ids", String.valueOf(Ctx.orderId), "note", note));
    }
}
