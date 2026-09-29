# VitalSafe 
> *Respuesta inmediata, vidas seguras*

VitalSafe es una solución móvil nativa para Android diseñada para optimizar la gestión de emergencias médicas y la disponibilidad inmediata de información clínica en El Salvador. La plataforma integra registro biométrico, reconocimiento automatizado de documentos de identidad, geolocalización en tiempo real y protocolos de autenticación.

---
##  requisitos previos 
**Entorno de desarrollo:** Android Studio Hedgehog (2023.1.1) o superior (Ladybug / Koala recomendado).
- **JDK:** Java 17.
- **Android SDK:**
  - `minSdk`: 24 (Android 7.0 Nougat).
  - `targetSdk` / `compileSdk`: 34.
- **Hardware/Dispositivo:** Dispositivo físico o emulador con soporte para Google Play Services, lector biométrico
---
## instalación y configuración
### 1 clonar el repositorio abrir el repositorio y clonar en la maquina local 
### 2 generar el archivo json en firebase y colocarlo a nivel de proyecto en la carpeta de app

##  Descripción General
VitalSafe aborda los retrasos críticos en la recolección de antecedentes médicos durante situaciones de emergencia mediante:
- **Lectura Inteligente de DUI:** Extracción DE dde dui con Google ML Kit y CameraX para registrar automáticamente ciudadanos salvadoreños.
- **Seguridad Biométrica:** Uso de sensor de huella para autorizar modificaciones al expediente médico.
- **Acceso Basado en Roles :** Detección automática en Firebase Firestore (`ciudadano`, `paramédico`, `administrador`).
- **Alertas SOS:** Notificaciones de auxilio con coordenadas GPS en tiempo real.

---

##  Arquitectura y Tecnologías
- **Lenguaje:** Kotlin
- **Framework UI:** Jetpack Compose con Material Design 3
- **Arquitectura:** MVVM (Model-View-ViewModel) + Repositorios reactivos
- **Base de Datos y Autenticación:** Firebase Authentication & Cloud Firestore (sincronización con SnapshotListeners)
- **Visión Artificial:** Google ML Kit (Text Recognition Latin)
- **Captura de Imagen:** AndroidX CameraX (Core, Camera2, Lifecycle, View)
- **Seguridad:** AndroidX Biometric API
---


---

##  Dependencias Principales

```kotlin
// UI & Arquitectura
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")
implementation("androidx.fragment:fragment-ktx:1.6.2")
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose:2.7.7")

// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")

// Reconocimiento de Texto y Cámara (DUI Scanner)
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")
implementation("com.google.mlkit:text-recognition:16.0.0")

// Seguridad Biométrica
implementation("androidx.biometric:biometric:1.2.0-alpha05")

---
