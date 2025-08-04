<#import "template.ftl" as layout>
<link rel="stylesheet" type="text/css" href="${url.resourcesPath}/css/login-config-totp.css">
<@layout.registrationLayout displayRequiredFields=false displayMessage=!messagesPerField.existsError('totp','userLabel') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        ${msg("loginTotpTitle")}
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <div class="login-otp-container">
                    <!-- Left side - Illustration -->
                    <div class="login-illustration">
                        <img src="${url.resourcesPath}/img/Illustration.png" alt="InTouch Logo">
                    </div>

                    <!-- Right side - TOTP Configuration Form -->
                    <div class="login-form-section">
<#--                        <div class="login-header">-->
<#--                            <div class="logo-container">-->
<#--                                <img src="${url.resourcesPath}/img/Bienvenue_sur_InTouchTask.png" class="logo-text" alt="InTouch Logo">-->
<#--                            </div>-->
<#--                            <div class="logo-container">-->
<#--                                <img src="${url.resourcesPath}/img/logo.png" class="logo-img" alt="InTouch Logo">-->
<#--                            </div>-->
<#--                        </div>-->

<#--                        <div class="totp-setup-info">-->
<#--                            <h2 class="setup-title">Configuration de l'authentification mobile</h2>-->
<#--                            <p class="setup-description">Vous devez configurer Mobile Authenticator pour activer votre compte.</p>-->
<#--                        </div>-->

                        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-totp-settings-form" method="post">
                            <div class="setup-steps">
<#--                                <div class="step">-->
<#--                                    <h3 class="step-title">1. Installez une application d'authentification</h3>-->
<#--                                    <p class="step-description">Installez une des applications suivantes sur votre mobile :</p>-->
<#--                                    <div class="apps-list">-->
<#--                                        <span class="app-name">FreeOTP</span>-->
<#--                                        <span class="app-name">Google Authenticator</span>-->
<#--                                        <span class="app-name">Microsoft Authenticator</span>-->
<#--                                    </div>-->
<#--                                </div>-->

                                <div class="step">
                                    <h3 class="step-title">Scannez le code QR et Entrez le code unique fourni par l'application</h3>
                                    <p class="step-description">Ouvrez l'application <span class="app-name">Google Authenticator</span> et scannez le code-barres :</p>

                                    <div class="qr-container">
                                        <img id="kc-totp-qr" src="data:image/png;base64, ${totp.totpSecretQrCode}" alt="Figure: Barcode">
                                    </div>

                                    <div class="manual-entry">
                                        <button type="button" class="manual-toggle" onclick="toggleManualEntry()">
                                            Impossible de scanner ?
                                        </button>
                                        <div id="manual-entry-details" class="manual-details" style="display: none;">
                                            <p class="manual-label">Code manuel :</p>
                                            <div class="manual-code">${totp.totpSecretEncoded}</div>
                                        </div>
                                    </div>
                                </div>

                            </div>

                            <div class="form-group">
                                <label for="totp" class="form-label">Code unique *</label>
                                <input type="text" id="totp" name="totp" autocomplete="off" class="form-input otp-input" autofocus placeholder="000000" maxlength="6" />
                                <#if messagesPerField.existsError('totp')>
                                    <span class="error-message">${kcSanitize(messagesPerField.get('totp'))?no_esc}</span>
                                </#if>
                            </div>

                            <div class="form-group">
                                <label for="userLabel" class="form-label">Nom de l'appareil</label>
                                <input type="text" id="userLabel" name="userLabel" class="form-input" value="${(totp.otpCredentialLabel!'')}" placeholder="Mon téléphone" />
                                <small class="field-hint">Fournissez un nom d'appareil pour vous aider à gérer vos appareils OTP.</small>
                                <#if messagesPerField.existsError('userLabel')>
                                    <span class="error-message">${kcSanitize(messagesPerField.get('userLabel'))?no_esc}</span>
                                </#if>
                            </div>

                            <#if isAppInitiatedAction??>
                                <div class="form-options">
                                    <label class="checkbox-container">
                                        <input type="checkbox" id="logout-sessions" name="logout-sessions" value="on" checked>
                                        <span class="checkmark"></span>
                                        Se déconnecter des autres appareils
                                    </label>
                                </div>
                            </#if>

                            <input type="hidden" id="totpSecret" name="totpSecret" value="${totp.totpSecret}" />
                            <#if mode??><input type="hidden" id="mode" name="mode" value="${mode}"/></#if>

                            <div class="form-actions">
                                <button class="login-button" type="submit">Soumettre</button>
                                <#if isAppInitiatedAction??>
                                    <button class="cancel-button" type="submit" name="cancel-aia" value="true">Annuler</button>
                                </#if>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <script>
          function toggleManualEntry() {
            const details = document.getElementById('manual-entry-details');
            const button = document.querySelector('.manual-toggle');

            if (details.style.display === 'none') {
              details.style.display = 'block';
              button.textContent = 'Masquer le code manuel';
            } else {
              details.style.display = 'none';
              button.textContent = 'Impossible de scanner ?';
            }
          }
        </script>
    </#if>
</@layout.registrationLayout>