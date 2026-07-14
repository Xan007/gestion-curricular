<div align="center">

# Sistema de Gestión Curricular

**Plataforma para la administración de planes de estudio, microcurrículos y propuestas académicas de la Universidad de los Llanos**

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![JavaFX](https://img.shields.io/badge/JavaFX-17.0.2-blue?logo=javafx&logoColor=white)](https://openjfx.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-RLS-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Supabase](https://img.shields.io/badge/Supabase-Storage%20%26%20Auth-20C2D8?logo=supabase&logoColor=white)](https://supabase.com/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-green?logo=spring&logoColor=white)](https://docs.spring.io/spring-ai/)
[![Maven](https://img.shields.io/badge/Maven-Multi--module-red?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/Uso-Académico-lightgrey)]()

</div>

---

## Tabla de contenidos

- [Acerca del proyecto](#acerca-del-proyecto)
- [Características principales](#características-principales)
- [Arquitectura del sistema](#arquitectura-del-sistema)
- [Roles y flujo de aprobación](#roles-y-flujo-de-aprobación)
- [Stack tecnológico](#stack-tecnológico)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Requisitos previos](#requisitos-previos)
- [Configuración del entorno](#configuración-del-entorno)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Documentación de la API](#documentación-de-la-api)
- [Asistente con IA](#asistente-con-ia)
- [Pruebas](#pruebas)
- [Recursos y documentación](#recursos-y-documentación)
- [Créditos](#créditos)

---

## Acerca del proyecto

Plataforma desarrollada para la **Universidad de los Llanos (Unillanos)** que **digitaliza la gestión de la estructura curricular**: planes de estudio, microcurrículos y propuestas de creación o modificación de cursos.

Los **docentes** presentan propuestas que recorren la jerarquía académica de la institución —**director de programa → comité → director de escuela → decano**— mediante un flujo de revisión, observaciones y firmas digitales, hasta su aceptación o rechazo.

> Unillanos: institución pública de educación superior, sede principal en la vereda Barcelona (Villavicencio, Meta, Colombia), con sedes adicionales en San Antonio, Emporio y Boquemonte.

---

## Características principales

- **Gestión de programas académicos** y sus planes de estudio (carga masiva mediante Excel/CSV).
- **Administración de cursos** con prerrequisitos, áreas, ciclos y tipos de curso.
- **Propuestas curriculares** con flujo de revisión jerárquico y bitácora de observaciones.
- **Firmas digitales** de directores de programa y de escuela para aceptación final.
- **Carga y almacenamiento de archivos** (microcurrículos, apoyos académicos, resultados) integrados con **Supabase Storage**.
- ** Seguridad basada en roles** (RBAC) con autenticación **JWT** y **Row-Level Security (RLS)** en PostgreSQL.
- **Notificaciones** in-app a los docentes sobre el estado de sus propuestas.
- **Asistente conversacional con IA** contextualizado por rol, integrado con el modelo `meta-llama/llama-4-scout-17b-16e-instruct` a través de **Groq**.
- **Documentación interactiva** de la API con **Swagger/OpenAPI**.
- **Cliente de escritorio** multiplataforma construido con **JavaFX**.

---

## Arquitectura del sistema

El proyecto sigue una arquitectura **cliente-servidor de escritorio (rich client)** multi-módulo de Maven, donde el frontend JavaFX consume directamente la API REST del backend y reutiliza sus dependencias.

```
┌─────────────────────────────────────────────────────────────────┐
│                      Cliente de escritorio                        │
│                        (JavaFX + FXML)                            │
│  Login · MainScreen · ProgramCourses · AdminPlantel · Register   │
└──────────────────────────────┬────────────────────────────────────┘
                               │ HTTP / REST (Jackson, JWT en header)
┌──────────────────────────────▼────────────────────────────────────┐
│                         Backend (Spring Boot)                      │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐  │
│  │ Controller │→ │  Service   │→ │ Repository │→ │  Entities  │  │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │  Security (JWT · RBAC)  ·  AI Agent (Spring AI + Tools)     │ │
│  └──────────────────────────────────────────────────────────────┘ │
└──────────┬──────────────────────────────┬─────────────────────────┘
           │                              │
   ┌───────▼─────────┐          ┌──────────▼──────────┐
   │   PostgreSQL    │          │   Supabase Storage   │
   │   (con RLS)     │          │   (buckets/archivos)│
   └─────────────────┘          └──────────────────────┘
```

**Patrones y estilo arquitectónico**

- **Arquitectura en capas** (Controller → Service → Repository → Entity).
- **DTOs + Mappers** (MapStruct) para desacoplar la API del modelo de persistencia.
- **Seguridad declarativa** con `@PreAuthorize` y un filtro JWT personalizado.
- **Agente IA con *function calling***: el LLM invoca *tools* (`ProgramTools`, `CourseTools`) para consultar datos reales del sistema.
- **Prompt engineering por rol**: el `PromptBuilder` selecciona el prompt del sistema según el rol del usuario.

---

## Roles y flujo de aprobación

### Roles del sistema

| Rol                     | Descripción                                                                  |
| ----------------------- | ---------------------------------------------------------------------------- |
| `ADMIN`                 | Administración del plantel de usuarios y configuración general del sistema. |
| `DECANO`                | Visibilidad total de las propuestas de todos los programas.                  |
| `DIRECTOR_DE_PROGRAMA`  | Revisa propuestas, las aprueba/envía a ajustes y firma para aceptación final. |
| `COMITE_DE_PROGRAMA`    | Revisa propuestas en fase de comité y las aprueba o rechaza.                 |
| `DIRECTOR_DE_ESCUELA`   | Firma (o rechaza) propuestas que están en espera de firmas.                  |
| `DOCENTE`               | Crea propuestas y consulta el estado de las propias.                         |

### Flujo de una propuesta

```
                  DOCENTE crea la propuesta
                            │
                            ▼
              ┌──────────────────────────────┐
              │   EN_REVISION_DIRECTOR        │
              └──────────────┬───────────────┘
                     ACEPTAR  │    AJUSTES (rechazo director)
            ┌────────────────┴───────────────────┐
            ▼                                     ▼
   EN_REVISION_COMITE                       AJUSTES_SOLICITADOS
            │                              (el docente ajusta y reenvía)
            ▼
   ESPERANDO_FIRMAS
            │
            ├── Director de programa firma  ✔
            ├── Director de escuela firma   ✔  ──►  ACEPTADA
            │
            └── Cualquiera rechaza firma    ✗  ──►  RECHAZADA
```

Cada transición registra una **bitácora de observaciones** (`timestamp | rol | usuario | acción | nota`) y envía una **notificación** al docente responsable.

---

## Stack tecnológico

| Capa                | Tecnología                                                                 |
| ------------------- | ------------------------------------------------------------------------- |
| Lenguaje            | Java 17 (frontend) / Java 23 (backend)                                    |
| Backend             | Spring Boot 3.4.4, Spring Web, Spring Data JPA, Spring Security            |
| Inteligencia Artificial | Spring AI 1.0.0 (OpenAI client) · Groq · `llama-4-scout-17b-16e-instruct` |
| Persistencia        | PostgreSQL (con Row-Level Security), Hibernate                            |
| Almacenamiento      | Supabase Storage ( buckets para archivos curriculares )                  |
| Seguridad           | JWT (JJWT 0.11.5), RBAC, RLS de PostgreSQL                                |
| Frontend            | JavaFX 17.0.2, FXML, ControlsFX                                          |
| Mapeo               | MapStruct 1.6.3 + Lombok                                                  |
| Procesamiento Excel | Apache POI 5.4.0                                                         |
| Documentación API   | springdoc-openapi 2.8.6 (Swagger UI)                                     |
| Build               | Maven (multi-módulo)                                                     |

---

## Estructura del repositorio

```
gestion-curricular/
├── pom.xml                       # POM padre multi-módulo
├── backend/                      # API REST + lógica de negocio + agente IA
│   ├── pom.xml
│   ├── readme.md
│   └── src/main/
│       ├── java/org/unisoftware/gestioncurricular/
│       │   ├── GestionCurricularApplication.java
│       │   ├── controller/        # Controladores REST (y subcarpeta files/)
│       │   ├── service/           # Lógica de negocio (y subcarpeta files/)
│       │   ├── repository/        # Repositorios JPA (y subcarpeta files/)
│       │   ├── entity/            # Entidades JPA + converters + files/
│       │   ├── dto/                # Objetos de transferencia de datos
│       │   ├── mapper/            # Mappers MapStruct
│       │   ├── security/          # Filtro JWT, utilidades de seguridad, roles
│       │   ├── agent/             # Configuración del agente IA + tools + prompts
│       │   ├── config/            # SecurityConfig, SwaggerConfig, Supabase...
│       │   ├── exception/         # Manejador global de excepciones
│       │   └── util/              # Enums, parsers de Excel/CSV, helpers
│       └── resources/
│           ├── application.properties
│           ├── prompts/           # Prompts del sistema por rol (IA)
│           └── img/
├── frontend/                     # Cliente de escritorio JavaFX
│   ├── pom.xml
│   └── src/main/
│       ├── java/org/unisoftware/gestioncurricular/frontend/
│       │   ├── JavaFXApplication.java   # Entry point (arranque Spring + JavaFX)
│       │   ├── controller/              # Controladores FXML
│       │   ├── service/                 # Clientes REST del backend
│       │   ├── dto/                     # DTOs del frontend
│       │   └── util/                     # SessionManager, JwtDecodeUtil
│       └── resources/
│           ├── fxml/               # Vistas FXML (Login, MainScreen, Register, ...)
│           ├── css/                # Hojas de estilo
│           └── img/
└── Documentacion Gestion Curricular/
    └── Gestion Curricular Documentos y Diagramas/
        ├── Arquitecura del sw/       # Diagrama de arquitectura
        ├── Modelamiento del sw/      # UML, mockups, modelo de datos
        ├── Documentos/               # Requerimientos IEEE 830, cronograma, etc.
        ├── Plantillas/
        └── Puebas/
```

---

## Requisitos previos

- **JDK 17** o superior (backend compila con Java 23; el frontend requiere Java 17 por JavaFX 17).
- **Apache Maven** 3.8+ (o usar el wrapper `mvnw` incluido).
- **PostgreSQL** con soporte para **Row-Level Security** y tipos personalizados (`estado_propuesta`).
- Una cuenta y proyecto en **Supabase** (URL, anon key, service role key y JWT secret).
- Una **API key de Groq** para el asistente con IA.

---

## Configuración del entorno

El backend lee las credenciales de un archivo `.env` en la raíz del proyecto `backend/`. Crea el archivo `backend/.env` con las siguientes variables:

```dotenv
DATABASE_URL=jdbc:postgresql://<host>:<port>/<database>?user=<user>&password=<password>
DATABASE_USERNAME=<user>
DATABASE_PASSWORD=<password>

SUPABASE_URL=https://<project>.supabase.co
SUPABASE_ANON_KEY=<anon_key>
SUPABASE_SERVICE_ROLE_KEY=<service_role_key>
SUPABASE_JWT_SECRET=<jwt_secret>

GROQ_API_KEY=<groq_api_key>
```

> El archivo `.env` está ignorado por Git (ver `.gitignore`). **No subas credenciales reales al repositorio.**

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd gestion-curricular
```

### 2. Construir los módulos (desde la raíz del proyecto)

```bash
./mvnw clean install
```

> En Windows usa `.\mvnw.cmd` en lugar de `./mvnw`.

### 3. Ejecutar el backend (API REST)

```bash
cd backend
./mvnw spring-boot:run
```

El servidor quedará disponible en **http://localhost:8080**.

### 4. Ejecutar el cliente de escritorio (JavaFX)

```bash
cd frontend
./mvnw javafx:run
```

Se abrirá la ventana de **inicio de sesión** del cliente de escritorio.

### (Opcional) Generar el JAR ejecutable del frontend

```bash
cd frontend
./mvnw package
java -jar frontend/target/frontend-0.0.1-SNAPSHOT.jar
```

---

## Documentación de la API

Una vez el backend esté corriendo, la documentación interactiva está disponible en:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Los endpoints principales agrupados por recurso:

| Recurso        | Base path       | Descripción                                             |
| -------------- | --------------- | ------------------------------------------------------ |
| Auth           | `/auth`         | Autenticación y emisión de JWT.                         |
| Users          | `/users`        | Gestión de usuarios y roles.                            |
| Programs       | `/programs`     | Programas académicos y planes de estudio.              |
| Courses        | `/courses`      | Cursos, prerrequisitos y malla curricular.             |
| Proposals      | `/proposals`     | Propuestas y su flujo de revisión/firma.               |
| Notifications  | `/notifications`| Notificaciones in-app para los usuarios.               |
| Files          | `/files/**`     | Carga y gestión de archivos en Supabase Storage.       |
| AI             | `/ai/generate`  | Asistente conversacional con IA (por rol).             |

> La API usa **JWT (Bearer)** para autenticación. En Swagger, introduce el token en el botón **Authorize**.

---

## Asistente con IA

El sistema incluye un **asistente conversacional** que ayuda a los usuarios a consultar información sobre programas, planes de estudio y cursos.

- Implementado con **Spring AI** (`ChatClient`) y el modelo **`meta-llama/llama-4-scout-17b-16e-instruct`** servido por **Groq**.
- Cuenta con **memoria de conversación** (`MessageWindowChatMemory`, 5 mensajes) parametrizada por `conversationId` (UUID del usuario).
- Usa **function calling** mediante *tools* (`ProgramTools`, `CourseTools`) para consultar datos reales del sistema en lugar de generarlos.
- El **prompt del sistema se construye dinámicamente según el rol** del usuario (`docente`, `decano`, `director de programa`, `director de escuela`, `comité de programa`, o `invitado`), desde archivos en `backend/src/main/resources/prompts/`.

Endpoint:

```
GET /ai/generate?message=<mensaje del usuario>
```

---

## Pruebas

Ejecutar las pruebas del backend:

```bash
cd backend
./mvnw test
```

> El proyecto incluye pruebas unitarias/de integración de Spring Boot en `backend/src/test/`.

---

## Recursos y documentación

La carpeta `Documentacion Gestion Curricular/` contiene los entregables formales del proyecto:

- **Arquitectura del software** — diagrama de arquitectura (`.drawio.pdf`).
- **Modelamiento del software** — diagramas UML, mockups y modelo de datos.
- **Documentos** — análisis de requerimientos (IEEE 830), cronograma/presupuesto y descripción del sistema.
- **Manual técnico** (IEEE 830).
- **Pruebas** y **Plantillas**.

---

## Créditos

**Universidad de los Llanos — Unillanos**
Villavicencio, Meta, Colombia · Sede principal vereda Barcelona.

<div align="center">

Desarrollado con fines académicos para la gestión curricular institucional.

</div>
