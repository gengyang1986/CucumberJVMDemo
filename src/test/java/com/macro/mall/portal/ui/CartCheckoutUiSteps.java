package com.macro.mall.portal.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 前台商城 H5（mall-app-web，uni-app Vue3）「登录 → 购物车 → 去结算 → 提交订单」UI 步骤。
 *
 * <p>UI 驱动：Playwright（Java 版 com.microsoft.playwright），不使用 Selenium。
 * uni-app 编译到 H5 后 view/text/button 会渲染成 uni-view/uni-text/uni-button，源码里的 class
 * 会保留，因此本类按源码中的 class 与 input placeholder 定位，主要依据：
 * pages/public/login.vue、pages/cart/cart.vue、pages/order/createOrder.vue、pages.json（tabBar）。
 *
 * <p>鉴权约定：mall-portal 的 JwtAuthenticationTokenFilter 要求请求头 Authorization 以
 * jwt.tokenHead（application.yml 中为 "Bearer "）开头，否则 token 根本不会被解析，
 * 受保护接口（/member/**、/cart/**）直接返回 401。因此本类登录后按 tokenHead + token 拼接请求头，
 * tokenHead 优先取登录响应 data.tokenHead，缺失时兜底 "Bearer "。
 *
 * <p>本类自包含，且只在 @ui 场景下初始化（不会为接口场景拉起浏览器）。
 * 环境变量：WEB_BASE_URL、PORTAL_BASE_URL（或 BASE_URL）、MEMBER_USERNAME、MEMBER_PASSWORD；
 * 可选：UI_HEADLESS（默认 true）、UI_TIMEOUT_MS（默认 20000）、UI_PRODUCT_ID（默认 1）。
 *
 * <p>前置数据通过接口准备：/sso/login 取 token → 无默认地址则 /member/address/add →
 * 购物车为空则按 /cart/getProduct/{productId} 的 sku 调 /cart/add 加一件。
 */
public class CartCheckoutUiSteps {

    private static final int DEFAULT_TIMEOUT_MS = 20000;
    private static final long DEFAULT_PRODUCT_ID = 1L;
    /** mall-portal application.yml 中 jwt.tokenHead 的默认值，注意末尾有一个空格 */
    private static final String DEFAULT_TOKEN_HEAD = "Bearer ";

    private String webBaseUrl;
    private String portalBaseUrl;
    private String memberUsername;
    private String memberPassword;
    private boolean headless = true;
    private int timeoutMs = DEFAULT_TIMEOUT_MS;
    private long productId = DEFAULT_PRODUCT_ID;
    private boolean captureScreenshot = false;
    private boolean captureVideo = false;
    private boolean captureTrace = false;
    private boolean captureFailedOnly = true;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    // ==================== 配置与生命周期 ====================

    @Before("@ui")
    public void resolveUiConfig() {
        webBaseUrl = firstNonBlank(System.getenv("WEB_BASE_URL"),
                System.getenv("MALL_APP_WEB_BASE_URL"),
                System.getProperty("web.baseUrl"));
        assertThat(webBaseUrl)
                .as("未获取到 H5 站点地址：请在接口测试环境 app-portal 配置变量 WEB_BASE_URL（mall-app-web 的访问地址）")
                .isNotBlank();

        portalBaseUrl = firstNonBlank(System.getenv("PORTAL_BASE_URL"),
                System.getenv("BASE_URL"),
                System.getProperty("portal.baseUrl"));
        assertThat(portalBaseUrl)
                .as("未获取到前台商城接口地址：请配置 PORTAL_BASE_URL（或 BASE_URL），用于通过接口准备购物车与收货地址")
                .isNotBlank();

        memberUsername = firstNonBlank(System.getenv("MEMBER_USERNAME"), System.getenv("PORTAL_USERNAME"));
        memberPassword = firstNonBlank(System.getenv("MEMBER_PASSWORD"), System.getenv("PORTAL_PASSWORD"));
        assertThat(memberUsername).as("未获取到会员账号：请配置环境变量 MEMBER_USERNAME").isNotBlank();
        assertThat(memberPassword).as("未获取到会员密码：请配置环境变量 MEMBER_PASSWORD").isNotBlank();

        headless = !"false".equalsIgnoreCase(firstNonBlank(System.getenv("UI_HEADLESS"), "true"));
        timeoutMs = parseInt(firstNonBlank(System.getenv("UI_TIMEOUT_MS")), DEFAULT_TIMEOUT_MS);
        productId = parseLong(firstNonBlank(System.getenv("UI_PRODUCT_ID")), DEFAULT_PRODUCT_ID);
        captureScreenshot = envBool("CAPTURE_SCREENSHOT", false);
        captureVideo = envBool("CAPTURE_VIDEO", false);
        captureTrace = envBool("CAPTURE_TRACE", false);
        captureFailedOnly = envBool("CAPTURE_FAILED_ONLY", true);
    }

    @After("@ui")
    public void closeBrowser(Scenario scenario) {
        boolean shouldCapture = !captureFailedOnly || scenario.isFailed();
        if (page != null && captureScreenshot && shouldCapture) {
            try {
                byte[] shot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                scenario.attach(shot, "image/png", "UI 失败截图");
            } catch (Exception ignored) {
                // 截图失败不影响断言结论
            }
        }
        if (context != null && captureTrace) {
            try {
                if (shouldCapture) {
                    Path trace = Files.createTempFile("cucumber-trace-", ".zip");
                    context.tracing().stop(new Tracing.StopOptions().setPath(trace));
                    scenario.attach(Files.readAllBytes(trace), "application/zip", "UI 失败 trace");
                    Files.deleteIfExists(trace);
                } else {
                    context.tracing().stop();
                }
            } catch (Exception ignored) {
                // trace 采集失败不影响断言结论
            }
        }
        Path video = page != null && page.video() != null ? page.video().path() : null;
        try { if (context != null) context.close(); } catch (Exception ignored) { /* 忽略清理异常 */ }
        if (captureVideo && video != null && Files.exists(video)) {
            try {
                if (shouldCapture) {
                    scenario.attach(Files.readAllBytes(video), "video/webm", "UI 失败视频");
                }
                Files.deleteIfExists(video);
            } catch (Exception ignored) {
                // 视频采集失败不影响断言结论
            }
        }
        try { if (browser != null) browser.close(); } catch (Exception ignored) { /* 忽略清理异常 */ }
        try { if (playwright != null) playwright.close(); } catch (Exception ignored) { /* 忽略清理异常 */ }
        page = null;
        context = null;
        browser = null;
        playwright = null;
    }

    // ==================== 步骤定义 ====================

    @Given("H5 商城站点地址已配置")
    public void h5SiteConfigured() {
        assertThat(webBaseUrl).as("H5 站点地址 WEB_BASE_URL 未配置").isNotBlank();
    }

    @Given("会员账号的购物车与收货地址数据已准备")
    public void prepareMemberData() {
        String authorization = portalLogin();
        ensureDefaultAddress(authorization);
        ensureCartHasItem(authorization);
    }

    @When("打开商城登录页并输入会员账号密码登录")
    public void loginOnLoginPage() {
        Page p = page();
        p.navigate(trimTrailingSlash(webBaseUrl) + "/#/pages/public/login",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // uni-app H5：placeholder 渲染为独立的 div.uni-input-placeholder，真实 input 为 input.uni-input-input（无 placeholder 属性）
        Locator usernameInput = firstVisible(p,
                "uni-input input.uni-input-input",
                ".input-item:has-text('用户名') input",
                "input.uni-input-input");
        usernameInput.fill(memberUsername);

        // 密码框优先用 type=password 定位（H5 端 password 属性会映射为 input[type='password']）
        Locator passwordInput = firstVisible(p,
                "input[type='password']",
                "uni-input input.uni-input-input >> nth=1",
                ".input-item:has-text('密码') input");
        passwordInput.fill(memberPassword);

        Locator loginButton = p.locator(".confirm-btn").first();
        waitForVisible(loginButton, "登录按钮");
        loginButton.click();
    }

    @Then("应登录成功并跳转到首页")
    public void loginSucceeded() {
        Page p = page();
        waitForUrlContains(p, "#/pages/index/index");
        assertThat(p.url()).as("登录成功后应回到首页（login.vue 中登录成功即 uni.switchTab 首页）")
                .contains("#/pages/index/index");
    }

    @When("点击底部「购物车」标签页")
    public void clickCartTab() {
        Page p = page();
        Locator cartTab = p.locator(".uni-tabbar__item")
                .filter(new Locator.FilterOptions().setHasText("购物车"))
                .first();
        waitForVisible(cartTab, "底部「购物车」标签页");
        cartTab.click();
        waitForUrlContains(p, "#/pages/cart/cart");
    }

    @Then("购物车页应显示可结算商品")
    public void cartPageReady() {
        Page p = page();
        assertThat(p.url()).as("应进入购物车页").contains("#/pages/cart/cart");
        Locator cartItems = p.locator(".cart-item");
        waitForVisible(cartItems.first(), "购物车商品条目");
        assertThat(cartItems.count()).as("购物车商品条数应大于 0（前置已通过接口加购）").isGreaterThan(0);
        Locator checkoutButton = p.locator(".action-section .confirm-btn").first();
        waitForVisible(checkoutButton, "「去结算」按钮");
        assertThat(checkoutButton.innerText()).as("购物车底部按钮文案").contains("去结算");
    }

    @When("在购物车页点击「去结算」")
    public void clickCheckout() {
        Page p = page();
        p.locator(".action-section .confirm-btn").first().click();
        waitForUrlContains(p, "#/pages/order/createOrder");
    }

    @Then("创建订单页应显示「提交订单」按钮且展示收货地址与商品")
    public void createOrderPageReady() {
        Page p = page();
        assertThat(p.url()).as("应进入创建订单页").contains("#/pages/order/createOrder");
        Locator submit = p.locator(".footer .submit").first();
        waitForVisible(submit, "「提交订单」按钮");
        assertThat(submit.innerText()).as("底部提交按钮文案").contains("提交订单");
        Locator receiver = p.locator(".address-section .name").first();
        waitForVisible(receiver, "收货人姓名");
        assertThat(receiver.innerText()).as("创建订单页应带出默认收货地址（前置已准备）").isNotBlank();
        assertThat(p.locator(".goods-section .g-item").count()).as("创建订单页应展示结算商品").isGreaterThan(0);
    }

    @When("在创建订单页点击「提交订单」")
    public void clickSubmitOrder() {
        Page p = page();
        Locator submit = p.locator(".footer .submit").first();
        waitForVisible(submit, "「提交订单」按钮");
        submit.click();
    }

    @Then("应弹出「订单创建成功」提示")
    public void orderCreatedModalShown() {
        Page p = page();
        Locator modal = p.locator(".uni-modal").first();
        waitForVisible(modal, "下单结果弹窗");
        assertThat(modal.innerText())
                .as("下单结果弹窗文案；若不是成功提示，先检查前置数据（地址/商品）与接口环境")
                .contains("订单创建成功");
        Locator cancel = modal.locator(".uni-modal__btn")
                .filter(new Locator.FilterOptions().setHasText("取消"))
                .first();
        if (cancel.count() > 0) {
            cancel.click();
            waitForUrlContains(p, "#/pages/order/order");
        }
    }

    // ==================== Playwright 辅助 ====================

    private Page page() {
        if (page != null) {
            return page;
        }
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(List.of("--disable-dev-shm-usage")));
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(390, 844)
                .setLocale("zh-CN")
                .setIgnoreHTTPSErrors(true);
        if (captureVideo) {
            try {
                Path videoDir = Path.of(firstNonBlank(System.getenv("RESULT_DIR"), "/result"), "videos");
                Files.createDirectories(videoDir);
                contextOptions.setRecordVideoDir(videoDir);
            } catch (Exception ignored) {
                // 视频目录不可用时退化为不录像
            }
        }
        context = browser.newContext(contextOptions);
        if (captureTrace) {
            try {
                context.tracing().start(new Tracing.StartOptions()
                        .setScreenshots(true).setSnapshots(true).setSources(true));
            } catch (Exception ignored) {
                // trace 不可用时继续执行
            }
        }
        page = context.newPage();
        page.setDefaultTimeout(timeoutMs);
        return page;
    }

    private Locator waitForVisible(Locator locator, String what) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout((double) timeoutMs));
            return locator;
        } catch (PlaywrightException e) {
            throw new AssertionError("等待「" + what + "」出现超时；当前地址：" + currentUrl()
                    + "；页面提示：" + visibleToast(), e);
        }
    }

    private Locator firstVisible(Page p, String... selectors) {
        for (String selector : selectors) {
            Locator locator = p.locator(selector).first();
            try {
                locator.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(1000d));
                return locator;
            } catch (PlaywrightException ignored) {
                // 继续尝试下一个候选选择器
            }
        }
        throw new AssertionError("未找到可见元素，候选选择器：" + String.join(" | ", selectors)
                + "；当前地址：" + p.url() + "；页面提示：" + visibleToast());
    }

    private void waitForUrlContains(Page p, String fragment) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (p.url().contains(fragment)) {
                return;
            }
            p.waitForTimeout(200);
        }
        throw new AssertionError("等待页面跳转到包含「" + fragment + "」超时；当前地址：" + p.url()
                + "；页面提示：" + visibleToast());
    }

    private String currentUrl() {
        return page != null ? page.url() : "";
    }

    private String visibleToast() {
        try {
            if (page != null) {
                Locator toast = page.locator(".uni-toast").first();
                if (toast.count() > 0) {
                    return toast.innerText();
                }
            }
        } catch (Exception ignored) {
            // 忽略读取提示失败
        }
        return "无";
    }

    // ==================== 接口辅助（仅准备数据，不做 UI 断言） ====================

    /**
     * 会员登录，返回可直接放入 Authorization 请求头的完整值（tokenHead + token）。
     * mall-portal 的 JwtAuthenticationTokenFilter 只有在请求头以 tokenHead 开头时才解析 token，
     * 因此不能只放裸 token。
     */
    private String portalLogin() {
        Response response = RestAssured.given()
                .baseUri(trimTrailingSlash(portalBaseUrl))
                .contentType(ContentType.URLENC)
                .formParam("username", memberUsername)
                .formParam("password", memberPassword)
                .post("/sso/login");
        assertThat(response.statusCode()).as("会员登录接口 HTTP 状态，响应：%s", response.asString()).isEqualTo(200);
        JsonPath json = response.jsonPath();
        assertThat(json.get("code")).as("会员登录业务码，响应：%s", response.asString()).isEqualTo(200);
        Object data = json.get("data");
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            Object token = dataMap.get("token");
            assertThat(token).as("登录响应 data.token 为空，响应：%s", response.asString()).isNotNull();
            return withTokenHead(String.valueOf(token), dataMap.get("tokenHead"));
        }
        assertThat(data).as("登录响应 data 为空，响应：%s", response.asString()).isNotNull();
        return withTokenHead(String.valueOf(data), null);
    }

    /**
     * 拼接 Authorization 请求头：tokenHead + token（tokenHead 取登录响应 data.tokenHead，缺省 "Bearer "）。
     * 已带前缀时原样返回，避免重复拼接成 "Bearer Bearer xxx"。
     */
    private static String withTokenHead(String token, Object tokenHeadFromResponse) {
        String raw = token == null ? "" : token.trim();
        if (raw.startsWith(DEFAULT_TOKEN_HEAD.trim())) {
            return raw;
        }
        String head = tokenHeadFromResponse == null || String.valueOf(tokenHeadFromResponse).isBlank()
                ? DEFAULT_TOKEN_HEAD
                : String.valueOf(tokenHeadFromResponse);
        return head + raw;
    }

    /** 前置接口若鉴权失败，直接抛出指向 Authorization 前缀的明确断言，避免后续步骤症状失真 */
    private void assertAuthorized(Response response, String what) {
        int status = response.statusCode();
        Integer bodyCode = null;
        try {
            bodyCode = response.jsonPath().getInt("code");
        } catch (RuntimeException ignored) {
            // 响应体非 JSON 时只按 HTTP 状态判断
        }
        boolean unauthorized = status == 401 || status == 403
                || (bodyCode != null && (bodyCode == 401 || bodyCode == 403));
        assertThat(unauthorized)
                .as("%s 返回未认证（HTTP %s，响应：%s）：Authorization 请求头必须是 tokenHead + token，"
                        + "mall-portal 的 jwt.tokenHead 为 \"Bearer \"，裸 token 不会被 JwtAuthenticationTokenFilter 解析",
                        what, status, response.asString())
                .isFalse();
    }

    private void ensureDefaultAddress(String authorization) {
        Response listResponse = RestAssured.given()
                .baseUri(trimTrailingSlash(portalBaseUrl))
                .header("Authorization", authorization)
                .get("/member/address/list");
        assertAuthorized(listResponse, "收货地址列表接口 /member/address/list");
        for (Map<String, Object> address : readList(listResponse.jsonPath(), "data")) {
            Object defaultStatus = address.get("defaultStatus");
            if (defaultStatus instanceof Number && ((Number) defaultStatus).intValue() == 1) {
                return;
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "自动化收货人");
        body.put("phoneNumber", "13800138000");
        body.put("defaultStatus", 1);
        body.put("province", "广东省");
        body.put("city", "深圳市");
        body.put("region", "南山区");
        body.put("detailAddress", "科技园自动化测试地址 1 号");

        Response addResponse = RestAssured.given()
                .baseUri(trimTrailingSlash(portalBaseUrl))
                .header("Authorization", authorization)
                .contentType(ContentType.JSON)
                .body(body)
                .post("/member/address/add");
        assertThat(addResponse.jsonPath().get("code"))
                .as("准备默认收货地址失败，响应：%s", addResponse.asString())
                .isEqualTo(200);
    }

    private void ensureCartHasItem(String authorization) {
        Response listResponse = RestAssured.given()
                .baseUri(trimTrailingSlash(portalBaseUrl))
                .header("Authorization", authorization)
                .get("/cart/list");
        assertAuthorized(listResponse, "购物车列表接口 /cart/list");
        if (!readList(listResponse.jsonPath(), "data").isEmpty()) {
            return;
        }

        Response productResponse = RestAssured.given()
                .baseUri(trimTrailingSlash(portalBaseUrl))
                .header("Authorization", authorization)
                .get("/cart/getProduct/{productId}", productId);
        Object skuId = firstSkuId(productResponse.jsonPath());
        assertThat(skuId)
                .as("未从 /cart/getProduct/%s 解析到 skuId，响应：%s", productId, productResponse.asString())
                .isNotNull();

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("productId", productId);
        item.put("productSkuId", skuId);
        item.put("quantity", 1);
        // cart.vue 对 productAttr 做 JSON.parse，必须传合法 JSON 数组字符串，否则购物车渲染为空
        item.put("productAttr", "[]");

        Response addResponse = RestAssured.given()
                .baseUri(trimTrailingSlash(portalBaseUrl))
                .header("Authorization", authorization)
                .contentType(ContentType.JSON)
                .body(item)
                .post("/cart/add");
        assertThat(addResponse.jsonPath().get("code"))
                .as("加购失败，响应：%s", addResponse.asString())
                .isEqualTo(200);
    }

    private Object firstSkuId(JsonPath productJson) {
        for (String path : new String[]{"data.productSkuStockList", "data.skuStockList"}) {
            for (Map<String, Object> sku : readList(productJson, path)) {
                Object id = sku.get("id");
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }

    private List<Map<String, Object>> readList(JsonPath json, String path) {
        try {
            List<Map<String, Object>> list = json.getList(path);
            return list == null ? List.of() : list;
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    // ==================== 通用工具 ====================

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String trimTrailingSlash(String url) {
        String result = url.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static boolean envBool(String name, boolean fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) return fallback;
        String v = value.trim().toLowerCase();
        return "true".equals(v) || "1".equals(v) || "yes".equals(v) || "on".equals(v);
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static long parseLong(String value, long defaultValue) {
        try {
            return value == null ? defaultValue : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}