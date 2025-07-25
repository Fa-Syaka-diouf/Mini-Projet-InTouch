param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("export", "import")]
    [string]$action
)

# Variables
$CONFIG_DIR = "keycloak-config"
$REALM_NAME = "task-management"
$REALM_FILE = "$REALM_NAME"+"-realm"+".json"
$LOCAL_REALM_PATH = "$CONFIG_DIR\$REALM_FILE"

function Ensure-Config-Dir {
    if (-not (Test-Path $CONFIG_DIR)) {
        New-Item -ItemType Directory -Path $CONFIG_DIR | Out-Null
        Write-Host "Created directory: $CONFIG_DIR"
    }
}

function Export-Realm {
    Ensure-Config-Dir

    # Delete old file if exists
    if (Test-Path $LOCAL_REALM_PATH) {
        Remove-Item $LOCAL_REALM_PATH
        Write-Host "Old realm file deleted: $LOCAL_REALM_PATH"
    }

    docker exec -it keycloak-server /opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/export --realm $REALM_NAME --users realm_file
    docker cp keycloak-server:/opt/keycloak/data/export/$REALM_FILE $LOCAL_REALM_PATH

    Write-Host "Export done. Realm saved to: $LOCAL_REALM_PATH"
}

function Import-Realm {
    if (-not (Test-Path $LOCAL_REALM_PATH)) {
        Write-Host "Error: Realm file not found at $LOCAL_REALM_PATH"
        exit 1
    }

    docker cp $LOCAL_REALM_PATH keycloak-server:/opt/keycloak/data/import/$REALM_FILE
    docker exec -it keycloak-server /opt/keycloak/bin/kc.sh import --file /opt/keycloak/data/import/$REALM_FILE --override true

    Write-Host "Import done from: $LOCAL_REALM_PATH"
}

if ($action -eq "export") {
    Export-Realm
} elseif ($action -eq "import") {
    Import-Realm
} else {
    Write-Host "Invalid action. Use 'export' or 'import'."
}
