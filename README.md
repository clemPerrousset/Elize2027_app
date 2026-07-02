# Élysée 2027 — Application Android

Sondage présidentiel participatif en temps réel pour l'élection présidentielle française de 2027.

Chaque utilisateur peut voter pour le candidat de son choix, changer d'avis ou se rétracter à tout moment. Le classement est mis à jour en direct depuis le serveur.

> **Backend** → [clemPerrousset/Elyze_backend](https://github.com/clemPerrousset/Elyze_backend) (Rust + SQLite, hébergé en France)

---

## Fonctionnement

1. Au premier lancement, un écran d'introduction présente l'application et ses conditions d'utilisation.
2. L'écran principal affiche la liste des candidats déclarés ou fortement pressentis, classés par nombre de votes décroissant (ordre alphabétique en cas d'égalité).
3. Un tap sur un candidat envoie un vote au serveur. Taper à nouveau sur le même candidat annule le vote. Taper sur un autre candidat transfère le vote.
4. Les scores sont récupérés au lancement et à chaque actualisation manuelle (bouton ↻).

---

## Vie privée & sécurité

- **Aucune donnée personnelle** n'est collectée (ni nom, ni email, ni numéro de téléphone).
- Chaque vote est associé à un **identifiant pseudonyme** : un hash SHA-256 de l'`ANDROID_ID` de l'appareil. Cet identifiant n'est pas réversible vers une identité réelle.
- L'`ANDROID_ID` est stable entre les réinstallations et ne se remet à zéro qu'à la réinitialisation usine — ce qui empêche de voter plusieurs fois via réinstallation.
- Chaque requête est signée par un **token HMAC-SHA256** calculé à partir de l'identifiant et de la date du jour, pour limiter les appels forgés.
- Le code est **open source** et vérifiable par tous.

---

## Architecture technique

### Application Android

| Couche | Technologie |
|---|---|
| UI | Jetpack Compose + Material3 |
| Animations | `animateItem`, `AnimatedVisibility`, `animateFloatAsState` |
| Réseau | Ktor (client Android) |
| Sérialisation | kotlinx.serialization |
| Stockage local | DataStore Preferences |
| Images | Coil |
| Architecture | MVVM (ViewModel + StateFlow) |
| Auth | HMAC-SHA256, secret injecté via `BuildConfig` |

### Identifiant appareil

```
phone_id = SHA-256(ANDROID_ID)   // envoyé dans chaque requête
token    = HMAC-SHA256(phone_id + ":" + YYYYMMDD, VOTE_HMAC_SECRET)
```

### Comportement hors-ligne

Si le serveur est injoignable au lancement, l'application affiche quand même la liste complète des candidats (données embarquées) avec les scores à zéro. Le dernier vote connu est récupéré depuis le cache local (DataStore).

---

## Structure du projet

```
app/src/main/java/com/applicationrush/elise2027/
├── data/
│   ├── api/          # Client Ktor (GET /votes, GET /votes/:id, POST /vote)
│   ├── model/        # Modèles de données + liste des candidats hardcodée
│   └── repository/   # Logique réseau + DataStore
├── ui/
│   ├── screens/      # OnboardingScreen, VoteScreen
│   ├── theme/        # Couleurs, thème Compose
│   └── viewmodel/    # VoteViewModel
└── util/
    ├── DeviceId.kt   # ANDROID_ID → SHA-256
    └── HmacToken.kt  # Génération du token HMAC
```

---

## Candidats

La liste des candidats est **embarquée dans l'application** (fichier `Models.kt`). Le serveur ne stocke que les IDs et les compteurs de votes — il n'est pas la source de vérité sur qui est candidat. La liste est mise à jour manuellement au fil des officialisations de candidatures.

Les IDs utilisés (ex : `melenchon_jeanluc`, `bardella_jordan`) doivent être enregistrés sur le serveur via l'endpoint admin `POST /candidates` avant que les votes ne soient acceptés.

---

## Lancer le projet

### Prérequis

- Android Studio Hedgehog ou plus récent
- SDK Android 26+
- Un serveur backend démarré (voir [Elyze_backend](https://github.com/clemPerrousset/Elyze_backend))

### Configuration

Créer ou compléter `local.properties` à la racine du projet (ce fichier est gitignored) :

```properties
sdk.dir=/chemin/vers/android/sdk
VOTE_HMAC_SECRET=<secret partagé avec le serveur>
SERVER_URL=http://<ip-ou-domaine>:3000
```

> Pour le développement local, le backend accepte `VOTE_HMAC_SECRET=DISABLED` côté serveur pour ignorer la validation du token.

### Build

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew bundleRelease
```

### Enregistrer les candidats sur le serveur

À faire une seule fois après le premier déploiement du backend :

```bash
SERVER="http://<ip>:3000"
ADMIN_TOKEN="<ton_admin_token>"

for ID in \
  arthaud_nathalie asselineau_francois melenchon_jeanluc tondelier_marine \
  guedj_jerome bouamrane_karim ruffin_francois batho_delphine \
  glucksmann_raphael hollande_francois faure_olivier lucaslundy_benjamin \
  massard_lydie retailleau_bruno bertrand_xavier dupontaignan_nicolas \
  lisnard_david wauquiez_laurent attal_gabriel philippe_edouard \
  darmanin_gerald bardella_jordan philippot_florian zemmour_eric
do
  curl -s -X POST "$SERVER/candidates" \
    -H "Content-Type: application/json" \
    -H "X-Admin-Token: $ADMIN_TOKEN" \
    -d "{\"id\": \"$ID\"}" && echo " ✓ $ID"
done
```

---

## Limitations connues

- **Sécurité du secret HMAC** : le secret est embarqué dans l'APK et peut être extrait par décompilation. Une implémentation future pourrait utiliser la [Play Integrity API](https://developer.android.com/google/play/integrity) pour une vérification côté serveur sans secret client.
- **Représentativité** : les participants ne représentent pas fidèlement le corps électoral français. Les résultats donnent une tendance, pas une projection.
- **Anti-triche** : l'application ne garantit pas l'absence de manipulation, mais mise sur une participation massive pour en réduire l'impact.

---

## Licence

Open source. Voir [LICENSE](LICENSE).
