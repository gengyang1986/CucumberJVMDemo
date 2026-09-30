package com.macro.mall.bdd.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台管理-商品管理（对应 admin_product.feature）
 *
 * 说明：
 * 1. POST /product/create 返回的是受影响行数，不含商品 id，因此创建后通过 GET /product/list?productSn=xx 反查 id；
 * 2. 分类/品牌 id 必须由测试环境提供，默认 0 会在步骤中直接断言失败并给出配置提示；
 * 3. 创建的测试商品名称带 "BDD自动化" 前缀，便于环境清理。
 */
public class AdminProductSteps {

    private static final String BASE_URL = System.getProperty("api.baseUrl",
            System.getenv().getOrDefault("API_BASE_URL", "http://localhost:88888"));
    private static final String ADMIN_USERNAME = System.getProperty("admin.username", "admin");
    private static final String ADMIN_PASSWORD = System.getProperty("admin.password", "123456");
    private static final long PRODUCT_CATEGORY_ID = Long.parseLong(System.getProperty("test.product.categoryId", "0"));
    private static final long PRODUCT_BRAND_ID = Long.parseLong(System.getProperty("test.product.brandId", "0"));

    private Response response;
    private String adminToken;
    private String createdProductSn;
    private Long createdProductId;

    @Before(order = 10)
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    private void loginAsAdmin(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/admin/login");
        adminToken = loginResponse.jsonPath().getString("data.token");
        assertThat(adminToken)
                .as("管理员登录失败，请检查后台账号数据与 admin.username/admin.password 配置")
                .isNotBlank();
    }

    private Map<String, Object> productBody(String name, String productSn) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("productSn", productSn);
        body.put("price", 199.00);
        body.put("originalPrice", 299.00);
        body.put("stock", 100);
        body.put("unit", "件");
        body.put("weight", 500);
        body.put("sort", 0);
        body.put("publishStatus", 1);
        body.put("verifyStatus", 0);
        body.put("productCategoryId", PRODUCT_CATEGORY_ID);
        body.put("brandId", PRODUCT_BRAND_ID);
        body.put("description", "由 BDD 自动化用例创建，可安全删除");
        return body;
    }

    @Given("后台管理员 {string} 已使用密码 {string} 登录后台管理服务")
    public void adminHasLoggedIn(String username, String password) {
        loginAsAdmin(username, password);
    }

    @When("后台创建一件货号为自动生成、名称为 {string} 的商品")
    public void createProduct(String name) {
        assertThat(PRODUCT_CATEGORY_ID)
                .as("请通过 -Dtest.product.categoryId 指定测试环境已存在的商品分类 id")
                .isNotZero();
        createdProductSn = "BDD-AUTO-" + System.currentTimeMillis();
        response = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(productBody(name, createdProductSn))
                .when()
                .post("/product/create");
    }

    @When("后台以刚创建的货号作为筛选条件分页查询商品")
    public void queryProductByCreatedSn() {
        response = given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("pageNum", 1)
                .queryParam("pageSize", 10)
                .queryParam("productSn", createdProductSn)
                .when()
                .get("/product/list");
        List<Map<String, Object>> list = response.jsonPath().getList("data.list");
        if (list != null && !list.isEmpty()) {
            Object id = list.get(0).get("id");
            createdProductId = id == null ? null : Long.valueOf(String.valueOf(id));
        }
    }

    @When("后台以页码 {int}、每页 {int} 条查询商品列表")
    public void queryProductByPage(int pageNum, int pageSize) {
        response = given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("pageNum", pageNum)
                .queryParam("pageSize", pageSize)
                .when()
                .get("/product/list");
    }

    @Given("后台已创建一件自动生成货号、名称为 {string} 的商品")
    public void createProductReadyToUse(String name) {
        createProduct(name);
        assertThat(response.jsonPath().getInt("code")).as("前置创建商品失败").isEqualTo(200);
        queryProductByCreatedSn();
        assertThat(createdProductId)
                .as("创建商品后未能按货号 %s 反查到商品 id，请检查商品查询条件与环境数据", createdProductSn)
                .isNotNull();
    }

    @When("后台根据刚创建的商品 id 查询商品编辑信息")
    public void queryProductUpdateInfo() {
        response = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/product/updateInfo/{id}", createdProductId);
    }

    @When("后台根据刚创建的商品 id 再次查询商品编辑信息")
    public void queryProductUpdateInfoAgain() {
        queryProductUpdateInfo();
    }

    @When("后台把刚创建的商品名称更新为 {string}")
    public void updateProductName(String name) {
        response = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(productBody(name, createdProductSn))
                .when()
                .post("/product/update/{id}", createdProductId);
    }

    @Then("商品接口返回码为 {int}")
    public void productApiCodeShouldBe(int code) {
        assertThat(response.jsonPath().getInt("code")).as("商品接口返回码，响应体：%s", response.asString()).isEqualTo(code);
    }

    @Then("商品分页结果的 total 大于 0")
    public void productPageTotalShouldBePositive() {
        assertThat(response.jsonPath().getLong("data.total")).as("商品分页 total").isPositive();
    }

    @Then("商品分页结果第一条的货号等于刚创建的货号")
    public void firstProductSnShouldEqualCreatedSn() {
        assertThat(response.jsonPath().getString("data.list[0].productSn")).as("第一条商品货号").isEqualTo(createdProductSn);
    }

    @Then("商品分页结果的 pageSize 为 {int}")
    public void productPageSizeShouldBe(int pageSize) {
        assertThat(response.jsonPath().getInt("data.pageSize")).as("分页 pageSize").isEqualTo(pageSize);
    }

    @Then("商品分页结果的 list 字段存在")
    public void productPageListShouldExist() {
        assertThat(response.jsonPath().getList("data.list")).as("分页 list 字段").isNotNull();
    }

    @Then("商品编辑信息中的名称等于 {string}")
    public void productUpdateInfoNameShouldBe(String name) {
        assertThat(response.jsonPath().getString("data.name")).as("商品编辑回显名称").isEqualTo(name);
    }
}
