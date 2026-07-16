# Despliegue Continuo con Docker – InkaPark

## 1. Introducción

El despliegue continuo (CD) de InkaPark utiliza **Docker** y **GitHub Actions** para automatizar la construcción, empaquetado y publicación de la aplicación como imagen de contenedor. Esto garantiza que cada cambio en el código fuente se transforme automáticamente en una imagen desplegable, eliminando errores manuales y reduciendo el tiempo de entrega.

---

## 2. Arquitectura del Despliegue

```
┌─────────────┐    push     ┌──────────────────┐    build    ┌─────────────────┐
│  Desarrolla  │ ──────────► │  GitHub Actions   │ ──────────► │  Imagen Docker   │
│  (Git push)  │             │  (CI/CD Pipeline) │             │  (Empaquetada)   │
└─────────────┘             └──────────────────┘             └────────┬────────┘
                                                                      │
                                                                      ▼ push
                                                           ┌─────────────────┐
                                                           │   Docker Hub     │
                                                           │  (Registry)      │
                                                           └────────┬────────┘
                                                                      │
                                                                      ▼ pull
                                                           ┌─────────────────┐
                                                           │  Cualquier       │
                                                           │  servidor/PC     │
                                                           └─────────────────┘
```

---

## 3. Componentes del Pipeline

### 3.1 Dockerfile (Multi-stage Build)

Ubicación: `Dockerfile` en la raíz del proyecto

```dockerfile
# ETAPA 1: Compilar la aplicación
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn dependency:go-offline -q
COPY src src
RUN mvn package -DskipTests -q

# ETAPA 2: Ejecutar la aplicación
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Explicación de cada línea:**

| Línea | Descripción |
|-------|-------------|
| `FROM maven:3.9-eclipse-temurin-21-alpine AS build` | Usa una imagen con Maven y JDK 21 como etapa de compilación |
| `WORKDIR /build` | Define `/build` como directorio de trabajo |
| `COPY pom.xml ./` | Copia el archivo de dependencias Maven |
| `RUN mvn dependency:go-offline -q` | Descarga todas las dependencias (sin mostrar logs) |
| `COPY src src` | Copia el código fuente de la aplicación |
| `RUN mvn package -DskipTests -q` | Compila y genera el archivo JAR |
| `FROM eclipse-temurin:21-jre-alpine` | Segunda etapa: solo JRE (sin Maven, más ligera) |
| `WORKDIR /app` | Directorio de trabajo para la imagen final |
| `COPY --from=build /build/target/*.jar app.jar` | Copia el JAR generado desde la etapa anterior |
| `EXPOSE 8080` | Expone el puerto 8080 |
| `ENTRYPOINT ["java", "-jar", "app.jar"]` | Comando para iniciar la aplicación |

### 3.2 GitHub Actions Workflow

Ubicación: `.github/workflows/docker-publish.yml`

```yaml
name: Publish Docker Image to Docker Hub

on:
  push:
    branches: [ "main" ]
    tags: [ 'v*.*.*' ]
  workflow_dispatch:

env:
  REGISTRY: docker.io
  IMAGE_NAME: inkapark
  IMAGE_TAG: latest

jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log into registry ${{ env.REGISTRY }}
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Debug
        run: |
          echo "github.repository: ${{ github.repository }}"
          echo "env.REGISTRY: ${{ env.REGISTRY }}"
          echo "github.sha: ${{ github.sha }}"
          echo "env.IMAGE_NAME: ${{ env.IMAGE_NAME }}"

      - name: Build and push Docker image
        id: build-and-push
        uses: docker/build-push-action@v6
        with:
          context: .
          push: true
          tags: ${{ env.REGISTRY }}/${{ secrets.DOCKERHUB_USERNAME }}/${{ env.IMAGE_NAME }}:${{ env.IMAGE_TAG }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
```

**Explicación de cada paso del workflow:**

| Paso | Acción utilizada | Descripción |
|------|------------------|-------------|
| Checkout repository | `actions/checkout@v4` | Descarga el código del repositorio en el runner |
| Set up Docker Buildx | `docker/setup-buildx-action@v3` | Configura el builder avanzado de Docker para caché y multi-arquitectura |
| Log into registry | `docker/login-action@v3` | Se autentica en Docker Hub usando los secrets configurados |
| Debug | Comando shell | Imprime variables de entorno para verificación en logs |
| Build and push | `docker/build-push-action@v6` | Construye la imagen desde el Dockerfile y la sube a Docker Hub |

---

## 4. Configuración de Secrets

Los secrets son credenciales almacenadas de forma segura en GitHub. **Nunca** se almacenan directamente en el código.

### 4.1 Secrets requeridos

| Secret | Descripción | Cómo obtenerlo |
|--------|-------------|----------------|
| `DOCKERHUB_USERNAME` | Usuario de Docker Hub | [hub.docker.com](https://hub.docker.com) → tu perfil |
| `DOCKERHUB_TOKEN` | Token de acceso | Docker Hub → Account Settings → Security → New Access Token |

### 4.2 Pasos para configurar los secrets

1. Ir a GitHub → Repositorio → **Settings**
2. Sección **Security** → **Secrets and variables** → **Actions**
3. Clic en **New repository secret**
4. Crear `DOCKERHUB_USERNAME` con el usuario de Docker Hub
5. Crear `DOCKERHUB_TOKEN` con el token generado

---

## 5. Flujo de Ejecución

### 5.1 Flujo automático (CD)

```
1. Desarrollador hace push a main
   └─► git push origin main

2. GitHub Actions detecta el cambio
   └─► Ejecuta docker-publish.yml

3. Pipeline ejecuta los pasos:
   ├─► Checkout: clona el código
   ├─► Buildx: configura el builder
   ├─► Login: se autentica en Docker Hub
   ├─► Debug: muestra variables en logs
   └─► Build & Push: construye y sube la imagen

4. Imagen disponible en Docker Hub
   └─► docker.io/reichelsaa/inkapark:latest
```

### 5.2 Flujo manual (ejecución desde Docker Hub)

```bash
# Descargar la imagen
docker pull reichelsaa/inkapark:latest

# Ejecutar la aplicación
docker run -p 8080:8080 reichelsaa/inkapark:latest

# Acceder en el navegador
# http://localhost:8080
```

---

## 6. Resultados Esperados

### 6.1 Workflow exitoso

En la pestaña **Actions** de GitHub se muestra:

```
✅ Set up job
✅ Checkout repository
✅ Set up Docker Buildx
✅ Log into registry docker.io
✅ Debug
✅ Build and push Docker image
```

### 6.2 Imagen publicada en Docker Hub

```
Docker Hub → Repositories → reichelsaa/inkapark
├── Tag: latest
├── Último push: hace X minutos
└── Tamaño: ~200MB (imagen Alpine ligera)
```

### 6.3 Ejecución de la imagen

```bash
$ docker run -p 8080:8080 reichelsaa/inkapark:latest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.5.6)

Started InkaparkApplication in 3.2 seconds
```

---

## 7. Ventajas del Despliegue con Docker

| Ventaja | Descripción |
|---------|-------------|
| **Portabilidad** | La imagen corre en cualquier sistema operativo con Docker |
| **Consistencia** | Mismo comportamiento en desarrollo, pruebas y producción |
| **Aislamiento** | La app no afecta ni es afectada por otras aplicaciones del servidor |
| **Escalabilidad** | Se pueden crear múltiples instancias de la misma imagen |
| **Rollback** | Se puede volver a una versión anterior simplemente usando otro tag |
| **Rapidez** | El despliegue toma segundos, no horas de configuración manual |

---

## 8. Comandos Útiles de Docker

```bash
# Ver imágenes locales
docker images

# Ejecutar en segundo plano
docker run -d -p 8080:8080 --name inkapark reichelsaa/inkapark:latest

# Ver contenedores en ejecución
docker ps

# Detener el contenedor
docker stop inkapark

# Ver logs
docker logs inkapark

# Eliminar contenedor
docker rm inkapark

# Eliminar imagen
docker rmi reichelsaa/inkapark:latest
```

---

## 9. Troubleshooting (Solución de problemas)

| Error | Causa | Solución |
|-------|-------|----------|
| `Username and password required` | Secrets no configurados | Verificar `DOCKERHUB_USERNAME` y `DOCKERHUB_TOKEN` en Settings → Secrets |
| `permission denied` | Token sin permisos | Generar nuevo token con permisos de Read & Write |
| `manifest unknown` | Imagen no existe | Verificar que el push se completó exitosamente en Docker Hub |
| `port already in use` | Puerto 8080 ocupado | Cambiar puerto: `docker run -p 9090:8080 ...` |

---

## 10. Conclusión

El despliegue continuo con Docker y GitHub Actions permite a InkaPark:

- Automatizar la construcción y publicación de la aplicación
- Garantizar que cada versión esté empaquetada de forma consistente
- Facilitar la distribución y ejecución en cualquier entorno
- Reducir errores humanos en el proceso de despliegue

Este pipeline es la base para futuras mejoras como despliegue en la nube (AWS, Azure, GCP) y orquestación con Kubernetes.

---

## 11. Ejecución Local con Docker

### 11.1 Requisitos previos

| Componente | Versión mínima | Verificar con |
|------------|----------------|---------------|
| Docker Desktop | 4.0+ | `docker --version` |
| Puerto 8080 | Disponible | `netstat -ano | findstr :8080` |
| Puerto 3307 | Disponible | `netstat -ano | findstr :3307` |

### 11.2 Arquitectura local

```
┌─────────────────────────────────────────────────┐
│  Tu PC (localhost)                               │
│                                                  │
│  ┌──────────────┐       ┌────────────────────┐   │
│  │ InkaPark     │ ─────►│ MySQL              │   │
│  │ Puerto 8080  │       │ Puerto 3307        │   │
│  └──────────────┘       └────────────────────┘   │
│         │                        │               │
│         ▼                        ▼               │
│  http://localhost:8080    Contenedores Docker     │
└─────────────────────────────────────────────────┘
```

### 11.3 Paso 1: Construir la imagen Docker

```bash
# Ubicarse en la raíz del proyecto
cd Inkapark

# Construir la imagen
docker build -t inkapark:latest .
```

**Explicación del comando:**

| Parte | Descripción |
|-------|-------------|
| `docker build` | Comando para construir una imagen Docker |
| `-t inkapark:latest` | Etiqueta (tag) de la imagen: nombre:versión |
| `.` | Contexto de build (directorio actual donde está el Dockerfile) |

**Resultado esperado:**
```
#17 exporting to image
#17 naming to docker.io/library/inkapark:latest done
#17 DONE 1.8s
```

### 11.4 Paso 2: Levantar MySQL en Docker

```bash
docker run -d --name mysql-inkapark -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=inkapark -p 3307:3306 mysql:8.0
```

**Explicación de cada parámetro:**

| Parámetro | Descripción |
|-----------|-------------|
| `-d` | Ejecuta el contenedor en segundo plano (detached mode) |
| `--name mysql-inkapark` | Nombre identificador del contenedor |
| `-e MYSQL_ROOT_PASSWORD=root` | Contraseña del usuario root de MySQL |
| `-e MYSQL_DATABASE=inkapark` | Crea la base de datos automáticamente al iniciar |
| `-p 3307:3306` | Mapea puerto 3307 de tu PC al 3306 del contenedor |
| `mysql:8.0` | Imagen oficial de MySQL versión 8.0 |

**Nota:** Se usa puerto 3307 porque el 3306 puede estar ocupado por una instalación local de MySQL.

**Verificar que MySQL está corriendo:**
```bash
docker ps --filter "name=mysql-inkapark"
```

### 11.5 Paso 3: Ejecutar InkaPark

```bash
docker run -d --name inkapark-app -p 8080:8080 -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3307/inkapark?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima" -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=root inkapark:latest
```

**Explicación de cada parámetro:**

| Parámetro | Descripción |
|-----------|-------------|
| `-d` | Ejecuta en segundo plano |
| `--name inkapark-app` | Nombre del contenedor |
| `-p 8080:8080` | Puerto 8080 de tu PC → 8080 del contenedor |
| `-e SPRING_DATASOURCE_URL` | URL de conexión a MySQL (sobreescribe application.properties) |
| `-e SPRING_DATASOURCE_USERNAME` | Usuario de MySQL |
| `-e SPRING_DATASOURCE_PASSWORD` | Contraseña de MySQL |
| `inkapark:latest` | Imagen a ejecutar |

**¿Qué es `host.docker.internal`?**
Es un DNS especial de Docker que permite que un contenedor acceda a servicios que corren en tu PC (host). En este caso, MySQL está corriendo en el puerto 3307 de tu PC, y el contenedor lo necesita en el 3306.

### 11.6 Paso 4: Verificar que funciona

```bash
# Ver logs de la aplicación
docker logs inkapark-app

# Buscar esta línea en los logs:
# Started InkaparkApplication in X.XXX seconds
```

**Abrir en el navegador:**
```
http://localhost:8080
```

### 11.7 Comandos de gestión

```bash
# Ver contenedores en ejecución
docker ps

# Ver logs en tiempo real
docker logs -f inkapark-app

# Detener InkaPark
docker stop inkapark-app

# Detener MySQL
docker stop mysql-inkapark

# Reiniciar InkaPark
docker restart inkapark-app

# Eliminar contenedores
docker rm -f inkapark-app mysql-inkapark

# Ver imágenes Docker locales
docker images
```

### 11.8 Solución de problemas

| Error | Causa | Solución |
|-------|-------|----------|
| `port already in use` | Puerto 8080 o 3307 ocupado | Cambiar puerto: `-p 9090:8080` |
| `connection refused` | MySQL no está listo | Esperar 30 segundos o verificar `docker ps` |
| `Access denied for user` | Credenciales incorrectas | Verificar variables `-e SPRING_DATASOURCE_*` |
| `Unknown database` | Base de datos no existe | Verificar `-e MYSQL_DATABASE=inkapark` |
| `Container already exists` | Contenedor con ese nombre ya existe | `docker rm -f inkapark-app` y volver a ejecutar |
