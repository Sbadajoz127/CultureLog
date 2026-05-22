<p align="center">
  <img src="https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white" alt="React 19"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4"/>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white" alt="Vite 8"/>
</p>

<h1 align="center">📚 CultureLog</h1>

<p align="center">
  <strong>Tu red social de cultura personal</strong><br/>
  Registra, valora y comparte las películas, series, libros, videojuegos, anime, manga y música que consumes.
</p>

---

## 📖 ¿Qué es CultureLog?

CultureLog es una **red social cultural** que combina el concepto de biblioteca personal de medios (al estilo Letterboxd o Goodreads) con un **feed social** donde puedes compartir, comentar y descubrir lo que otros usuarios están consumiendo.

### Características principales

- **Biblioteca multimedia personal** — Organiza contenido en 7 categorías (películas, series, libros, videojuegos, anime, manga y música) con estados de seguimiento, valoraciones, notas y etiquetas personalizadas.
- **Búsqueda en catálogos externos** — Busca en TMDB, Open Library, Jikan, RAWG y Deezer; añade resultados a tu biblioteca con un clic.
- **Feed social** — Publica posts vinculados a elementos de tu biblioteca, sigue a otros usuarios y descubre nuevo contenido.
- **Interacciones sociales** — Likes, guardados, comentarios con hilos de respuestas y notificaciones en tiempo real.
- **Perfiles personalizables** — Avatar, banner, tema oscuro/claro, color de acento personalizado y controles de privacidad.
- **Panel de administración** — Moderación de usuarios, posts, comentarios e ítems con estadísticas y gráficas.

---

## 🏗️ Arquitectura

El proyecto es un **monorepo** con dos aplicaciones principales:

```
CultureLog/
├── backend/CultureLog/          # API REST (Spring Boot + Java 21)
│   ├── src/main/java/...        # Controladores, servicios, modelos, DTOs
│   ├── src/main/resources/      # application.properties
│   └── pom.xml                  # Dependencias Maven
│
└── CultureLogReact/CultureLog/  # SPA (React 19 + Vite)
    ├── src/
    │   ├── components/          # Componentes reutilizables
    │   ├── pages/               # Páginas de la aplicación
    │   ├── context/             # Providers (Auth, Theme, Notificaciones)
    │   ├── services/            # Cliente API (Axios)
    │   └── App.jsx              # Rutas y estilos
    ├── package.json
    └── vite.config.js           # Proxy dev → backend
```

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|------|------------|
| **Frontend** | React 19, Vite 8, React Router 7, Axios, Lucide Icons, Sonner, Recharts |
| **Backend** | Spring Boot 4.0, Java 21, Spring Security, Spring Data JPA, Spring Mail |
| **Base de datos** | MySQL |
| **Autenticación** | JWT (stateless) + BCrypt |
| **Almacenamiento de imágenes** | Cloudinary |
| **Email** | Gmail SMTP |
| **Caché** | Caffeine |
| **APIs externas** | TMDB, Open Library, Jikan, RAWG, Deezer |

---

## 🚀 Instalación y configuración

### Requisitos previos

- **Java 21** (JDK)
- **Maven** (o usa el wrapper incluido `mvnw`)
- **Node.js** (v18+) y **npm**
- **MySQL** (servidor en ejecución)
- Cuenta de **Cloudinary**
- Credenciales **Gmail** (contraseña de aplicación)
- API Keys: **TMDB** y **RAWG**

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/CultureLog.git
cd CultureLog
```

### 2. Configurar la base de datos

Crea una base de datos MySQL vacía. Hibernate se encargará de crear las tablas automáticamente:

```sql
CREATE DATABASE culturelog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar variables de entorno (Backend)

Crea el archivo `backend/CultureLog/.env` con el siguiente contenido:

```properties
# Base de datos
DB_URL=jdbc:mysql://localhost:3306/culturelog
DB_USERNAME=tu_usuario
DB_PASSWORD=tu_contraseña

# JWT (mínimo 32 caracteres)
JWT_SECRET=tu-clave-secreta-de-al-menos-32-caracteres

# Email (Gmail SMTP)
MAIL_USERNAME=tu_correo@gmail.com
MAIL_PASSWORD=tu_contraseña_de_aplicacion

# Cloudinary
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret

# APIs externas
TMDB_API_KEY=tu_clave_tmdb
RAWG_API_KEY=tu_clave_rawg

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173

# Usuario administrador (opcional, se crea al iniciar)
ADMIN_USERNAME=admin
ADMIN_PASSWORD=tu_contraseña_admin
ADMIN_EMAIL=admin@tudominio.com
```

### 4. Iniciar el backend

```bash
cd backend/CultureLog
./mvnw spring-boot:run
```

> En Windows usa `mvnw.cmd spring-boot:run`

El servidor arranca en **http://localhost:8080**.

### 5. Iniciar el frontend

```bash
cd CultureLogReact/CultureLog
npm install
npm run dev
```

La aplicación estará disponible en **http://localhost:5173**.

> El servidor de desarrollo de Vite redirige automáticamente las llamadas `/api/*` al backend.

---

## 🔐 Autenticación

CultureLog utiliza autenticación **JWT stateless**:

1. El usuario se registra y recibe un email de verificación.
2. Tras verificar el email, puede iniciar sesión y obtiene un token JWT (válido 24h).
3. Todas las peticiones autenticadas incluyen el token en el header `Authorization: Bearer <token>`.

### Flujos disponibles

| Flujo | Endpoint |
|-------|----------|
| Registro | `POST /api/auth/register` |
| Verificar email | `POST /api/auth/verify-email` |
| Iniciar sesión | `POST /api/auth/login` |
| Recuperar contraseña | `POST /api/auth/request-reset` |
| Restablecer contraseña | `POST /api/auth/reset-password` |

---

## 📡 API REST

Todos los endpoints se encuentran bajo `/api`. Los principales grupos son:

| Recurso | Ruta base | Descripción |
|---------|-----------|-------------|
| Auth | `/api/auth` | Registro, login y recuperación de contraseña |
| Usuarios | `/api/users` | Perfiles, ajustes, búsqueda de usuarios |
| Posts | `/api/posts` | Feed, publicaciones, likes, guardados, comentarios |
| Biblioteca | `/api/items` | CRUD de ítems de la biblioteca personal |
| Búsqueda | `/api/search` | Búsqueda en APIs externas y añadir a biblioteca |
| Seguimientos | `/api/follows` | Seguir/dejar de seguir, solicitudes pendientes |
| Notificaciones | `/api/notifications` | Listado y marcado de notificaciones |
| Imágenes | `/api/images` | Subida de imágenes a Cloudinary |
| Admin | `/api/admin` | Moderación y estadísticas (solo ADMIN) |

---

## 🎨 Interfaz de usuario

- **Tema oscuro por defecto** con opción de tema claro y seguimiento del sistema.
- **Color de acento personalizable** por usuario.
- **Diseño responsive** con adaptación para móvil y escritorio.
- **Skeleton loaders** durante la carga de contenido.
- **Scroll infinito** en el feed y listados.
- **Vistas de biblioteca** en lista y cuadrícula con filtros por tipo y estado.
- **Interfaz completamente en español**.

---

## 📊 Panel de administración

Accesible en `/admin` para usuarios con rol `ADMIN`:

- Gestión de usuarios (buscar, ver perfil, eliminar).
- Moderación de posts y comentarios.
- Gestión de ítems de biblioteca.
- Estadísticas de la plataforma con gráficas de barras y circulares.

---

## 🏗️ Build de producción

```bash
# Frontend
cd CultureLogReact/CultureLog
npm run build          # Genera archivos estáticos en dist/

# Backend
cd backend/CultureLog
./mvnw package         # Genera JAR ejecutable en target/
```

Para producción, sirve los archivos estáticos del frontend y configura el JAR del backend como servicio con las variables de entorno adecuadas.

---

## 📂 APIs externas utilizadas

| API | Contenido | Documentación |
|-----|-----------|---------------|
| [TMDB](https://www.themoviedb.org/documentation/api) | Películas y series | Requiere API Key |
| [Open Library](https://openlibrary.org/developers/api) | Libros | Sin API Key |
| [Jikan](https://jikan.moe/) | Anime y manga | Sin API Key |
| [RAWG](https://rawg.io/apidocs) | Videojuegos | Requiere API Key |
| [Deezer](https://developers.deezer.com/api) | Música | Sin API Key |

---

## 📄 Licencia

Proyecto desarrollado como **Trabajo de Fin de Grado (TFG)**.

---

<p align="center">
  Hecho con ❤️ por el equipo de CultureLog
</p>
