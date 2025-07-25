<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <div class="login-container">
                    <!-- Left side - Illustration -->
                    <div class="login-illustration">
                        <img src="${url.resourcesPath}/img/Illustration.png" alt="InTouch Logo">
                    </div>

                    <!-- Right side - Login Form -->
                    <div class="login-form-section">
                        <div class="login-header">
                            <div class="logo-container">
                                <img src="${url.resourcesPath}/img/Bienvenue_sur_InTouchTask.png" class="logo-text" alt="InTouch Logo">
                            </div>
                            <div class="logo-container">
                                <img src="${url.resourcesPath}/img/logo.png" class="logo-img" alt="InTouch Logo">
                            </div>

                        </div>

                        <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                            <div class="form-group">
                                <label for="username" class="form-label">Email</label>
                                <input tabindex="1" id="username" class="form-input" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="off" placeholder="example@intouchgroup.net" />
                                <#if messagesPerField.existsError('username')>
                                    <span class="error-message">${kcSanitize(messagesPerField.get('username'))?no_esc}</span>
                                </#if>
                            </div>

                            <div class="form-group">
                                <label for="password" class="form-label">Password</label>
                                <div class="password-input-container">
                                    <input tabindex="2" id="password" class="form-input" name="password" type="password" autocomplete="off" placeholder="••••••••••" />
                                </div>
                                <#if messagesPerField.existsError('password')>
                                    <span class="error-message">${kcSanitize(messagesPerField.get('password'))?no_esc}</span>
                                </#if>
                            </div>

                            <div class="form-options">
                                <#if realm.rememberMe && !usernameEditDisabled??>
                                    <label class="checkbox-container">
                                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" <#if login.rememberMe??>checked</#if>>
                                        <span class="checkmark"></span>
                                        Remember me
                                    </label>
                                </#if>
                                <#if realm.resetPasswordAllowed>
                                    <a tabindex="5" href="${url.loginResetCredentialsUrl}" class="forgot-password">Forgot Password?</a>
                                </#if>
                            </div>

                            <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>

                            <button tabindex="4" class="login-button" name="login" id="kc-login" type="submit">Login</button>

                            <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
                                <div class="register-link">
                                    Don't have an account? <a href="${url.registrationUrl}">Register</a>
                                </div>
                            </#if>
                        </form>
                    </div>
                </div>
            </div>
        </div>


    </#if>
</@layout.registrationLayout>