# InkaPark 🎢

**InkaPark** es una plataforma integral para la gestión de un parque de atracciones inspirado en la cultura inca peruana.  
Desarrollada con **Java 21**, **Spring Boot 3.2.0**, **Hibernate 6.2.7.Final**, y un front-end responsive con **Thymeleaf**, combina funcionalidades de administración, gestión de usuarios, venta de boletos y experiencias culturales interactivas.

---

## ⚡ Funcionalidades Principales

### 🎫 Gestión de Boletos y Aforo
- Compra de boletos online con **tarjeta, Yape o Plin**.
- Generación automática de boletas en **PDF**.
- Envío de comprobantes de pago por **email**.
- Control de aforo por fecha de evento.
- Validación de entradas mediante **lector de códigos QR o barras**.

### 👤 Gestión de Usuarios y Roles
- Registro e inicio de sesión.
- Roles de **Usuario** y **Administrador**.
- Gestión de perfiles y privilegios.

### 🛠 Administración y CRUD
- Panel de administración para:
  - Gestionar usuarios registrados.
  - Responder mensajes de formulario de contacto.
  - Visualizar y gestionar **tickets**.
- Validación de entradas al parque con escaneo de boletos.

### 🌐 Publicidad y Experiencia
- Sitio web público que muestra atracciones y experiencias culturales.
- Integración de imágenes y contenido histórico de la cultura Inca.

---

## 🏗 Arquitectura del Proyecto
InkaPark/
├─ src/main/java/com/example/inkapark
│ ├─ controller # Controladores REST y web
│ ├─ modelo # Entidades JPA
│ ├─ repositorio # Repositorios Spring Data JPA
│ ├─ servicio # Lógica de negocio
│ └─ config # Configuraciones y seguridad
├─ src/main/resources
│ ├─ templates # HTML Thymeleaf
│ ├─ static # CSS, JS, imágenes
│ └─ application.properties
└─ pom.xml # Dependencias y configuración Maven

---

## 🏃‍♂️ Instalación y Ejecución
**1: Clona el repositorio:**

git clone https://github.com/tu-user/inkapark.git
cd inkapark

**2: Configura la base de datos en src/main/resources/application.properties:**

spring.datasource.url=jdbc:mysql://localhost:3306/inkapark
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD

**3: Ejecuta el proyecto:**

mvn spring-boot:run

**4: Accede en tu navegador:**

http://localhost:8080

## NOTAS
- Todos los pagos son simulados para pruebas.
- Se recomienda usar Java 21 para compatibilidad completa.
- SMTP configurado en application.properties para notificaciones de correo.
- Base de datos debe estar creada
