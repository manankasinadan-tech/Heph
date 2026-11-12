# Hephaistos Launcher

<div align="center">
  <img src="app/src/main/res/drawable/hephaistos_icon_1794444239132.jpg" alt="Hephaistos Logo" width="128" height="128" style="border-radius: 24px;" />
  <br />
  <strong>Lanceur d'applications et orchestrateur de commandes pour l'écosystème modulaire de portage GSI Android.</strong>
  <br /><br />
  <a href="https://github.com/hephaistos-org/hephaistos-launcher/actions"><img src="https://img.shields.io/badge/build-passing-brightgreen?style=flat-square&logo=githubactions" alt="Build Status" /></a>
  <a href="#spécifications-de-lécosystème"><img src="https://img.shields.io/badge/ecosystem-com.hephaistos.*-amber?style=flat-square" alt="Ecosystem" /></a>
  <a href="#licence"><img src="https://img.shields.io/badge/license-Apache_2.0-blue?style=flat-square" alt="License" /></a>
</div>

---

## 📌 Présentation

**Hephaistos** (`com.hephaistos.launcher`) est la première brique et l'hôte de l'écosystème Hephaistos. Conçu pour le portage automatique de GSI (Generic System Image), l'extraction et la manipulation de partitions système (`system.img`, `vendor.img`, `product.img`), Hephaistos agit comme :
- **Un lanceur épuré et moderne** qui affiche exclusivement les applications appartenant à l'écosystème (`com.hephaistos.*` ou sélectionnées manuellement).
- **Un dock flottant Material 3 inspiré de macOS** offrant un accès direct au rafraîchissement, à la console et aux paramètres.
- **Une console / terminal unifiée** capable d'exécuter des commandes et des actions en temps réel sur n'importe quel module avec formatage JSON.
- **Le gardien des règles d'interface unifiées** pour que les futurs moteurs n'aient pas besoin de réinventer l'UI.

---

## 📖 Guide de Création des Futures Applications

> 🔗 **Accès direct aux spécifications complètes :**  
> Consultez le document de référence [**ECOSYSTEM_SPEC.md**](app/src/main/assets/ECOSYSTEM_SPEC.md) ainsi que le modèle de manifeste [**manifest.json**](app/src/main/assets/manifest.json).

Toute nouvelle application ou tout agent IA créant un module dans l'écosystème doit respecter les standards suivants :

### 1. Nom de package obligatoire
Chaque application future doit avoir pour identifiant :
```
com.hephaistos.<nom_application>
```
*Exemples : `com.hephaistos.unpacker`, `com.hephaistos.gsi_patcher`, `com.hephaistos.imgtool`.*

### 2. Auto-documentation & Manifeste
Chaque application créée doit intégrer un fichier `manifest.json` téléchargeable détaillant ses commandes, arguments et formats de réponse en JSON strict.

### 3. Protocole d'Intents & IPC
- **Envoi de commande** : Intent avec action `com.hephaistos.action.EXECUTE`
  - Extra `EXTRA_COMMAND` : Nom de la commande (ex: `unpack`)
  - Extra `EXTRA_PAYLOAD` : Chaîne JSON contenant les arguments
  - Extra `EXTRA_REQUEST_ID` : Identifiant de requête
  - Extra `EXTRA_SENDER` : Package de l'émetteur
- **Réponse du module** : Intent avec action `com.hephaistos.action.RESULT`
  - Extra `EXTRA_RESPONSE_JSON` : Réponse structurée (statut, logs, données)

### 4. Directives d'Interface Unifiées
Les applications futures s'appuient sur l'interface fournie par Hephaistos :
- Accueil minimaliste
- Liste verticale d'options
- Cartes d'options simples avec icône et libellé
- File picker popup intégré
- Bascule liste/grille
- Toggles pour les options binaires
- Menus déroulants épurés
- Modales centrées minimales
- Suivi de la progression dans les notifications Android
- Thème dynamique respecté (Sombre / Clair / Système)
- Intégration transparente avec le terminal unifié

---

## 🚀 Compilation & Déploiement

Ce projet est prêt à être compilé avec **GitHub Actions** via le workflow [`.github/workflows/build.yml`](.github/workflows/build.yml).

### Compilation locale
```bash
gradle assembleDebug
```
L'APK généré sera disponible dans `app/build/outputs/apk/debug/app-debug.apk`.
