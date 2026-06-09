# Keystore Setup for Release Signing

## What is `KEYSTORE_BASE64`?

Il file `keystore.jks` codificato in base64, che serve per firmare l'APK di release.

## Creare un nuovo keystore

Se non hai ancora un keystore:

```sh
keytool -genkey -v -keystore keystore.jks -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
```

Ti chiederà:
- Una password per lo store → `KEYSTORE_PASSWORD`
- Un alias → `KEY_ALIAS`
- Una password per la chiave → `KEY_PASSWORD`

## Configurare i GitHub Secrets

Codifica il keystore in base64 e copialo negli appunti:

```sh
base64 -i keystore.jks | tr -d '\n' | pbcopy
```

Poi vai su **GitHub → Settings → Secrets → Actions** e aggiungi i seguenti secrets:

| Secret             | Valore                              |
|--------------------|-------------------------------------|
| `KEYSTORE_BASE64`  | output del comando base64 sopra     |
| `KEYSTORE_PASSWORD`| password dello store                |
| `KEY_ALIAS`        | alias della chiave                  |
| `KEY_PASSWORD`     | password della chiave               |
