# Manual de Usuario – InkaPark

## 1. Introducción

**InkaPark** es una plataforma web para la gestión de un parque de atracciones inspirado en la cultura inca peruana. Permite a los usuarios comprar boletos en línea, explorar atracciones y gestionar experiencias culturales interactivas.

### 1.1 Requisitos del sistema

| Componente | Mínimo | Recomendado |
|------------|--------|-------------|
| Navegador web | Chrome 90+, Firefox 88+, Edge 90+ | Chrome último versión |
| Conexión a internet | 1 Mbps | 5 Mbps o más |
| Resolución de pantalla | 1024 x 768 | 1920 x 1080 |

### 1.2 Acceso a la plataforma

```
URL: http://localhost:8080 (local)
URL: https://tu-dominio.com (producción)
```

---

## 2. Módulo de Autenticación

### 2.1 Registro de cuenta

1. Ir a la página de registro: `/auth/register`
2. Completar el formulario:

| Campo | Descripción | Restricción |
|-------|-------------|-------------|
| Nombre completo | Nombre del usuario | Máximo 100 caracteres |
| Correo electrónico | Solo correos @gmail.com | Obligatorio, único |
| Contraseña | Contraseña segura | Máximo 255 caracteres |

3. Clic en **Registrarse**
4. Revisar el correo electrónico para verificar la cuenta
5. Clic en el enlace de verificación recibido

### 2.2 Inicio de sesión

1. Ir a la página de login: `/auth/login`
2. Ingresar correo electrónico y contraseña
3. Clic en **Iniciar Sesión**

**Nota:** Si la cuenta no está verificada, se mostrará un mensaje indicando que se necesita verificar el correo electrónico.

### 2.3 Cierre de sesión

1. Clic en **Cerrar Sesión** en el menú de navegación
2. La sesión se terminate automáticamente

---

## 3. Módulo de Boletos

### 3.1 Comprar boletos

1. Iniciar sesión como usuario
2. Ir a la sección **Tickets**
3. Seleccionar la **fecha del evento** (próximos 7 días disponibles)
4. Seleccionar la **cantidad de boletos** (máximo según aforo disponible)
5. Completar los datos de pago:

| Campo | Descripción |
|-------|-------------|
| Número de tarjeta | 16 dígitos (Visa, Mastercard, Amex, Diners) |
| CVV | 3 dígitos |
| Vencimiento | MM/AA |
| Teléfono | Número de contacto |
| Dirección | Dirección de facturación |

6. Clic en **Pagar**
7. Recibir comprobante de pago por correo electrónico
8. Descargar boletos en formato PDF

### 3.2 Métodos de pago aceptados

| Método | Tipo | Descripción |
|--------|------|-------------|
| Visa | Tarjeta de crédito/débito | Empieza con 1 |
| Mastercard | Tarjeta de crédito/débito | Empieza con 2 |
| American Express | Tarjeta de crédito | Empieza con 3 |
| Diners Club | Tarjeta de crédito | Empieza con 4 |

**Nota:** Los pagos son simulados para fines académicos. No se realizan cobros reales.

### 3.3 Estructura del boleto

El boleto PDF contiene:

| Sección | Contenido |
|---------|-----------|
| Cabecera | Logo de InkaPark, tipo de tarjeta utilizada |
| Datos del cliente | Nombre, correo, teléfono, dirección |
| Detalle de la operación | Código de boleta, fecha del evento, cantidad, monto |
| Resumen visual | Total pagado, tickets, fecha del evento |
| Términos y condiciones | Políticas de uso del boleto |

### 3.4 Código de boleta

- Cada boleta tiene un código único alfanumérico de 8 caracteres
- Ejemplo: `23OP2I6M`
- Se usa para validar la entrada al parque

---

## 4. Módulo de Atracciones

### 4.1 Explorar atracciones

1. Ir a la sección **Atracciones** en el menú de navegación
2. Explorar las atracciones disponibles
3. Ver imágenes, descripciones e información histórica

### 4.2 Galería de imágenes

1. Ir a la sección **Galería**
2. Explorar las imágenes del parque
3. Ver detalles de cada atracción

### 4.3 Información cultural

1. Ir a la sección **Nosotros**
2. Conocer la historia y cultura inca
3. Explorar el contenido histórico del parque

---

## 5. Módulo de Contacto

### 5.1 Enviar mensaje de contacto

1. Ir a la sección **Contacto**
2. Completar el formulario:

| Campo | Descripción |
|-------|-------------|
| Nombre | Nombre del remitente |
| Correo electrónico | Correo de contacto |
| Asunto | Tema del mensaje |
| Mensaje | Contenido del mensaje |

3. Clic en **Enviar Mensaje**
4. Recibir confirmación de envío

### 5.2 Respuesta del administrador

- Los administradores revisan y responden los mensajes
- Se recibe notificación por correo electrónico cuando se responde

---

## 6. Panel de Administración

### 6.1 Acceso al panel

1. Ir a `/admin/login`
2. Ingresar credenciales de administrador
3. Clic en **Iniciar Sesión**

### 6.2 Gestión de usuarios

**Ruta:** `/admin/usuarios`

| Función | Descripción |
|---------|-------------|
| Ver lista | Visualizar todos los usuarios registrados |
| Buscar | Filtrar usuarios por nombre o correo |
| Editar | Modificar datos de un usuario |
| Cambiar rol | Asignar rol de CLIENTE o ADMIN |

### 6.3 Gestión de tickets

**Ruta:** `/admin/tickets`

| Función | Descripción |
|---------|-------------|
| Ver tickets | Lista de todos los boletos vendidos |
| Filtrar | Por fecha, estado o usuario |
| Cambiar estado | Marcar como VIGENTE, USADA o CANCELADA |
| Validar entrada | Verificar código de boleta |

### 6.4 Gestión de mensajes de contacto

**Ruta:** `/admin/mensajes`

| Función | Descripción |
|---------|-------------|
| Ver mensajes | Lista de mensajes de contacto |
| Responder | Enviar respuesta al usuario |
| Marcar como leído | Actualizar estado del mensaje |

### 6.5 Control de aforo

**Ruta:** `/admin/aforo`

| Función | Descripción |
|---------|-------------|
| Ver disponibilidad | Aforo por fecha |
| Modificar aforo | Cambiar capacidad máxima |
| Estadísticas | Ventas por día, ingresos |

---

## 7. Estados de los Boletos

| Estado | Color | Descripción |
|--------|-------|-------------|
| VIGENTE | Verde | Boleto válido para uso |
| USADA | Azul | Boleto ya fue escaneado en entrada |
| CANCELADA | Rojo | Boleto cancelado (se devolvió aforo) |

---

## 8. Estructura de la Aplicación

```
InkaPark/
├── Páginas públicas
│   ├── Inicio (/)
│   ├── Atracciones (/atracciones)
│   ├── Galería (/galeria)
│   ├── Nosotros (/nosotros)
│   └── Contacto (/contacto)
│
├── Autenticación
│   ├── Login (/auth/login)
│   ├── Registro (/auth/register)
│   └── Verificación (/auth/verificar)
│
├── Usuario
│   ├── Tickets (/tickets)
│   └── Compra de boletos (/tickets/pagar)
│
└── Administración
    ├── Panel (/admin)
    ├── Usuarios (/admin/usuarios)
    ├── Tickets (/admin/tickets)
    ├── Mensajes (/admin/mensajes)
    └── Aforo (/admin/aforo)
```

---

## 9. Preguntas Frecuentes

### ¿Puedo cancelar un boleto?
Sí, contactando al administrador. Se devolverá el aforo disponible.

### ¿Puedo comprar boletos sin registrar cuenta?
No, es necesario iniciar sesión para realizar compras.

### ¿Qué hago si olvidé mi contraseña?
Contacta al administrador para restablecerla.

### ¿Los boletos son transferibles?
El boleto está vinculado al usuario que lo compró. Para transferencias, contacta al administrador.

### ¿Cuántos boletos puedo comprar por persona?
Depende del aforo disponible para la fecha seleccionada.

---

## 10. Soporte Técnico

| Canal | Información |
|-------|-------------|
| Correo | inkaparka@gmail.com |
| Formulario | Sección Contacto en la plataforma |

---

## 11. Glosario

| Término | Definición |
|---------|------------|
| **Aforo** | Capacidad máxima de personas permitidas en una fecha específica |
| **Boleta** | Documento digital que da derecho de ingreso al parque |
| **CVV** | Código de seguridad de 3 dígitos en la tarjeta |
| **GHCR** | GitHub Container Registry, registro de contenedores de GitHub |
| **JAR** | Java Archive, formato de empaquetado de aplicaciones Java |
| **PDF** | Portable Document Format, formato de documento portátil |
| **SemVer** | Semantic Versioning, sistema de versionado (v1.2.3) |
| **Tag** | Etiqueta de versión en una imagen Docker |
