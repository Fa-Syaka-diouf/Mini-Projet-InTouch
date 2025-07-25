param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("export", "import")]
    [string]$action
)

# Variables
$CONFIG_DIR = "keycloak-config"
$REALM_NAME = "task-management"
$REALM_FILE = "$REALM_NAME"+"-realm"+".json"
$FULL_PATH = Join-Path $CONFIG_DIR $REALM_FILE

function Export-Realm {
    # Créer le dossier s'il n'existe pas
    if (-not (Test-Path -Path $CONFIG_DIR)) {
        New-Item -ItemType Directory -Path $CONFIG_DIR | Out-Null
        Write-Host "Dossier '$CONFIG_DIR' créé."
    }

    # Vérifie si le fichier existe déjà
    if (Test-Path -Path $FULL_PATH) {
        $response = Read-Host "Le fichier '$FULL_PATH' existe déjà. Voulez-vous l'écraser ? (o/n)"
        if ($response -ne 'o' -and $response -ne 'O') {
            Write-Host "Export annulé. Le fichier existant n'a pas été modifié."
            return
        }
    }

    Write-Host "Démarrage de l'export du realm '$REALM_NAME'..."

    # Export du realm
    $exportResult = docker exec keycloak-server /opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/export --realm $REALM_NAME --users realm_file 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Erreur lors de l'export : $exportResult"
        return
    }

    # Vérifier si le fichier a été créé dans le conteneur
    $fileExists = docker exec keycloak-server test -f "/opt/keycloak/data/export/$REALM_FILE" 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Le fichier exporté n'a pas été trouvé dans le conteneur."
        Write-Host "Vérification du contenu du dossier export :"
        docker exec keycloak-server ls -la /opt/keycloak/data/export/
        return
    }

    # Copier le fichier du conteneur vers l'hôte
    $copyResult = docker cp "keycloak-server:/opt/keycloak/data/export/$REALM_FILE" $FULL_PATH 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Erreur lors de la copie : $copyResult"
        return
    }

    Write-Host "Export terminé : $FULL_PATH"
}

function Import-Realm {
    if (-not (Test-Path -Path $FULL_PATH)) {
        Write-Host "Le fichier '$FULL_PATH' n'existe pas. Impossible d'importer."
        Write-Host "Fichiers disponibles dans '$CONFIG_DIR' :"
        if (Test-Path -Path $CONFIG_DIR) {
            Get-ChildItem -Path $CONFIG_DIR -Filter "*.json" | ForEach-Object {
                Write-Host "   - $($_.Name)"
            }
        } else {
            Write-Host "   Le dossier '$CONFIG_DIR' n'existe pas."
        }
        return
    }

    Write-Host "Démarrage de l'import du realm '$REALM_NAME'..."

    # Créer le dossier import dans le conteneur s'il n'existe pas
    docker exec keycloak-server mkdir -p /opt/keycloak/data/import

    # Copier le fichier vers le conteneur
    $copyResult = docker cp $FULL_PATH "keycloak-server:/opt/keycloak/data/import/$REALM_FILE" 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Erreur lors de la copie vers le conteneur : $copyResult"
        return
    }

    # Importer le realm
    $importResult = docker exec keycloak-server /opt/keycloak/bin/kc.sh import --file "/opt/keycloak/data/import/$REALM_FILE" --override true 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Erreur lors de l'import : $importResult"
        return
    }

    Write-Host "Import terminé."
}

# Execution
switch ($action) {
    "export" { Export-Realm }
    "import" { Import-Realm }
    default { Write-Host "Action invalide. Utilisez 'export' ou 'import'." }
}