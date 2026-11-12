# Spécifications de l'Écosystème Modulaire Hephaistos

> **Standard architectural & IPC v1.0.0**  
> Ce document définit l'ensemble des règles obligatoires pour la création de toute application ou moteur modulaire au sein de l'écosystème **Hephaistos** (outils de portage GSI, déballage et modification de partitions système Android).

---

## 1. Philosophie & Principes Fondamentaux

1. **Autonomie & Modularité Totale** :  
   Chaque application créée est un outil ou un moteur indépendant (`com.hephaistos.<nom_application>`). Aucun outil ne dépend directement du code source d'un autre au moment de la compilation.
2. **Pilotage Inter-Modules & Orchestrateur** :  
   Chaque application peut être commandée via la console du lanceur Hephaistos, par une autre application de l'écosystème, ou par un orchestrateur automatisé.
3. **Zéro Interface Personnalisée Anarchique** :  
   Les futures applications **ne doivent pas inventer leur propre interface graphique**. Elles doivent s'appuyer strictement sur les composants et directives d'interface unifiés fournis et standardisés par Hephaistos.
4. **Auto-Documentation Obligatoire** :  
   Chaque application doit inclure un fichier `manifest.json` et un `README.md` téléchargeables et exposés, décrivant rigoureusement :
   - Ses commandes disponibles.
   - Ses arguments d'entrée (types, requis, optionnels).
   - Son format de sortie en JSON strict.
   - Ses codes d'erreur et statuts.

---

## 2. Nomenclatures et Packages

Toutes les applications de l'écosystème doivent impérativement respecter la convention de nommage suivante :

| Type de Composant | Convention de Package | Exemple |
| :--- | :--- | :--- |
| **Hôte & Lanceur** | `com.hephaistos.launcher` | Lanceur Hephaistos |
| **Moteur d'Extraction** | `com.hephaistos.unpacker` | Déballeur sparse / erofs / ext4 |
| **Patcher de GSI** | `com.hephaistos.patcher` | Outil d'injection de fix treble |
| **Générateur d'Overlays** | `com.hephaistos.overlay` | Créateur d'overlays RRO vendor |
| **Gestionnaire Sparse/Raw** | `com.hephaistos.imgtool` | Conversion simg2img / img2simg |

---

## 3. Protocole de Communication (IPC)

Les communications s'effectuent via des intents Android découplés avec des charges utiles structurées en JSON.

### 3.1. Émission d'une commande (`com.hephaistos.action.EXECUTE`)

Pour envoyer une commande à un module, l'émetteur (Hephaistos ou un autre module) diffuse un Intent ciblé ou avec action explicite :

```kotlin
val intent = Intent("com.hephaistos.action.EXECUTE").apply {
    setPackage("com.hephaistos.<target_module>")
    putExtra("EXTRA_COMMAND", "unpack")
    putExtra("EXTRA_PAYLOAD", """{"input_path":"/sdcard/system.img","output_dir":"/sdcard/out/"}""")
    putExtra("EXTRA_REQUEST_ID", "req_987654321")
    putExtra("EXTRA_SENDER", "com.hephaistos.launcher")
}
context.sendBroadcast(intent)
```

### 3.2. Réponse d'un module (`com.hephaistos.action.RESULT`)

Le module récepteur traite la commande et diffuse le résultat :

```kotlin
val resultIntent = Intent("com.hephaistos.action.RESULT").apply {
    setPackage(senderPackage) // ex: "com.hephaistos.launcher"
    putExtra("EXTRA_REQUEST_ID", requestId)
    putExtra("EXTRA_STATUS", "SUCCESS") // "SUCCESS" ou "ERROR"
    putExtra("EXTRA_RESPONSE_JSON", """
        {
          "status": "SUCCESS",
          "totalPartitions": 4,
          "partitions": ["system", "vendor", "product", "odm"],
          "executionTimeMs": 1420
        }
    """.trimIndent())
}
context.sendBroadcast(resultIntent)
```

### 3.3. Schéma JSON universel de réponse

Toute commande exécutée par un module DOIT renvoyer un objet JSON respectant le format suivant :

```json
{
  "status": "SUCCESS",
  "command": "unpack",
  "requestId": "req_987654321",
  "timestamp": 1731370000000,
  "data": {
    "outputFiles": ["/sdcard/out/system.raw"],
    "checksum": "a1b2c3d4..."
  },
  "logs": [
    "[INFO] Magic header verified: 0xed26ff3a",
    "[INFO] Decompressing chunks: 100%"
  ],
  "error": null
}
```

En cas d'erreur :

```json
{
  "status": "ERROR",
  "command": "unpack",
  "requestId": "req_987654321",
  "timestamp": 1731370000000,
  "data": null,
  "logs": [
    "[ERROR] Invalid sparse image magic header: 0x00000000"
  ],
  "error": {
    "code": "ERR_INVALID_HEADER",
    "message": "Fichier image corrompu ou format non supporté"
  }
}
```

---

## 4. Règles d'Interface Graphique (UI / UX) Hephaistos

Toutes les applications de l'écosystème doivent s'appuyer sur la charte graphique et les composants standards fournis par Hephaistos :

1. **Accueil Minimaliste** :
   - Écran épuré, centré sur la tâche principale sans surcharge cognitive.
2. **Options en Liste Verticale** :
   - Configuration claire sous forme de liste défilante.
3. **Cartes d'Option Simples** :
   - Carte Material 3 (`ElevatedCard` ou `OutlinedCard`) avec nom de l'option, brève description et icône vectorielle à gauche.
4. **Gestionnaire de Fichiers Intégré (File Picker)** :
   - Champ de saisie avec bouton d'exploration ouvrant le gestionnaire de fichiers en popup/dialogue modal.
5. **Basculement Liste / Grille** :
   - Les affichages de partitions ou fichiers supportent un basculement simple entre vue liste et vue grille.
6. **Boutons Toggle (Switch)** :
   - Pour les options booléennes (ex: activer la conversion sparse, compression zstd, injection root).
7. **Menus Déroulants Épurés (Dropdown)** :
   - `ExposedDropdownMenuBox` Material 3 pour les choix finis (ex: type de partition, architecture ARM64/x86_64).
8. **Modales Minimales (Dialogs)** :
   - Fenêtres pop-up centrées avec validation/annulation distincte.
9. **Intégration des Progressions dans les Notifications** :
   - Toute tâche longue (> 2s) doit mettre à jour une notification persistante avec barre de progression (`NotificationCompat.Builder.setProgress`).
10. **Thème Dynamique Unifié** :
    - Respect strict des 3 modes : Clair, Sombre, ou Adapté au système.
11. **Accès Console / Terminal** :
    - Toute application peut envoyer ses logs et recevoir ses ordres via le terminal unifié de Hephaistos.

---

## 5. Fichier Manifeste du Module (`manifest.json`)

Chaque application doit placer à la racine de ses assets ou exposer via un ContentProvider son fichier `manifest.json`.

Exemple :

```json
{
  "$schema": "https://hephaistos.dev/schemas/module-v1.json",
  "moduleId": "com.hephaistos.patcher",
  "name": "Hephaistos GSI Patcher",
  "version": "1.0.0",
  "description": "Moteur d'injection de correctifs Treble et overlays",
  "commands": [
    {
      "name": "patch_treble",
      "description": "Applique les correctifs de luminosité, audio et bluetooth pour GSI AOSP",
      "parameters": [
        { "name": "system_dir", "type": "string", "required": true },
        { "name": "target_soc", "type": "string", "required": false, "default": "generic" }
      ],
      "outputFormat": {
        "status": "string",
        "patchedFiles": "array of strings"
      }
    }
  ]
}
```

---

## 6. Guide pour les Développeurs et IA

Pour qu'une IA ou un développeur crée un nouveau module :
1. Déclarer le package sous `com.hephaistos.<outil>`.
2. Implémenter un `BroadcastReceiver` écoutant `com.hephaistos.action.EXECUTE`.
3. Valider la commande et les paramètres reçus dans `EXTRA_COMMAND` et `EXTRA_PAYLOAD`.
4. Émettre la réponse JSON sur `com.hephaistos.action.RESULT`.
5. Fournir le fichier `manifest.json` avec la liste complète des commandes.
