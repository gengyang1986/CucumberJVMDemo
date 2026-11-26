// 仅替换 CartCheckoutUiSteps 中受影响的两个方法（loginOnLoginPage 与 firstVisible 辅助），其余方法不变。
// 说明：原始步骤文件在输入中被截断，无法给出完整文件内容；此处提供可直接覆盖对应方法的修正片段。

    @When("打开商城登录页并输入会员账号密码登录")
    public void loginOnLoginPage() {
        Page p = page();
        p.navigate(trimTrailingSlash(webBaseUrl) + "/#/pages/public/login",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // uni-app H5：placeholder 是独立 div.uni-input-placeholder，真实输入元素为 input.uni-input-input
        Locator usernameInput = firstVisible(p,
                ".input-item:has-text('用户名') input.uni-input-input",
                "uni-input input.uni-input-input",
                "input.uni-input-input");
        usernameInput.click();
        usernameInput.fill(memberUsername);

        // 密码框优先按 type=password 定位，不依赖 placeholder 属性
        Locator passwordInput = firstVisible(p,
                "input[type='password']",
                "uni-input input[type='password']");
        passwordInput.click();
        passwordInput.fill(memberPassword);

        Locator loginButton = p.locator(".confirm-btn").first();
        waitForVisible(loginButton, "登录按钮");
        loginButton.click();
    }

    /**
     * 依次尝试候选选择器，任一可见即返回；首个输入框等待时间放宽到 10s。
     * 注意：不可用于 input[placeholder=...]，uni-app H5 的 placeholder 位于 div.uni-input-placeholder。
     */
    private Locator firstVisible(Page p, String... selectors) {
        long perSelectorTimeout = Math.max(timeoutMs, 10000L);
        for (String selector : selectors) {
            Locator locator = p.locator(selector).first();
            try {
                locator.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout((double) perSelectorTimeout));
                return locator;
            } catch (PlaywrightException ignored) {
                // 继续尝试下一个候选选择器
            }
        }
        throw new AssertionError("未找到可见元素，候选选择器：" + String.join(" | ", selectors)
                + "；当前地址：" + p.url() + "；页面提示：" + visibleToast()
                + "；提示：uni-app H5 的 placeholder 渲染为 div.uni-input-placeholder，请输入框请用 input.uni-input-input 定位");
    }