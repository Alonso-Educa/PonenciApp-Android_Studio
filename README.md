# PonenciApp — App Participante (Android / Kotlin)

> Aplicación Android nativa desarrollada en **Kotlin** para que los participantes puedan acceder a eventos de formación educativa, registrar su asistencia mediante **códigos QR** y valorar las ponencias.
>
> 🖥️ ¿Eres organizador? Consulta la app de gestión: [PonenciApp-Flutter](https://github.com/Alonso-Educa/PonenciApp-Flutter)

---

## 📋 Tabla de contenidos

- [Descripción](#-descripción)
- [Características principales](#-características-principales)
- [Tecnologías utilizadas](#-tecnologías-utilizadas)
- [Arquitectura del sistema](#-arquitectura-del-sistema)
- [Capturas de pantalla](#-capturas-de-pantalla)
- [Instalación y configuración](#-instalación-y-configuración)
- [Proyecto relacionado](#-proyecto-relacionado)

---

## 📖 Descripción

PonenciApp es un sistema de gestión de eventos educativos desarrollado como Trabajo de Fin de Grado (DAM). Esta app Android está dirigida a los **participantes** de los eventos: permite consultar el programa, inscribirse en ponencias, registrar asistencia escaneando un código QR y dejar valoraciones.

El backend es compartido con la [app de organizadores (Flutter)](https://github.com/Alonso-Educa/PonenciApp-Flutter), permitiendo que ambas aplicaciones operen en tiempo real sobre los mismos datos.

---

## ✨ Características principales

### Acceso a eventos
- Exploración de eventos disponibles con toda su información
- Inscripción en ponencias individuales dentro de un evento
- Visualización del programa completo con horarios y salas

### Registro de asistencia por QR
- Escaneo de códigos QR generados por el organizador
- Registro automático de asistencia en tiempo real en Firebase
- Historial de asistencias del participante

### Valoraciones
- Puntuación y comentario libre para cada ponencia asistida
- Las valoraciones son visibles para los organizadores en el panel de estadísticas

### Autenticación
- Login con **Google** y **Microsoft** (OAuth 2.0)
- Perfil del participante gestionado en Firebase

### Funcionamiento offline
- Caché local con **Room** para consultar datos sin conexión
- Sincronización automática al recuperar conectividad

---

## 🛠️ Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| **Kotlin** | Lenguaje principal de desarrollo |
| **Android Studio** | Entorno de desarrollo |
| **Firebase Firestore** | Base de datos en tiempo real (NoSQL), compartida con la app Flutter |
| **Firebase Authentication** | Login con Google y Microsoft |
| **Firebase Storage** | Descarga de recursos del evento |
| **Firebase Security Rules** | Control de acceso por rol de usuario |
| **Room (Jetpack)** | Base de datos local para funcionamiento offline |
| **Google Sign-In / MSAL** | Autenticación OAuth 2.0 |
| **ZXing / ML Kit** | Escaneo de códigos QR |
| **Jetpack (ViewModel, LiveData, Navigation)** | Arquitectura MVVM |

---

## 🏗️ Arquitectura del sistema

```
┌─────────────────────────────────────────────────────┐
│                   FIREBASE BACKEND                  │
│  Firestore · Auth · Storage · Hosting · Functions   │
└───────────────────┬─────────────────────────────────┘
                    │ Compartido
        ┌───────────┴────────────┐
        ▼                        ▼
┌───────────────┐        ┌───────────────┐
│  Flutter App  │        │  Android App  │
│ (Organizador) │        │ (Participante)│
│  Web / Móvil  │        │    Kotlin     │
└───────────────┘        └───────────────┘
```

### Patrón arquitectónico

La app Android sigue el patrón **MVVM (Model-View-ViewModel)** con los componentes Jetpack:

```
UI (Activities / Fragments)
        │
   ViewModel + LiveData
        │
   Repository
   ┌────┴────┐
Firebase   Room DB
(remoto)  (local/caché)
```

---

## 📸 Capturas de pantalla

> *Próximamente — se añadirán capturas de la app.*

Para una vista completa de la interfaz y funcionalidades, consulta la [página de ayuda](https://ponenciapp.web.app/ayuda.html).

---

## ⚙️ Instalación y configuración

### Requisitos previos

- [Android Studio](https://developer.android.com/studio) (versión reciente)
- Android SDK (API 26 o superior recomendado)
- Cuenta en [Firebase](https://firebase.google.com/) con el mismo proyecto que usa la app Flutter

### Pasos

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/Alonso-Educa/PonenciApp-Android_Studio.git
   ```

2. **Abrir en Android Studio**

   Abre Android Studio → `Open` → selecciona la carpeta del proyecto.

3. **Configurar Firebase**

   - Ve a [Firebase Console](https://console.firebase.google.com/) y accede al proyecto
   - Descarga el archivo `google-services.json` del proyecto Firebase
   - Colócalo en la carpeta `app/` del proyecto

4. **Sincronizar y ejecutar**
   - Haz clic en `Sync Now` cuando Android Studio lo solicite
   - Conecta un dispositivo Android o inicia un emulador
   - Pulsa `Run` (▶️)

> ⚠️ **Nota:** El archivo `google-services.json` no está incluido en el repositorio por razones de seguridad. Debes usar el mismo proyecto Firebase que la app organizador para que ambas apps compartan datos.

---

## 🔗 Proyecto relacionado

Este repositorio forma parte de un sistema de dos aplicaciones:

| Repositorio | Descripción | Tecnología |
|---|---|---|
| [PonenciApp-Flutter](https://github.com/Alonso-Educa/PonenciApp-Flutter) | App organizador + Panel web | Flutter / Dart |
| **Este repo** | App participante | Kotlin / Android |

---

## 👨‍💻 Autor

Desarrollado como Trabajo de Fin de Grado del ciclo **Desarrollo de Aplicaciones Multiplataforma (DAM)**.
