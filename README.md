# AppSolicitudes_Grupo8

App Android del caso **DSY1105-005D** (Duoc UC): *Gestión móvil y seguimiento de solicitudes de
clientes*. Permite registrar, listar, editar y eliminar solicitudes, que se guardan en un
**microservicio propio en Spring Boot**. Además, muestra el **tipo de cambio del día** obtenido
desde una **API externa**.

Todos los datos son **ficticios** (solicitudes, fotos y usuario de prueba).

## 1. Nombre del proyecto

**AppSolicitudes_Grupo8**: app móvil (`app-movil/`) + microservicio `ms-solicitudes`
(`ms-solicitudes/`).

## 2. Integrantes

| Integrante | Tareas |
|---|---|
| Bastian Alarcon | Microservicio `ms-solicitudes` (Spring Boot + H2), estructura MVVM de la app, integración con el microservicio (Retrofit, CRUD), consumo de la API externa, firma del APK (`.jks`) |
| Ignacio Hidalgo | Pruebas unitarias (Kotest + MockK), configuración de la firma (`keystore.properties.example`), README y planificación en Trello |

Planificación en Trello: https://trello.com/b/gEZQVUK8/dsy1105-app-solicitudes-ev-parcial-4

## 3. Funcionalidades

- **Login** con usuario de prueba (`demo` / `1234`) y validación de campos.
- **Menú principal** con una tarjeta del **tipo de cambio del día** (dólar y euro en pesos
  chilenos), obtenida de la API externa.
- **Lista de solicitudes** traída del microservicio (GET): código, título, categoría, estado y
  fecha.
- **Registrar solicitud** (POST): título, descripción, categoría y servicio. El microservicio
  genera el código `SOL-XXXXXXXX`, el estado **Recibida** y la fecha.
- **Editar solicitud** (PUT): al tocarla en la lista se abre el formulario con sus datos.
- **Eliminar solicitud** (DELETE): botón *Eliminar* en el formulario de edición.
- **Evidencia**: foto con la **cámara** o imagen de la **galería**.
- **Validaciones** con ícono y mensaje de error en cada campo (en la app y en el microservicio).
- **Animaciones** en la navegación, la lista y la foto de evidencia.

## 4. Endpoints utilizados

### Microservicio propio `ms-solicitudes` (Spring Boot, puerto 8080)

| Método | Endpoint | Qué hace | Respuesta |
|---|---|---|---|
| GET | `/api/solicitudes` | Lista todas las solicitudes (las más nuevas primero) | 200 |
| GET | `/api/solicitudes/{id}` | Busca una solicitud | 200 o 404 |
| POST | `/api/solicitudes` | Crea una solicitud (código, estado y fecha los pone el servidor) | 201 o 400 |
| PUT | `/api/solicitudes/{id}` | Edita título, descripción, categoría, servicio y evidencia | 200, 400 o 404 |
| DELETE | `/api/solicitudes/{id}` | Elimina una solicitud | 204 o 404 |

La app usa `http://10.0.2.2:8080/`, porque `10.0.2.2` es el `localhost` del computador visto
desde el emulador.

### API externa

| Método | Endpoint | Qué se usa |
|---|---|---|
| GET | `https://open.er-api.com/v6/latest/USD` | `rates.CLP` (pesos por dólar) y `rates.EUR` (para calcular el euro en pesos) |

Es una API pública, sin clave. Se consume con Retrofit y se muestra en el menú, separada del
microservicio.

## 5. Pasos para ejecutar

### Requisitos

- Android Studio con un emulador (API 26 o superior).
- **JDK 17 o 21** para el microservicio. En Android Studio: *Settings → Build, Execution,
  Deployment → Build Tools → Gradle → Gradle JDK = 21*.

### 5.1 Microservicio (primero)

Con IntelliJ: abrir la carpeta `ms-solicitudes` y ejecutar `MsSolicitudesApplication`.

O desde una terminal, dentro de `ms-solicitudes`:

```bash
./mvnw spring-boot:run
```

Queda en `http://localhost:8080/api/solicitudes`. La base **H2** se guarda en el archivo
`ms-solicitudes/data/solicitudesdb` y se crea sola. Para ver la tabla:
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/solicitudesdb`, usuario `sa`,
sin clave).

### 5.2 App móvil

1. Abrir la carpeta `app-movil` en Android Studio y esperar la sincronización de Gradle.
2. Con el microservicio corriendo, ejecutar la app en el emulador.
3. Ingresar con **demo** / **1234**.

### 5.3 Pruebas unitarias

En Android Studio: clic derecho en `app/src/test/java` → *Run 'Tests in solicitudes'*. Para ver
la cobertura: *Run with Coverage*.

O desde una terminal, dentro de `app-movil`:

```bash
./gradlew testDebugUnitTest createDebugUnitTestCoverageReport
```

El reporte queda en `app/build/reports/coverage/test/debug/index.html`.

| Archivo | Qué prueba | Qué se simula (MockK) |
|---|---|---|
| `SolicitudViewModelTest` | Carga de la lista, validaciones, guardar (POST y PUT), eliminar y errores de conexión | `SolicitudRepository` |
| `TipoCambioViewModelTest` | Cálculo del dólar y del euro en pesos, errores de la API y reintento | `TipoCambioRepository` |
| `LoginViewModelTest` | Validaciones del login | Nada (no tiene dependencias) |
| `RepositoryTest` | Que cada repositorio llame al endpoint correcto | `ApiService` y `TipoCambioApiService` |

Resultado: **29 pruebas OK**. Cobertura: **97 %** en `viewmodel` y **100 %** en `repository` y
`remote`. Las pantallas (`ui`) no se cuentan, porque son interfaz y no lógica.

## 6. APK firmado y llave `.jks`

- Llave: `app-movil/keystore/solicitudes.jks` (alias `solicitudes`).
- Configuración de la firma: bloque `signingConfigs` en `app-movil/app/build.gradle.kts`. Las
  claves se leen de `app-movil/keystore.properties`, que **no se sube** a GitHub (ver
  `keystore.properties.example`).
- APK firmado: `app-movil/app/release/app-release.apk`.

Cómo se generó:

1. Se creó la llave con `keytool` (RSA 2048, válida por 10 000 días), dentro de `app-movil`:

   ```bash
   keytool -genkeypair -keystore keystore/solicitudes.jks -alias solicitudes -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Se copió `keystore.properties.example` a `keystore.properties` y se escribieron las claves.
3. Se generó el APK firmado con la variante **release** (usa el `signingConfigs` del
   `build.gradle.kts`):

   ```bash
   ./gradlew assembleRelease
   ```

   El APK sale en `app/build/outputs/apk/release/` y se copió a `app/release/app-release.apk`.

En Android Studio se obtiene lo mismo con *Build → Generate Signed App Bundle or APK → APK*,
eligiendo la llave `keystore/solicitudes.jks`, el alias `solicitudes` y la variante **release**.

### Evidencia de la firma

Verificación del APK con `apksigner` (Android SDK build-tools):

```text
> apksigner verify --verbose --print-certs app/release/app-release.apk
Verifies
Verified using v2 scheme (APK Signature Scheme v2): true
Number of signers: 1
Signer #1 certificate DN: CN=AppSolicitudes Grupo8, OU=DSY1105, O=Duoc UC, L=Santiago, C=CL
Signer #1 certificate SHA-256 digest: 8fbe043d18146bc4cd7f918cee52ee09c44a0852ebc9de0bf8d5b14a8a953c32
```

Los datos del certificado (`CN=AppSolicitudes Grupo8, OU=DSY1105…`) son los mismos de la llave
`solicitudes.jks`, así que el APK quedó firmado con ella.

## Estructura

```
app-movil/app/src/main/java/com/example/solicitudes/
├── data/model/      Solicitud, TipoCambio y los UiState (Login, Solicitud, TipoCambio)
├── remote/          ApiService (microservicio), TipoCambioApiService (API externa), RetrofitInstance
├── repository/      SolicitudRepository, TipoCambioRepository
├── viewmodel/       LoginViewModel, SolicitudViewModel, TipoCambioViewModel
├── ui/screens/      LoginScreen, MenuScreen, ListaSolicitudesScreen, SolicitudFormScreen
├── ui/theme/        Colores y tema
├── navigation/      AppNavigation (login → menú → lista → formulario)
└── MainActivity.kt

ms-solicitudes/src/main/java/cl/duoc/solicitudes/
├── model/           Solicitud (entidad JPA, tabla "solicitudes")
├── repository/      SolicitudRepository (JpaRepository)
├── service/         SolicitudService (lógica: código, estado inicial, fecha)
├── controller/      SolicitudController (endpoints REST)
└── MsSolicitudesApplication.java
```

Flujo de datos: Pantalla → ViewModel → Repository → Retrofit (`ApiService`) → Controller →
Service → JpaRepository → H2.
