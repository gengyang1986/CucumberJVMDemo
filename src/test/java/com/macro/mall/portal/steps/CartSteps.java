package com.macro.mall.portal.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 前台购物车管理（mall-portal /cart/**）步骤实现。
 *
 * <p>接口与字段契约来自 back 仓 mall-portal 模块：
 * OmsCartItemController、OmsCartItemServiceImpl、UmsMemberController。
 *
 * <p>本类自包含：仅通过环境变量读取配置，不依赖其他 step 类的支撑类。
 * 需要环境变量：PORTAL_BASE_URL（或 BASE_URL）——由接口测试环境 app-portal 提供。
 * 会员账号由场景内随机注册，不依赖环境中的账号密码。
 */
public class CartSteps {

    private static final String BUSINESS_NAME = "BDD购物车测试商品";

    private String baseUrl;

    /** 当前会员（购物车归属方）token */
    private String token;
    /** 另一会员 token，用于越权场景 */
    private String otherToken;

    private Long recordedItemId;
    private Long recordedProductId;
    private int recordedQuantity;
    private Long cachedSkuId;
    private Response response;

    @Before
    public void resolveBaseUrlBeforeScenario() {
        baseUrl = resolveBaseUrl();
        token = null;
        otherToken = null;
        recordedItemId = null;
        recordedProductId = null;
        recordedQuantity = 0;
        cachedSkuId = null;
        response = null;
    }

    // ==================== 基础工具 ====================

    private String resolveBaseUrl() {
        String url = System.getenv("PORTAL_BASE_URL");
        if (StringUtils.isBlank(url)) {
            url = System.getenv("BASE_URL");
        }
        if (StringUtils.isBlank(url)) {
            url = System.getProperty("portal.baseUrl");
        }
        assertThat(url).as("未获取到前台商城端 baseUrl：请在接口测试环境 app-portal 中配置，"
                + "或以环境变量 PORTAL_BASE_URL / BASE_URL 注入").isNotBlank();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private RequestSpecification json(String bearer) {
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUrl)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON);
        if (StringUtils.isNotBlank(bearer)) {
            spec.header("Authorization", bearer);
        }
        return spec;
    }

    private RequestSpecification form(String bearer) {
        RequestSpecification spec = RestAssured.given()
                .baseUri(baseUrl)
                .accept(ContentType.JSON)
                .contentType(ContentType.URLENC);
        if (StringUtils.isNotBlank(bearer)) {
            spec.header("Authorization", bearer);
        }
        return spec;
    }

    private String randomTelephone() {
        return "13" + String.format("%09d", ThreadLocalRandom.current().nextLong(1_000_000_000L));
    }

    private String extractToken(Response loginResponse) {
        Object data = loginResponse.jsonPath().get("data");
        String memberToken;
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            Object value = dataMap.get("token") != null ? dataMap.get("token") : dataMap.get("accessToken");
            assertThat(value).as("登录响应 data 中缺少 token：%s", loginResponse.asString()).isNotNull();
            memberToken = String.valueOf(value);
        } else {
            assertThat(data).as("登录响应 data 为空：%s", loginResponse.asString()).isNotNull();
            memberToken = String.valueOf(data);
        }
        return memberToken;
    }

    /**
     * 随机注册并登录一个会员，返回 [token, memberId]。
     * register/login 均为 @RequestParam 形式参数，因此使用 form-urlencoded 提交。
     */
    private String[] createMemberSession() {
        String telephone = randomTelephone();
        String username = "bddcart" + System.nanoTime();
        String password = "Bdd@" + ThreadLocalRandom.current().nextInt(100000, 1000000);

        Response authCodeResponse = json(null).get("/sso/getAuthCode?telephone={telephone}", telephone);
        assertThat(authCodeResponse.jsonPath().getInt("code"))
                .as("获取验证码响应：%s", authCodeResponse.asString()).isEqualTo(200);
        String authCode = authCodeResponse.jsonPath().getString("data");
        assertThat(authCode).as("验证码为空，无法完成会员注册：%s", authCodeResponse.asString()).isNotBlank();

        Response registerResponse = form(null)
                .formParam("username", username)
                .formParam("password", password)
                .formParam("telephone", telephone)
                .formParam("authCode", authCode)
                .post("/sso/register");
        assertThat(registerResponse.jsonPath().getInt("code"))
                .as("会员注册响应：%s", registerResponse.asString()).isEqualTo(200);

        Response loginResponse = form(null)
                .formParam("username", username)
                .formParam("password", password)
                .post("/sso/login");
        assertThat(loginResponse.jsonPath().getInt("code"))
                .as("会员登录响应：%s", loginResponse.asString()).isEqualTo(200);
        String memberToken = extractToken(loginResponse);

        Response infoResponse = json(memberToken).get("/member/info");
        assertThat(infoResponse.jsonPath().getInt("code"))
                .as("查询当前会员信息响应：%s", infoResponse.asString()).isEqualTo(200);
        Long memberId = infoResponse.jsonPath().getLong("data.id");
        assertThat(memberId).as("当前会员 id 为空：%s", infoResponse.asString()).isNotNull();
        return new String[]{memberToken, String.valueOf(memberId)};
    }

    /** 解析商品可用规格 id，供加购使用；商品无规格时返回 null */
    private void prepareSku(long productId) {
        if (cachedSkuId != null) {
            return;
        }
        Response productResponse = json(token).get("/cart/getProduct/{productId}", productId);
        if (productResponse.jsonPath().getInt("code") != 200) {
            return;
        }
        Object data = productResponse.jsonPath().get("data");
        if (!(data instanceof Map)) {
            return;
        }
        Map<?, ?> dataMap = (Map<?, ?>) data;
        Object attributeList = dataMap.get("productAttributeList");
        if (attributeList instanceof List && !((List<?>) attributeList).isEmpty()) {
            Object first = ((List<?>) attributeList).get(0);
            if (first instanceof Map && ((Map<?, ?>) first).get("skuId") != null) {
                cachedSkuId = Long.valueOf(String.valueOf(((Map<?, ?>) first).get("skuId")));
                return;
            }
        }
        Object skuStockList = dataMap.get("productSkuStockList");
        if (skuStockList instanceof List && !((List<?>) skuStockList).isEmpty()) {
            Object first = ((List<?>) skuStockList).get(0);
            if (first instanceof Map && ((Map<?, ?>) first).get("id") != null) {
                cachedSkuId = Long.valueOf(String.valueOf(((Map<?, ?>) first).get("id")));
            }
        }
    }

    private Map<String, Object> cartItemBody(long productId, int quantity) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("productId", productId);
        if (cachedSkuId != null) {
            body.put("productSkuId", cachedSkuId);
        }
        body.put("productName", BUSINESS_NAME);
        body.put("productPic", "https://example.com/bdd-cart.png");
        body.put("productSubTitle", "BDD自动化");
        body.put("productPrice", 100);
        body.put("productAttr", "[]");
        body.put("quantity", quantity);
        return body;
    }

    private Response addProduct(String bearer, long productId, int quantity, Long memberIdOverride) {
        Map<String, Object> body = cartItemBody(productId, quantity);
        if (memberIdOverride != null) {
            body.put("memberId", memberIdOverride);
        }
        return json(bearer).body(body).post("/cart/add");
    }

    private Response currentMemberCart() {
        return json(token).get("/cart/list");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> cartItems(Response listResponse) {
        assertThat(listResponse.jsonPath().getInt("code"))
                .as("购物车列表响应：%s", listResponse.asString()).isEqualTo(200);
        List<Map<String, Object>> items = listResponse.jsonPath().getList("data");
        return items == null ? new ArrayList<>() : items;
    }

    private Map<String, Object> findItemById(List<Map<String, Object>> items, Long itemId) {
        if (itemId == null) {
            return null;
        }
        for (Map<String, Object> item : items) {
            if (String.valueOf(itemId).equals(String.valueOf(item.get("id")))) {
                return item;
            }
        }
        return null;
    }

    private Map<String, Object> findItemByProduct(List<Map<String, Object>> items, Long productId) {
        for (Map<String, Object> item : items) {
            if (String.valueOf(productId).equals(String.valueOf(item.get("productId")))) {
                return item;
            }
        }
        return null;
    }

    private void recordItem(Map<String, Object> item) {
        recordedItemId = Long.valueOf(String.valueOf(item.get("id")));
        recordedProductId = Long.valueOf(String.valueOf(item.get("productId")));
        recordedQuantity = Integer.parseInt(String.valueOf(item.get("quantity")));
    }

    // ==================== Background ====================

    @Given("前台商城端服务地址已配置")
    public void portalServiceConfigured() {
        assertThat(baseUrl).as("前台商城端 baseUrl 未配置").isNotBlank();
    }

    @Given("已注册并登录一个随机会员账号")
    public void registerAndLoginCurrentMember() {
        String[] session = createMemberSession();
        token = session[0];
    }

    @Given("另有一个已注册并登录的随机会员账号")
    public void registerAndLoginAnotherMember() {
        String[] session = createMemberSession();
        otherToken = session[0];
    }

    // ==================== When ====================

    @When("将商品 {long} 以数量 {int} 加入购物车")
    public void addProductToCart(long productId, int quantity) {
        recordedProductId = productId;
        prepareSku(productId);
        response = addProduct(token, productId, quantity, null);
    }

    @When("再次将商品 {long} 以数量 {int} 加入购物车")
    public void addSameProductAgain(long productId, int quantity) {
        response = addProduct(token, productId, quantity, null);
    }

    @When("以请求体 memberId {long} 将商品 {long} 以数量 {int} 加入购物车")
    public void addProductWithMemberId(long memberIdOverride, long productId, int quantity) {
        recordedProductId = productId;
        prepareSku(productId);
        response = addProduct(token, productId, quantity, memberIdOverride);
    }

    @When("查询当前会员购物车列表")
    public void listCurrentMemberCart() {
        response = currentMemberCart();
    }

    @When("切换到另一会员并查询购物车列表")
    public void listAnotherMemberCart() {
        assertThat(otherToken).as("尚未准备另一会员的 token").isNotBlank();
        response = json(otherToken).get("/cart/list");
    }

    @When("不携带 token 查询购物车列表")
    public void listCartWithoutToken() {
        response = json(null).get("/cart/list");
    }

    @When("查询购物车促销信息")
    public void listCartPromotion() {
        response = json(token).get("/cart/list/promotion");
    }

    @When("按已记录条目查询购物车促销信息")
    public void listCartPromotionByRecordedItem() {
        assertThat(recordedItemId).as("尚未记录购物车条目 id").isNotNull();
        response = json(token).queryParam("cartIds", recordedItemId).get("/cart/list/promotion");
    }

    @When("获取商品 {long} 的购物车规格信息")
    public void getCartProduct(long productId) {
        recordedProductId = productId;
        response = json(token).get("/cart/getProduct/{productId}", productId);
        prepareSku(productId);
    }

    @When("将已记录条目数量修改为 {int}")
    public void updateRecordedItemQuantity(int quantity) {
        assertThat(recordedItemId).as("尚未记录购物车条目 id").isNotNull();
        response = json(token)
                .queryParam("id", recordedItemId)
                .queryParam("quantity", quantity)
                .get("/cart/update/quantity");
    }

    @When("使用另一会员的 token 将已记录条目数量修改为 {int}")
    public void updateRecordedItemQuantityAsAnotherMember(int quantity) {
        assertThat(recordedItemId).as("尚未记录购物车条目 id").isNotNull();
        assertThat(otherToken).as("尚未准备另一会员的 token").isNotBlank();
        response = json(otherToken)
                .queryParam("id", recordedItemId)
                .queryParam("quantity", quantity)
                .get("/cart/update/quantity");
    }

    @When("使用另一会员的 token 删除已记录条目")
    public void deleteRecordedItemAsAnotherMember() {
        assertThat(recordedItemId).as("尚未记录购物车条目 id").isNotNull();
        assertThat(otherToken).as("尚未准备另一会员的 token").isNotBlank();
        response = json(otherToken).queryParam("ids", recordedItemId).post("/cart/delete");
    }

    @When("将已记录条目的规格更新为当前商品规格")
    public void updateRecordedItemAttr() {
        assertThat(recordedItemId).as("尚未记录购物车条目 id").isNotNull();
        Map<String, Object> body = cartItemBody(recordedProductId, recordedQuantity);
        body.put("id", recordedItemId);
        response = json(token).body(body).post("/cart/update/attr");
    }

    @When("删除已记录的购物车条目")
    public void deleteRecordedItem() {
        assertThat(recordedItemId).as("尚未记录购物车条目 id").isNotNull();
        response = json(token).queryParam("ids", recordedItemId).post("/cart/delete");
    }

    @When("清空当前会员购物车")
    public void clearCurrentMemberCart() {
        response = json(token).post("/cart/clear");
    }

    // ==================== Then ====================

    @Then("响应业务码应为 {int}")
    public void responseCodeShouldBe(int expectedCode) {
        assertThat(response.jsonPath().getInt("code"))
                .as("响应：%s", response.asString()).isEqualTo(expectedCode);
    }

    @Then("购物车列表中应包含商品 {long} 并记录其条目")
    public void cartShouldContainProduct(long productId) {
        recordedProductId = productId;
        Map<String, Object> item = findItemByProduct(cartItems(currentMemberCart()), productId);
        assertThat(item).as("购物车列表中未找到商品 %s 的条目", productId).isNotNull();
        recordItem(item);
    }

    @Then("购物车列表中不应包含商品 {long}")
    public void cartShouldNotContainProduct(long productId) {
        Map<String, Object> item = findItemByProduct(cartItems(currentMemberCart()), productId);
        assertThat(item).as("购物车列表中不应出现商品 %s，实际条目：%s", productId, item).isNull();
    }

    @Then("购物车列表中该商品数量应为 {int}")
    public void cartProductQuantityShouldBe(int expectedQuantity) {
        Map<String, Object> item = findItemByProduct(cartItems(currentMemberCart()), recordedProductId);
        assertThat(item).as("购物车列表中未找到商品 %s 的条目", recordedProductId).isNotNull();
        assertThat(Integer.parseInt(String.valueOf(item.get("quantity"))))
                .as("商品 %s 在购物车中的数量", recordedProductId).isEqualTo(expectedQuantity);
        recordItem(item);
    }

    @Then("购物车列表中该商品的 memberId 应不等于 {long}")
    public void cartProductMemberIdShouldNotBe(long unexpectedMemberId) {
        Map<String, Object> item = findItemByProduct(cartItems(currentMemberCart()), recordedProductId);
        assertThat(item).as("购物车列表中未找到商品 %s 的条目", recordedProductId).isNotNull();
        assertThat(String.valueOf(item.get("memberId")))
                .as("加购时 memberId 应由服务端按当前会员覆盖，而不是使用请求体中的值")
                .isNotEqualTo(String.valueOf(unexpectedMemberId));
    }

    @Then("购物车列表中条目数应为 {int}")
    public void cartItemCountShouldBe(int expectedCount) {
        assertThat(cartItems(currentMemberCart())).as("当前会员购物车条目数").hasSize(expectedCount);
    }

    @Then("促销列表中应包含商品 {long}")
    public void promotionShouldContainProduct(long productId) {
        assertThat(response.jsonPath().getInt("code"))
                .as("响应：%s", response.asString()).isEqualTo(200);
        List<Map<String, Object>> items = response.jsonPath().getList("data");
        assertThat(items).as("促销信息列表").isNotNull();
        boolean matched = items.stream()
                .anyMatch(item -> String.valueOf(productId).equals(String.valueOf(item.get("productId"))));
        assertThat(matched).as("促销列表中应包含商品 %s，实际：%s", productId, items).isTrue();
    }

    @Then("促销列表中条目数应为 {int}")
    public void promotionItemCountShouldBe(int expectedCount) {
        List<Map<String, Object>> items = response.jsonPath().getList("data");
        assertThat(items).as("促销信息列表").isNotNull().hasSize(expectedCount);
    }

    @Then("响应中的商品 id 应为 {long}")
    public void responseProductIdShouldBe(long productId) {
        assertThat(response.jsonPath().getInt("code"))
                .as("响应：%s", response.asString()).isEqualTo(200);
        assertThat(response.jsonPath().getLong("data.productId"))
                .as("响应中的商品 id").isEqualTo(productId);
    }

    @Then("已记录条目的数量应保持为 {int}")
    public void recordedItemQuantityShouldRemain(int expectedQuantity) {
        Map<String, Object> item = findItemById(cartItems(currentMemberCart()), recordedItemId);
        assertThat(item).as("当前会员的购物车条目 %s 应仍然存在", recordedItemId).isNotNull();
        assertThat(Integer.parseInt(String.valueOf(item.get("quantity"))))
                .as("条目 %s 的数量不应被他人修改", recordedItemId).isEqualTo(expectedQuantity);
    }

    @Then("已记录条目应仍存在于购物车")
    public void recordedItemShouldStillExist() {
        Map<String, Object> item = findItemById(cartItems(currentMemberCart()), recordedItemId);
        assertThat(item).as("当前会员的购物车条目 %s 不应被他人删除", recordedItemId).isNotNull();
    }

    @Then("当前实现返回业务码 {int} 且消息包含 {string}")
    public void currentImplementationReturns(int expectedCode, String expectedMessage) {
        assertThat(response.jsonPath().getInt("code"))
                .as("响应：%s", response.asString()).isEqualTo(expectedCode);
        assertThat(response.jsonPath().getString("message"))
                .as("响应消息：%s", response.asString()).contains(expectedMessage);
    }

    @Then("应返回未认证或无权访问")
    public void shouldBeRejectedAsUnauthenticated() {
        int statusCode = response.statusCode();
        boolean rejectedByStatus = statusCode == 401 || statusCode == 403;
        Integer bodyCode = null;
        try {
            bodyCode = response.jsonPath().getInt("code");
        } catch (RuntimeException ignored) {
            // 响应体非 JSON 时只按 HTTP 状态判定
        }
        boolean rejectedByBody = bodyCode != null && (bodyCode == 401 || bodyCode == 403);
        assertThat(rejectedByStatus || rejectedByBody)
                .as("未携带 token 访问 /cart/list 应被拒绝，实际 HTTP %s，响应体：%s", statusCode, response.asString())
                .isTrue();
    }
}
