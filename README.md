# Élysée 2027 — Application Android

Sondage présidentiel participatif en temps réel pour l'élection présidentielle française de 2027.

## Fonctionnement

- Votez pour votre candidat préféré en un tap
- Changez ou annulez votre vote à tout moment
- Classement mis à jour en temps réel depuis le serveur

## Vie privée

Aucune donnée personnelle n'est collectée. Le vote est associé à un identifiant unique dérivé de votre appareil (hash SHA-256 de l'`ANDROID_ID`), non réversible vers une identité.

## Architecture

- **UI** : Jetpack Compose, animations `animateItem`
- **Réseau** : Ktor (Kotlin)
- **Stockage local** : DataStore (fallback hors-ligne)
- **Auth** : HMAC-SHA256 par requête, secret injecté à la compilation
- **Backend** : [Élysée 2027 Backend](https://github.com/clemPerrousset/Elyze_backend) — Rust + SQLite, hébergé en France (OVH)

## Build

Prérequis : ajouter dans `local.properties` (non versionné) :

```
VOTE_HMAC_SECRET=<secret partagé avec le serveur>
SERVER_URL=http://<adresse-serveur>:3000
```

```bash
./gradlew assembleDebug
```
