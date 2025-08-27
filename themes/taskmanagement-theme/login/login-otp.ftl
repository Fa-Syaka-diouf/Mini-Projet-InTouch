<#import "template.ftl" as layout>
<link rel="stylesheet" type="text/css" href="${url.resourcesPath}/css/login-otp.css">
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('totp') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        ${msg("doLogIn")}
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <div class="login-container">
                    <!-- Left side - Illustration -->
                    <div class="login-illustration">
                        <img src="${url.resourcesPath}/img/Illustration.png" alt="InTouch Logo">
                    </div>

                    <!-- Right side - OTP Form -->
                    <div class="login-form-section">
                        <div class="login-header">
                            <div class="logo-container">
                                <img src="${url.resourcesPath}/img/Bienvenue_sur_InTouchTask.png" class="logo-text" alt="InTouch Logo">
                            </div>
                            <div class="logo-container">
                                <img src="${url.resourcesPath}/img/logo.png" class="logo-img" alt="InTouch Logo">
                            </div>
                        </div>

                        <div id="otp-info" class="otp-info">
                            <h2 class="otp-title">Code de vérification</h2>
                            <p class="otp-description">Entrez le code de vérification généré par votre application d'authentification</p>
                        </div>

                        <form id="kc-otp-login-form" action="${url.loginAction}" method="post">
                            <div class="form-group">
                                <label for="otp" class="form-label">Code OTP</label>
                                <input id="otp" name="otp" autocomplete="off" type="text" class="form-input otp-input" autofocus placeholder="000000" maxlength="6" />
                                <#if messagesPerField.existsError('totp')>
                                    <span class="error-message">${kcSanitize(messagesPerField.get('totp'))?no_esc}</span>
                                </#if>
                            </div>

                            <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>

                            <button class="login-button" name="login" id="kc-login" type="submit">Vérifier</button>

                            <!-- Formulaire de réinitialisation OTP (masqué par défaut) -->
                            <div id="reset-otp-form" class="${properties.kcFormGroupClass!}" style="display: none;">
                                <div class=" form-group">
                                    <label for="reset-email" class="${properties.kcLabelClass!} form-label">Confirmez votre adresse email :</label>
                                    <input id="reset-email" name="reset-email" type="email" class="${properties.kcInputClass!} form-input"
                                           placeholder="votre@email.com"/>
                                </div>
                                <div class="${properties.kcFormButtonsClass!} form-group" style="margin-top: 10px;">
                                    <button type="button" id="confirm-reset-btn" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} login-button">
                                        Envoyer l'email de réinitialisation
                                    </button>
                                    <button type="button" id="cancel-reset-btn" class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!} login-button">
                                        Annuler
                                    </button>
                                </div>
                            </div>

                            <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                                <a id="reset-otp-btn" class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!}">
                                    ${msg("resetOTP","Je n'ai pas accès à mon OTP")}
                                </a>

                            </div>
                            <div class="back-to-login">
                                <a href="${url.loginUrl}" class="back-link">← Retour à la connexion</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <script>
          document.addEventListener('DOMContentLoaded', function() {
            const resetOtpBtn = document.getElementById('reset-otp-btn');
            const resetOtpForm = document.getElementById('reset-otp-form');
            const confirmResetBtn = document.getElementById('confirm-reset-btn');
            const cancelResetBtn = document.getElementById('cancel-reset-btn');
            const kcFormButtons = document.getElementById('kc-form-buttons');
            const otp_info = document.getElementById('otp-info');
            const kc_login = document.getElementById('kc-login');
            const otpInput = document.querySelector('input[name="otp"]');
            const resetEmailInput = document.getElementById('reset-email');

            function getAuthUsername() {
              <#if auth?? && auth.attemptedUsername??>
              return '${auth.attemptedUsername}';
              </#if>

              <#if auth?? && auth.username??>
              return '${auth.username}';
              </#if>
              <#if login?? && login.username??>
              return '${login.username}';
              </#if>

              return prompt('Veuillez saisir votre nom d\'utilisateur:');
            }

            resetOtpBtn.addEventListener('click', function() {
              resetOtpForm.style.display = 'block';
              resetEmailInput.required = true;
              kcFormButtons.style.display = 'none';
              otpInput.style.display = 'none';
              otp_info.style.display = 'none';
              kc_login.style.display = 'none';
              otpInput.parentElement.style.display = 'none';
              resetEmailInput.focus();
            });

            cancelResetBtn.addEventListener('click', function() {
              resetOtpForm.style.display = 'none';
              resetEmailInput.required = false;
              kcFormButtons.style.display = 'block';
              otpInput.style.display = 'block';
              otp_info.style.display = 'block';
              kc_login.style.display = 'block';
              otpInput.parentElement.style.display = 'flex';
              resetEmailInput.value = '';
            });
            confirmResetBtn.addEventListener('click', function() {
              const email = resetEmailInput.value.trim();
              const username = getAuthUsername();

              if (!username) {
                alert('Impossible de récupérer le nom d\'utilisateur. Veuillez réessayer.');
                return;
              }

              if (!email) {
                alert('Veuillez saisir votre adresse email.');
                return;
              }

              if (!isValidEmail(email)) {
                alert('Veuillez saisir une adresse email valide.');
                return;
              }

              confirmResetBtn.disabled = true;
              confirmResetBtn.textContent = 'Envoi en cours...';


              fetch('http://localhost:8080/api/reset-otp', {
                method: 'POST',
                mode: 'cors',
                credentials: 'include',
                headers: {
                  'Content-Type': 'application/x-www-form-urlencoded',
                  'Accept': 'application/json'
                },
                body: new URLSearchParams({
                  username: username,
                  email: email
                })
              })
                .then(response => {
                  if (!response.ok) {
                    throw new Error('Network response was not ok');
                  }
                  return response.json();
                })
                .then(data => {
                  if (data.success) {
                    alert('Un email de réinitialisation a été envoyé à votre adresse. Vérifiez votre boîte mail.');
                  } else {
                    alert(data.message || 'Erreur lors de l\'envoi de l\'email. Vérifiez votre adresse email.');
                  }
                })
                .catch(error => {
                  console.error('Error:', error);
                  alert('Erreur de connexion.');

                })
                .finally(() => {
                  confirmResetBtn.disabled = false;
                  confirmResetBtn.textContent = 'Envoyer l\'email de réinitialisation';
                });
            });

            function isValidEmail(email) {
              const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
              return emailRegex.test(email);
            }

            resetEmailInput.addEventListener('keypress', function(e) {
              if (e.key === 'Enter') {
                confirmResetBtn.click();
              }
            });
          });
        </script>
    </#if>

</@layout.registrationLayout>