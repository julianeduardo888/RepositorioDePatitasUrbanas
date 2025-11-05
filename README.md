# 🐾 Patitas Urbanas (Proyecto de Aplicación Móvil)

Esta es una aplicación móvil para Android, desarrollada en Kotlin con Jetpack Compose y Firebase. Permite a los usuarios compartir y descubrir consejos, recetas y guarderías para sus mascotas en una comunidad.

## 1. Rama Funcional

La última versión funcional y estable del proyecto se encuentra en la rama: **`main`**

---

## 2. Cómo Instalar y Compilar el Proyecto

Para compilar y ejecutar este proyecto en Android Studio, se requieren los siguientes pasos, ya que el archivo de configuración de Firebase (`google-services.json`) no se incluye en el repositorio por seguridad.

### Prerrequisitos
* Android Studio (Versión Iguana o superior)
* Una cuenta de Firebase
* Un dispositivo o emulador de Android (API 30+)

### Pasos de Instalación
1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/julianeduardo888/RepositorioDePatitasUrbanas.git](https://github.com/julianeduardo888/RepositorioDePatitasUrbanas.git)
    ```
2.  **Crear un Proyecto en Firebase:**
    * Ve a la [Consola de Firebase](https://console.firebase.google.com/) y crea un nuevo proyecto.
    * Registra una nueva aplicación de **Android**.
    * Usa el nombre de paquete: `com.example.aplicacionpatitasurbanas`
3.  **Obtener `google-services.json`:**
    * Descarga el archivo `google-services.json` que Firebase te proporciona.
    * **Mueve** este archivo a la carpeta `/app` de tu proyecto (`AplicacionPatitasUrbanas/app/google-services.json`).
4.  **Habilitar Servicios de Firebase:**
    * En la consola de Firebase, ve a **Authentication** y habilita el proveedor **Email/Contraseña**.
    * Ve a **Firestore Database**, crea una base de datos en modo de **producción** y pega las [Reglas de Seguridad](httpsr://github.com/julianeduardo888/RepositorioDePatitasUrbanas/blob/main/REGLAS_FIREBASE.md) (ver Siguiente Paso).
    * Ve a **Storage** y habilita el almacenamiento.
5.  **Abrir en Android Studio:**
    * Abre el proyecto en Android Studio.
    * Espera a que Gradle sincronice las dependencias.
    * Ejecuta la aplicación (`Shift` + `F10`).

---

## 3. Ayudas Visuales (Capturas de Pantalla)

Aquí se muestra el flujo principal de la aplicación:

| Pantalla | Descripción |
| :--- | :--- |
| ![Inicio](ruta/a/tu/imagen1.png) | Pantalla de inicio de sesión y registro. |
| ![Menú Principal](ruta/a/tu/imagen2.png) | Menú principal con acceso a todas las secciones. |
| ![Lista de Publicaciones](ruta/a/tu/imagen3.png) | Listado de guarderías con botón de mapa. |
| ![Comentarios](ruta/a/tu/imagen4.png) | Sistema de reseñas con calificación por estrellas. |
| ![Mis Publicaciones](ruta/a/tu/imagen5.png) | Edición y borrado de publicaciones propias. |

*(Nota: Deberás reemplazar `ruta/a/tu/imagenX.png` con las rutas reales de las capturas que subas al repositorio).*
