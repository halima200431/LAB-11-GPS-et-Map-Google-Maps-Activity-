# LAB 11 — GPS et Google Maps Activity

## Démo — GeoPulseMap

**GeoPulseMap** est une application Android développée en Java qui affiche une Google Map et permet de suivre la position actuelle de l’utilisateur en temps réel.

L’application demande la permission de localisation, récupère les coordonnées GPS ou réseau, affiche un marker sur la carte et met à jour les informations de position dans une interface personnalisée.


https://github.com/user-attachments/assets/c1279285-73e4-4450-b583-072e05a4a811


---

## Objectif du lab

Ce projet a pour objectif de comprendre l’intégration de Google Maps dans une application Android et l’utilisation des services de localisation.

L’application permet de :

- afficher une carte Google Maps ;
- demander la permission de localisation à l’utilisateur ;
- récupérer la position avec `GPS_PROVIDER` et `NETWORK_PROVIDER` ;
- afficher latitude et longitude ;
- ajouter ou mettre à jour un marker sur la carte ;
- zoomer automatiquement sur la position actuelle ;
- afficher une alerte si la localisation est désactivée ;
- recentrer la carte grâce à un bouton personnalisé.

---

## Technologies utilisées

- Android Studio
- Java
- Google Maps SDK for Android
- LocationManager
- LocationListener
- CardView
- XML Layout
- Gradle Kotlin DSL

---

## Fonctionnalités principales

### Affichage de la carte

L’application utilise une `Google Maps Activity` avec un `SupportMapFragment` pour afficher la carte en plein écran.

### Gestion de la localisation

La localisation est récupérée avec deux providers :

- `GPS_PROVIDER` : plus précis, adapté à l’extérieur ;
- `NETWORK_PROVIDER` : plus rapide, basé sur le Wi-Fi ou le réseau mobile.

### Marker dynamique

Un marker personnalisé est affiché sur la position actuelle de l’utilisateur.  
Dans cette version, le marker est mis à jour à chaque nouvelle position pour garder une carte propre et lisible.

### Interface personnalisée

L’interface contient :

- un panneau supérieur avec le nom de l’application ;
- un panneau inférieur affichant :
  - le statut de localisation ;
  - la latitude ;
  - la longitude ;
  - la source utilisée ;
- un bouton pour recentrer la carte.

---

## Permissions utilisées

Dans le fichier `AndroidManifest.xml`, les permissions suivantes sont nécessaires :

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
