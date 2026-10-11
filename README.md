#  BancolAS Loan Application - Sistema de Evaluación de Préstamos

Sistema empresarial para la gestión, evaluación e historial de solicitudes de crédito de consumo. Construido con arquitectura reactiva de microservicios, mensajería asíncrona mediante **RabbitMQ** y frontend moderno en **Angular**.

---

## Arquitectura del Sistema

El proyecto sigue una arquitectura desacoplada basada en eventos (*Event-Driven Architecture*) y diseño modular guiado por dominio (*Feature-Driven Architecture*):

```text
[ Angular Frontend ] ──(HTTP REST)──> [ Spring Boot API (R2DBC) ] ──> [ PostgreSQL ]
                                                │
                                        (Publish Event)
                                                │
                                                ▼
                                   [ RabbitMQ TopicExchange ]
                                        ├── loan.audit.queue
                                        └── loan.notification.queue ──> [ Email Consumer (SMTP) ]

```
# Tecnologías Utilizadas

**Backend (bancoias-loan-api)**

* Java 23 & Spring Boot 3.3.4.
* Spring Data R2DBC (Acceso reactivo a base de datos PostgreSQL).
* Spring AMQP (RabbitMQ) (Patrón Publish/Subscribe con TopicExchange).
* Spring Boot Mail (Envío asíncrono de notificaciones vía SMTP).
* JUnit 5 & Mockito (Testing unitario e integrado).
* Maven (Gestión de dependencias).


**Frontend (frontend)**

* Angular 19+ (Componentes Standalone, Signals input/output e inject)
* Angular Material & Tailwind CSS
* Reactive Forms con FormGroupDirective para gestión limpia de validaciones
* Vitest / Karma & Jasmine (Pruebas unitarias frontend)

---

## 1. Instalación y Ejecución

Clonar el repositorio:

```env
git clone https://github.com/SYepesCommit/bancolas-loan-app.git
```

## 2. Infraestructura (Docker Compose)
Asegúrate de tener Docker (Desktop) corriendo y ejecuta:

```env
docker-compose up -d
```
-PostgreSQL: localhost:5432 (BD: bancolas_db)

-RabbitMQ Management: http://localhost:15672 (User/Pass: guest/guest)

## 3. Iniciar el Backend

```env
cd backend
.\mvnw.cmd spring-boot:run
```

## 4. Iniciar el Frontend

```env
cd ../frontend
npm install
npm start
```

- Aplicación Web: http://localhost:4200

---

## Ejecución y explicación de Pruebas Unitarias

## Pruebas Automatizadas en el Backend (Spring Boot / JUnit 5 + Mockito):

## En LoanServiceTest y LoanControllerTest se prueban automatizadamente los siguientes escenarios clave:

* Reglas de Negocio (RF02 / RF03):
* Rechazo cuando el cliente está en estado bloqueado (caso USR-30).
* Rechazo por capacidad disponible insuficiente.
* Aprobación exitosa y descuento atómico de la capacidad del cliente cuando cumple todas las condiciones.
* Idempotencia (RF05):
* Verificación de que una requestReference repetida retorna la respuesta original almacenada sin procesar un segundo crédito ni alterar el cupo del cliente.
* Integración y Mensajería:
* Prueba de contexto general (BancoiasLoanApiApplicationTests) con mocks de infraestructura (JavaMailSender, RabbitTemplate).
* Validación de endpoints REST en la capa controladora.

## Pruebas Automatizadas en el Frontend (Angular / Vitest + Jasmine):

## En loan-form.spec.ts y loan.service.spec.ts se prueban automáticamente los comportamientos de interfaz.

* Validación de Formularios (RF07):
* Rechazo de montos inferiores al mínimo ($10.000 COP).
* Rechazo de plazos fuera del rango permitido (menores a 6 o mayores a 60 meses).
* Validación de formato en la referencia (SOL-xxx).
* Asignación rápida de cliente desde los accesos directos (setQuickCustomer).
* Flujo de Usuario y Estado:
* Emisión correcta del evento submitForm con el payload transformado solo cuando el formulario es válido.
* Reseteo limpio del formulario (resetForm), conservando la autogeneración de consecutivos y evitando la activación indeseada de alertas rojas en Angular Material.

## Backend (Spring Boot)

```env
cd backend
mvn test
```

---

## Frontend (Angular)

```env
cd frontend
npx ng test --watch=false
```

---

## Registro de Decisiones Técnicas (ADRs)

### 1. Stack Backend: Spring WebFlux + Spring Data R2DBC
* **Contexto / Problema:** El sistema requiere procesar evaluaciones de crédito de forma altamente concurrente (RF04) sin bloquear hilos de ejecución.
* **Alternativas consideradas:** 
  1. Spring MVC + JPA/Hibernate (bloqueante tradicional).
  2. Spring WebFlux + R2DBC (completamente reactivo y no bloqueante).
* **Decisión adoptada:** Spring WebFlux con Spring Data R2DBC.
* **Justificación:** Garantiza un throughput elevado y uso eficiente de memoria/CPU al procesar peticiones I/O no bloqueantes desde la capa Web hasta la base de datos PostgreSQL.
* **Trade-offs / Riesgos:** Mayor complejidad en el manejo de flujos asíncronos (`Mono`/`Flux`) y en el control transaccional reactivo.
* **Validación:** Pruebas unitarias con `StepVerifier` y ejecución de solicitudes concurrentes verificando no bloqueo de hilos principales.

---

### 2. Arquitectura Orientada a Eventos: RabbitMQ TopicExchange
* **Contexto / Problema:** La notificación por correo y la auditoría no deben penalizar el tiempo de respuesta de la API ni hacer fallar la evaluación financiera si el servidor SMTP decae.
* **Alternativas consideradas:**
  1. Llamadas síncronas HTTP/REST directas al servicio de correo.
  2. Métodos asíncronos en memoria con `@Async`.
  3. Cola de mensajería asíncrona mediante RabbitMQ.
* **Decisión adoptada:** Publicación de eventos en RabbitMQ a través de un `TopicExchange` con dos colas independientes (`loan.audit.queue` y `loan.notification.queue`).
* **Justificación:** Desacopla totalmente el Core Financiero de las tareas secundarias. Si la cola de correo falla, la transacción de aprobación/rechazo no se ve afectada.
* **Trade-offs / Riesgos:** Dependencia de infraestructura adicional (Broker de RabbitMQ).
* **Validación:** Mapeo de eventos en `LoanServiceTest` y consumidores asíncronos aislados en `LoanNotificationConsumer`.

---

### 3. Estrategia de Idempotencia (RF05)
* **Contexto / Problema:** Evitar el procesamiento doble de una misma `requestReference` y prevenir la sobre-asignación de crédito si un cliente reintenta una petición.
* **Alternativas consideradas:**
  1. Caché temporal en memoria / Redis.
  2. Verificación y persistencia atómica en base de datos PostgreSQL con restricción única sobre `requestReference`.
* **Decisión adoptada:** Consulta de existencia de `requestReference` en PostgreSQL previo al procesamiento.
* **Justificación:** Otorga consistencia fuertemente transaccional (ACID/R2DBC) sin agregar la complejidad operacional de un servidor Redis adicional.
* **Trade-offs / Riesgos:** Una lectura adicional a la base de datos por cada solicitud de préstamo.
* **Validación:** Prueba unitaria en `LoanServiceTest` que reenvía una referencia existente y verifica la devolución exacta de la respuesta almacenada previamente sin modificar la capacidad del cliente.

---

### 4. Frontend: Angular Standalone + Signals + Reset Limpio
* **Contexto / Problema:** Crear una interfaz reactiva (RF07) que gestione validaciones en tiempo real sin falsos errores de validación tras reiniciar el formulario con Angular Material.
* **Alternativas consideradas:**
  1. Arquitectura clásica por `NgModules`.
  2. Arquitectura moderna de componentes *Standalone* con `Signals` y gestión mediante `FormGroupDirective`.
* **Decisión adoptada:** Componentes *Standalone*, `input`/`output` reactivos de Angular 19+ y reseteo explícito de la directiva `FormGroupDirective`.
* **Justificación:** Reduce el código repetitivo (*boilerplate*), mejora la legibilidad, incrementa el rendimiento de renderizado y soluciona el estado interno `submitted = true` de Angular Material.
* **Trade-offs / Riesgos:** Requiere familiaridad con las características más recientes del ecosistema Angular.
* **Validación:** Suite de pruebas en Vitest/Jasmine (`loan-form.spec.ts`) con 100% de tests aprobados sobre validaciones, consecutivo dinámico y reseteo.


--- 

## Documentación sobre el Uso de Inteligencia Artificial

Durante el desarrollo del proyecto se utilizó Inteligencia Artificial como un copiloto y herramienta de apoyo para la discusión de ideas de arquitectura, refactorización de código, resolución de problemas puntuales de entorno y estructuración de la documentación. Todas las decisiones de diseño, implementación final y control de calidad estuvieron a cargo del desarrollador.

### 1. Actividades en las que sirvió de apoyo
* **Refactorización del Frontend:** Asistencia en la separación y modularización de la interfaz (ya que inicialmente toda la lógica y vista del frontend se encontraban concentradas en un solo archivo), migrando hacia una estructura orientada a características (*Feature-Driven Architecture*) con componentes *Standalone*, uso de *Signals* (`input`/`output` e `inject`) y separación clara de modelos, servicios y componentes (`LoanFormComponent`, `LoanHistoryComponent`, etc.).
* **Ideación y Arquitectura:** Discusión de alternativas para el desacoplamiento de procesos secundarios (eventos asíncronos con RabbitMQ vs. procesamiento síncrono) y estrategias de idempotencia (RF05).
* **Depuración de Pruebas Unitarias:** Apoyo en el diagnóstico de errores de contexto en Spring Boot (`JavaMailSender` y `RabbitTemplate`) al ejecutar la suite de pruebas.
* **Estructuración de Documentación:** Colaboración en la redacción técnica del `README.md` y en la síntesis del Registro de Decisiones Técnicas (ADRs).

### 2. Sugerencias e ideas aprovechadas
* Estrategia de desacoplamiento modular del frontend pasando de una vista monolítica en un único archivo a componentes *Standalone* reutilizables y limpios.
* Implementación de la generación automática de la referencia basada en marca de tiempo para asegurar la unicidad del consecutivo (`SOL-HHMMSS`) en el formulario.
* Solución técnica para resetear la bandera interna `submitted` de Angular Material utilizando `@ViewChild(FormGroupDirective)`.
* Enfoque para mockear componentes de infraestructura externa (SMTP/RabbitMQ) en las pruebas de integración del backend usando `@MockBean`.
* Estructura organizada para presentar el mapeo de requisitos de negocio (RF01 - RF07).

### 3. Validación de las sugerencias
* **Pruebas Automatizadas:** Toda refactorización o solución fue validada mediante la ejecución local de las suites de prueba (`npx ng test` y `mvn test`), adaptando las aserciones a la nueva estructura modular y manteniendo 0 fallos.
* **Pruebas Funcionales (E2E):** Comprobación manual del flujo completo en el navegador conectando el frontend modularizado con el backend en tiempo real.
* **Criterio y Revisión Manual:** Inspección estática del código para asegurar que cumpliera con las buenas prácticas de legibilidad, mantenibilidad y estándares del proyecto.

### 4. Sugerencias descartadas o ajustadas
* **Descartadas:** Se descartaron ideas y componentes adicionales generados durante la discusión (como calculadoras de cuotas o simuladores de tasas) para mantener el alcance acotado estrictamente a los requerimientos de la prueba.
* **Ajustadas:** El reseteo convencional del formulario (`loanForm.reset()`) se ajustó a `formDirective.resetForm()` al detectar que Angular Material mantenía la bandera visual de formulario enviado.

### 5. Consideraciones de seguridad y privacidad
* **Cero credenciales compartidas:** No se utilizaron ni expusieron claves reales, tokens de acceso, ni información de entornos de producción.
* **Manejo de datos sintéticos:** Todas las consultas y pruebas se realizaron exclusivamente con datos de prueba ficticios (`USR-10`, `USR-20`, montos simulados y entornos `localhost`).
