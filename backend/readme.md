# Curriculum Management API

API RESTful desarrollada con Spring Boot para gestionar propuestas curriculares universitarias. Permite a los docentes proponer nuevos cursos o modificaciones, los cuales son revisados por directores de programa, comités académicos y decanos.

## Funcionalidades principales

- Gestión de cursos y programas académicos.
- Registro y seguimiento de propuestas curriculares con archivos adjuntos.
- Flujo de revisión y aprobación según jerarquía académica.
- Seguridad basada en roles.
- Row-Level Security (RLS) en PostgreSQL para control de acceso por usuario.
- Autenticación y almacenamiento de archivos integrados con Supabase.
- Documentación interactiva con Swagger.

## Configuración del entorno

Este proyecto utiliza variables de entorno definidas en un archivo `.env` ubicado en la raíz del proyecto.  
Ejemplo de contenido:

DATABASE_URL=jdbc:postgresql://<host>:<port>/<database>?user=<user>&password=<password>  
DATABASE_USERNAME=<user>  
DATABASE_PASSWORD=<password>

SUPABASE_URL=https://<project>.supabase.co  
SUPABASE_ANON_KEY=<anon_key>  
SUPABASE_SERVICE_ROLE_KEY=<service_role_key>  
SUPABASE_JWT_SECRET=<jwt_secret>

## Documentación

La documentación de los endpoints está disponible en:  
**http://localhost:8080/swagger-ui/index.html**

## Estructura del proyecto

- `controller`: controladores REST.
- `service`: lógica de negocio.
- `repository`: interfaces JPA para acceso a datos.
- `dto`: objetos para transferencia de datos.
- `entity`: modelos que representan las entidades del sistema.
- `config`: configuración de seguridad, Supabase y CORS.
