<#import "template.ftl" as layout>
<link rel="stylesheet" type="text/css" href="${url.resourcesPath}/css/register.css">
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('firstName','lastName','email','username','password','password-confirm') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        ${msg("registerTitle")}
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <div class="register-container">
                    <!-- Left side - Illustration -->
                    <div class="register-illustration">
                        <img src="${url.resourcesPath}/img/Illustration.png" alt="InTouch Logo">
                    </div>

                    <!-- Right side - Register Form -->
                    <div class="register-form-section">
                        <div class="register-header">
                            <div class="logo-container">
                                <img src="${url.resourcesPath}/img/Bienvenue_sur_InTouchTask.png" class="logo-text" alt="InTouch Logo">
                            </div>
                            <div class="logo-container">
                                <img src="${url.resourcesPath}/img/logo.png" class="logo-img" alt="InTouch Logo">
                            </div>
                            <h2 class="register-title">Créer un compte</h2>
                        </div>

                        <form id="kc-form-register" onsubmit="register.disabled = true; return true;" action="${url.registrationAction}" method="post">
                            <div class="form-row">
                                <div class="form-group">
                                    <label for="firstName" class="form-label">Prénom</label>
                                    <input tabindex="1" id="firstName" class="form-input" name="firstName" value="${(register.formData.firstName!'')}" type="text" autofocus autocomplete="given-name" placeholder="Votre prénom" />
                                    <#if messagesPerField.existsError('firstName')>
                                        <span class="error-message">${kcSanitize(messagesPerField.get('firstName'))?no_esc}</span>
                                    </#if>
                                </div>

                                <div class="form-group">
                                    <label for="lastName" class="form-label">Nom</label>
                                    <input tabindex="2" id="lastName" class="form-input" name="lastName" value="${(register.formData.lastName!'')}" type="text" autocomplete="family-name" placeholder="Votre nom" />
                                    <#if messagesPerField.existsError('lastName')>
                                        <span class="error-message">${kcSanitize(messagesPerField.get('lastName'))?no_esc}</span>
                                    </#if>
                                </div>
                            </div>
                            <div class="form-row">
                                <div class="form-group">
                                    <label for="email" class="form-label">Email</label>
                                    <input tabindex="3" id="email" class="form-input" name="email" value="${(register.formData.email!'')}" type="email" autocomplete="email" placeholder="votre.email@exemple.com" />
                                    <#if messagesPerField.existsError('email')>
                                        <span class="error-message">${kcSanitize(messagesPerField.get('email'))?no_esc}</span>
                                    </#if>
                                </div>

                                <div class="form-group">
                                    <label for="user.attributes.country" class="form-label">Pays</label>
                                    <select tabindex="4" id="country" class="form-select" name="user.attributes.country">
                                        <option value="Senegal" <#if (register.formData['user.attributes.country']!'Senegal') == 'Senegal'>selected</#if>>Sénégal</option>
                                        <option value="France" <#if (register.formData['user.attributes.country']!'') == 'France'>selected</#if>>France</option>
                                        <option value="Mali" <#if (register.formData['user.attributes.country']!'') == 'Mali'>selected</#if>>Mali</option>
                                        <option value="Burkina Faso" <#if (register.formData['user.attributes.country']!'') == 'Burkina Faso'>selected</#if>>Burkina Faso</option>
                                        <option value="Cote d'Ivoire" <#if (register.formData['user.attributes.country']!'') == 'Cote d\'Ivoire'>selected</#if>>Côte d'Ivoire</option>
                                        <option value="Ghana" <#if (register.formData['user.attributes.country']!'') == 'Ghana'>selected</#if>>Ghana</option>
                                        <option value="Nigeria" <#if (register.formData['user.attributes.country']!'') == 'Nigeria'>selected</#if>>Nigeria</option>
                                        <option value="Maroc" <#if (register.formData['user.attributes.country']!'') == 'Maroc'>selected</#if>>Maroc</option>
                                        <option value="Tunisie" <#if (register.formData['user.attributes.country']!'') == 'Tunisie'>selected</#if>>Tunisie</option>
                                        <option value="Cameroun" <#if (register.formData['user.attributes.country']!'') == 'Cameroun'>selected</#if>>Cameroun</option>
                                    </select>
                                    <#if messagesPerField.existsError('user.attributes.country')>
                                        <span class="error-message">${kcSanitize(messagesPerField.get('user.attributes.country'))?no_esc}</span>
                                    </#if>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="username" class="form-label">Nom d'utilisateur</label>
                                <input tabindex="5" id="username" class="form-input" name="username" value="${(register.formData.username!'')}" type="text" autocomplete="username" placeholder="votre_nom_utilisateur" />
                                <#if messagesPerField.existsError('username')>
                                    <span class="error-message">${kcSanitize(messagesPerField.get('username'))?no_esc}</span>
                                </#if>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="password" class="form-label">Mot de passe</label>
                                    <div class="password-input-container">
                                        <input tabindex="6" id="password" class="form-input" name="password" type="password" autocomplete="new-password" placeholder="••••••••••" />
                                        <button type="button" class="password-toggle" onclick="togglePassword('password')">
                                        </button>
                                    </div>
                                    <#if messagesPerField.existsError('password')>
                                        <span class="error-message">${kcSanitize(messagesPerField.get('password'))?no_esc}</span>
                                    </#if>
                                </div>

                                <div class="form-group">
                                    <label for="password-confirm" class="form-label">Confirmer le mot de passe</label>
                                    <div class="password-input-container">
                                        <input tabindex="7" id="password-confirm" class="form-input" name="password-confirm" type="password" autocomplete="new-password" placeholder="••••••••••" />
                                        <button type="button" class="password-toggle" onclick="togglePassword('password-confirm', 'eye-icon-2')">
                                            <span id="eye-icon-2">👁️</span>
                                        </button>
                                    </div>
                                    <#if messagesPerField.existsError('password-confirm')>
                                        <span class="error-message">${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}</span>
                                    </#if>
                                </div>
                            </div>

                            <button tabindex="8" class="register-button" name="register" id="kc-register" type="submit">Créer mon compte</button>

                            <div class="login-link">
                                Vous avez déjà un compte? <a href="${url.loginUrl}">Se connecter</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <script>
          function togglePassword(inputId, iconId) {
            const input = document.getElementById(inputId);
            const icon = document.getElementById(iconId);

            if (input.type === 'password') {
              input.type = 'text';
              icon.textContent = '🙈';
            } else {
              input.type = 'password';
              icon.textContent = '👁️';
            }
          }
        </script>

    </#if>
</@layout.registrationLayout>