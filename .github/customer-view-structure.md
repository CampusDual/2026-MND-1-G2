# Prototipo de clientes – Proyecto Crucero

**Documento de referencia (Single Source of Truth)**

Este documento describe cómo es el prototipo de clientes: qué pantallas tiene, qué catálogo maneja, qué datos almacena y qué límites tiene. Es una fuente de información. No contiene instrucciones de desarrollo.

## 1. Objetivo

El prototipo es la interfaz web que utilizan los pasajeros para comunicar incidencias, solicitar productos y servicios para su camarote y consultar el estado de sus peticiones.

La aplicación es visual, sencilla e intuitiva. El pasajero completa una petición seleccionando opciones, sin redactar explicaciones largas.

Alcance inicial: únicamente incidencias y solicitudes relacionadas con el camarote.

## 2. Pantalla principal: Mi camarote

Encabezado:

> ¡Bienvenido a bordo! ¿Qué podemos hacer por ti hoy?

La pantalla ofrece tres botones principales:

| Botón | Descripción | Icono |
| --- | --- | --- |
| Comunicar una incidencia | Tengo un problema o algo no funciona correctamente. | Incidencia |
| Realizar una solicitud | Necesito productos, artículos o un servicio para mi camarote. | Añadir |
| Mis solicitudes | Consultar el estado de peticiones anteriores. | Seguimiento |

El número de camarote y los datos necesarios de identificación se cargan automáticamente a partir de la sesión del pasajero. El pasajero no introduce estos datos manualmente en ninguna solicitud.

## 3. Comunicar una incidencia

### Paso 1. Seleccionar categoría

Título de la pantalla: **¿Qué problema tienes en tu camarote?**

| Categoría | Descripción |
| --- | --- |
| Agua y baño | Grifos, ducha, WC |
| Climatización | Aire acondicionado |
| Electricidad | Luces y enchufes |
| Conexión y TV | Wi-Fi y televisión |
| Mobiliario | Cama, armario y puertas |
| Limpieza | Higiene del camarote |
| Otro problema | Otras incidencias |

### Paso 2. Seleccionar subcategoría

Las opciones cambian dinámicamente según la categoría seleccionada.

**Agua y baño**

- No sale agua.
- No hay agua caliente.
- El grifo pierde agua.
- Hay una fuga.
- La ducha está averiada.
- El WC está atascado.
- La cisterna no funciona.

**Climatización**

- El aire acondicionado no funciona.
- No enfría suficiente.
- No calienta suficiente.
- No puedo regular la temperatura.
- Hace demasiado ruido.

**Electricidad**

- No hay electricidad.
- Una luz no funciona.
- Un enchufe está averiado.
- Un interruptor no funciona.

**Conexión y TV**

- No funciona el Wi-Fi.
- La conexión es lenta.
- La televisión no funciona.
- El mando está averiado.

**Mobiliario**

- La cama está averiada.
- El armario no cierra.
- Un cajón está atascado.
- La puerta está averiada.
- Hay mobiliario dañado.

**Limpieza**

- El camarote no está limpio.
- La limpieza es insuficiente.
- El baño está sucio.
- Hay malos olores.

**Otro problema**

- La categoría existe en el catálogo y está disponible.
- No tiene subcategorías definidas.

Al seleccionar una subcategoría, el pasajero puede continuar al siguiente paso.

## 4. Realizar una solicitud

### Paso 1. Seleccionar categoría

Título de la pantalla: **¿Qué necesitas?**

| Categoría | Descripción |
| --- | --- |
| Toallas y baño | Toallas, alfombrilla, albornoz |
| Ropa de cama | Almohadas, mantas, sábanas |
| Amenities | Gel, champú, jabón y otros |
| Limpieza | Servicios de limpieza adicionales |
| Otros artículos | Artículos disponibles a bordo |

### Paso 2. Seleccionar producto o servicio

Las opciones se muestran dinámicamente según la categoría.

**Toallas y baño**

- Toalla de baño.
- Toalla de mano.
- Alfombrilla de baño.
- Albornoz.

**Ropa de cama**

- Almohada adicional.
- Manta adicional.
- Cambio de sábanas.
- Cambio de funda de almohada.

**Amenities**

- Gel.
- Champú.
- Jabón.
- Papel higiénico.
- Kit dental.
- Otros amenities disponibles.

**Limpieza**

- Limpieza adicional del camarote.
- Repaso del baño.
- Retirada de basura.
- Cambio de papeleras.

**Otros artículos**

- Perchas.
- Bolsas de lavandería.

## 5. Detalles de la solicitud

Una vez seleccionado un producto, la pantalla de detalles contiene:

**¿Cuántas necesitas?**

- La cantidad se controla mediante botones + y −.
- La cantidad inicial es 1.
- La cantidad mínima es 1; no se admiten cantidades inferiores.

**¿Cuándo las necesitas?**

El pasajero elige entre dos opciones:

| Opción | Descripción |
| --- | --- |
| Lo antes posible | Sujeto a disponibilidad y prioridad. |
| En el próximo turno disponible | Según la planificación del equipo. |

**Resumen de la petición**

- Producto seleccionado.
- Cantidad.
- Camarote, asignado automáticamente.
- Momento preferido de entrega.

La pantalla termina con el botón **Enviar solicitud**.

Los campos se adaptan al tipo de solicitud: no todas las peticiones necesitan cantidad ni horario de entrega.

## 6. Confirmación y seguimiento

Tras enviar una incidencia o una solicitud, la aplicación muestra una pantalla de confirmación común.

Mensaje:

> ¡Tu petición ha sido enviada! Podrás consultar su estado desde «Mis solicitudes».

Resumen que acompaña al mensaje:

- Tipo de petición.
- Producto o incidencia seleccionada.
- Camarote.
- Departamento responsable, cuando esté identificado.

La pantalla incluye el botón **Volver al inicio**.

**Estados de una petición**

| Estado | Descripción |
| --- | --- |
| Pendiente | Registrada y a la espera de asignación. |
| Asignada | Un departamento se hace cargo. |
| En proceso | La petición está siendo atendida. |
| Resuelta | La petición ha sido completada. |

## 7. Mis solicitudes

La pantalla «Mis solicitudes» permite al pasajero consultar todas las peticiones realizadas durante su estancia.

Cada petición muestra:

- Nombre de la solicitud o incidencia.
- Fecha de creación.
- Estado actual.
- Departamento responsable, si está asignado.

Desde cada petición se accede a su detalle.

El pasajero solo consulta las peticiones asociadas a su propia estancia.

## 8. Modelo de datos

Las tablas y los campos usan nombres en inglés y snake\_case.

### 8.1. `cabins` (Camarotes)

| Campo | Descripción |
| --- | --- |
| `cabin_id` | Identificador único del camarote. |
| `cabin_number` | Número real del camarote (por ejemplo, 405). |

### 8.2. `request_categories` (Categorías)

| Campo | Descripción |
| --- | --- |
| `category_id` | Identificador único de la categoría. |
| `name` | Nombre de la categoría. |
| `type` | Tipo de categoría. Toma el valor `INCIDENT` (incidencias) o `SERVICE` (solicitudes de servicios). |
| `description` | Descripción. |
| `icon` | Icono asociado. |
| `active` | Indica si está activa. |

### 8.3. `request_subcategories` (Subcategorías)

| Campo | Descripción |
| --- | --- |
| `subcategory_id` | Identificador único de la subcategoría. |
| `category_id` | Referencia a `request_categories.category_id`. |
| `name` | Nombre de la subcategoría. |
| `description` | Descripción. |
| `icon` | Icono asociado. |
| `active` | Indica si está activa. |

### 8.4. `service_requests` (Solicitudes)

| Campo | Descripción |
| --- | --- |
| `service_request_id` | Identificador único de la solicitud. |
| `cabin_id` | Referencia a `cabins.cabin_id`. |
| `category_id` | Referencia a `request_categories.category_id`. |
| `subcategory_id` | Referencia a `request_subcategories.subcategory_id`. |
| `quantity` | Cantidad solicitada, cuando corresponda. |
| `preferred_delivery` | Momento preferido para recibir el servicio. |
| `status` | Estado actual de la solicitud. |
| `created_at` | Fecha y hora de creación. |
| `updated_at` | Fecha y hora de la última actualización. |

### 8.5. `request_status_history` (Historial de estados)

| Campo | Descripción |
| --- | --- |
| `history_request_id` | Identificador único del registro del historial. |
| `service_request_id` | Referencia a `service_requests.service_request_id`. |
| `previous_status` | Estado anterior. |
| `new_status` | Estado nuevo. |
| `changed_at` | Fecha y hora del cambio. |

### 8.6. Identificadores (ID)

- Cada tabla tiene un identificador principal con un nombre específico (`cabin_id`, `category_id`, `service_request_id`, etc.), generado automáticamente por la base de datos.
- Los identificadores son independientes entre tablas. Puede existir un `cabin_id = 1` y un `category_id = 1` sin conflicto.
- Los campos que hacen referencia a identificadores de otras tablas son claves externas (*foreign keys*) y relacionan los registros entre sí.
- Los identificadores no son necesariamente consecutivos. Si se elimina un registro, su número no se reutiliza necesariamente.

### 8.7. Estancias

En esta primera versión no existe la tabla `stays`. Las solicitudes quedan vinculadas directamente al camarote mediante `cabin_id`. Esto simplifica el prototipo. Más adelante podrá añadirse una forma de distinguir las solicitudes de diferentes viajes.

### 8.8. Catálogo

Las categorías y subcategorías se almacenan en la base de datos y se cargan dinámicamente. Las opciones no se duplican en cada pantalla.

## 9. Privacidad y seguridad

- Se minimizan los datos personales almacenados.
- Se utilizan sesiones temporales.
- Las solicitudes se asocian a un identificador de estancia y camarote.
- No se almacenan nombres, apellidos, pasaportes ni tarjetas de crédito junto a las incidencias.
- El pasajero solo consulta sus propias peticiones.
- Está prevista la anonimización de los datos identificativos al finalizar el crucero.

## 10. Alcance del prototipo

La primera versión del prototipo:

- Utiliza datos ficticios.
- Incluye todas las pantallas descritas en este documento.
- Incluye todas las categorías y subcategorías especificadas.
- Permite crear incidencias y solicitudes.
- Permite seleccionar productos y cantidades.
- Permite seleccionar el momento de entrega.
- Muestra una confirmación después del envío.
- Permite consultar el seguimiento.
- Simula los cambios de estado.

La primera versión no incluye conexiones reales con el PMS, proveedores ni sistemas externos.

## 11. Principios de diseño y restricciones

- El catálogo es cerrado: contiene únicamente las categorías y subcategorías definidas en las secciones 3 y 4. No existen otras.
- Todas las opciones del catálogo definido están disponibles.
- Los nombres de tablas y campos son los definidos en la sección 8.
- La interfaz es coherente en todas las pantallas y se compone de elementos reutilizables.

## 12. Resultado final

La aplicación es navegable y funcional para el pasajero, y permite completar dos recorridos.

**Incidencias**

Inicio → Comunicar incidencia → Categoría → Subcategoría → Confirmación → Seguimiento.

**Solicitudes**

Inicio → Realizar solicitud → Categoría → Producto → Cantidad y horario → Confirmación → Seguimiento.

El diseño responde al prototipo original de ocho pantallas y permite probar todas las opciones con datos ficticios.

## 13. Puntos no definidos en el documento original

Estos puntos no están especificados y se recogen para que quede constancia de ello:

- No se indica qué productos o servicios llevan cantidad ni cuáles no. Solo se establece que no todas las peticiones necesitan cantidad ni horario.
- No se define cómo se asigna el departamento responsable ni dónde se almacena, porque ningún campo del modelo lo recoge.
- La sección 9 menciona un identificador de estancia, mientras que la sección 8.7 indica que no existe la tabla `stays`. No se define dónde se guarda ese identificador.
- No se definen los valores concretos de los campos `status` y `preferred_delivery`; solo se definen sus nombres visibles.

## 14. Corrección respecto al documento original

En el documento original, `request_status_history.service_request_id` figuraba como referencia a `service_requests.request_id`. Ese campo no existe: la clave de `service_requests` es `service_request_id`. En este documento la referencia figura corregida.
