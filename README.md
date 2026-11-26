# EcoApp Hannah

Aplicación Android nativa en **Kotlin** para **Hannah Lab**, empresa peruana de tecnología. Presenta la empresa, permite contactarla y ofrece un panel privado donde cada trabajador ve sus horas registradas y su planilla. El administrador ve los datos de todo el equipo.

> Los trabajadores, tarifas, proyectos y horas que trae la app son **datos de demostración ficticios**.

## Funcionalidades

**Acceso**

- **Splash** de 5 segundos con la marca.
- **Login**, **registro** y **recuperar contraseña** con Firebase Authentication. La sesión queda iniciada hasta que se cierre.

**Menú lateral** (con la sesión iniciada)

- **Inicio**: presentación de la empresa y sus seis líneas de servicio.
- **Nosotros**: forma de trabajo, valores y fortalezas.
- **Contactos**: oficina, correo y web (abren Maps, la app de correo y el navegador), mapa con la ubicación de la oficina y formulario con validación que guarda el mensaje en Firestore.
- **Login**: la cuenta con la sesión iniciada y el acceso al panel.
- **Cerrar sesión**: vuelve a la pantalla de Login.

**Panel del trabajador**

| Pestaña | Developer | Admin |
| --- | --- | --- |
| Inicio | Sus horas del mes, neto estimado, avance contra su meta y sus registros (hoy, semana, mes). Puede registrar sus horas | Horas y planilla de todo el equipo, y los registros de cada persona. Puede registrar horas de cualquier trabajador |
| Análisis | Gráfico de sus horas por semana contra la meta y horas por proyecto | Lo mismo para el equipo, con filtro por persona y horas por persona |
| Planilla | Su cálculo de salario | Planilla de todos; puede editar la tarifa de cada trabajador (se guarda en Firestore) |
| Perfil | Sus datos y cerrar sesión | Además, alta de nuevos trabajadores, con su cuenta y contraseña |

La planilla se calcula por horas: `bruto = horas × tarifa`, menos un descuento referencial de 13 % (ONP).

## Tecnologías

- Kotlin, Views con XML y ViewBinding
- Material Components 3 (Navigation Drawer, barra inferior, campos de texto)
- Room (SQLite) con KSP para la base local
- Coroutines y Flow
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) para los gráficos
- Maps SDK para Android (modo lite) para el mapa de Contactos
- Firebase Authentication para las cuentas y Firestore para perfiles (`usuarios`), fichas de trabajadores (`trabajadores`), horas (`registros`) y mensajes de contacto (`mensajes`). La base local guarda una copia de las fichas y las horas para las pantallas del panel
- Fuentes Oswald e Inter incluidas en `res/font`

Versiones: Android Gradle Plugin 8.11, Kotlin 2.1, Gradle 8.14, `minSdk` 26, `targetSdk` 36.

## Cómo ejecutarla

1. Clonar el repositorio y copiar en `app/` el archivo `google-services.json` de un proyecto de Firebase con una app Android de paquete `com.hannahlab.ecoapp`. No está en el repositorio y sin él la app no compila.
2. Abrir la carpeta en **Android Studio** (File → Open).
3. Esperar a que termine la sincronización de Gradle. Si Android Studio ofrece actualizar Gradle o el plugin de Android, no lo aceptes: las versiones del proyecto ya son compatibles entre sí.
4. Para ver el mapa, agregar en `local.properties` la línea `MAPS_API_KEY=tu_clave`, con una clave de **Maps SDK for Android** restringida al paquete `com.hannahlab.ecoapp` y a la huella SHA-1 del certificado con el que se firma la app. Sin la clave, la app compila, pero el mapa no carga.
5. Elegir un emulador o un teléfono con depuración USB (con servicios de Google Play) y presionar **Run**.

También desde la terminal, con un emulador abierto:

```bash
./gradlew installDebug
```

## Configurar Firebase

1. En **Authentication → Método de acceso**, habilitar **Correo electrónico/contraseña**.
2. En **Firestore Database → Reglas**, pegar el contenido de [`firestore.rules`](firestore.rules) y publicar. Sin las reglas no se puede iniciar sesión.

## Cuentas

Cada persona crea la suya con **Registrarse** y empieza como trabajador. Para convertirla en administrador: en Firestore, colección `usuarios`, cambiar su campo `rol` a `ADMIN` y volver a iniciar sesión.

El administrador crea trabajadores desde **Perfil → Nuevo trabajador** y, al abrir el panel, sube a Firestore las fichas y horas de demostración que falten.

Los datos de demostración pertenecen a estos correos; una cuenta registrada con uno de ellos ve sus horas, una vez que el administrador las haya subido: `ana@ecoapp.test`, `luis@ecoapp.test`, `carla@ecoapp.test`.

Para volver a los datos iniciales: Ajustes → Apps → EcoApp Hannah → Almacenamiento → Borrar datos.

## Estructura

```
app/src/main/java/com/hannahlab/ecoapp/
├── EcoApp.kt                 Application: base de datos, sesión y datos semilla
├── data/
│   ├── auth/                 Cuentas con Firebase Authentication y perfil en Firestore
│   ├── local/                Room: entidades, DAOs, AppDatabase y SeedData
│   ├── repository/           Fichas, horas y mensajes de contacto en Firestore
│   └── session/              Sesión del usuario (SharedPreferences)
├── util/                     Fechas, formato de moneda y cálculo de planilla
└── ui/
    ├── splash/  acceso/  registro/  recuperar/
    ├── main/                 Menú lateral y sus Fragments
    ├── panel/                Panel del trabajador: inicio, análisis, planilla y perfil
    ├── horas/                Formulario de registro de horas
    ├── planilla/             Formulario de salario
    └── trabajador/           Alta de trabajadores (solo admin)
```

Las contraseñas de las cuentas las gestiona Firebase Authentication; la app no las guarda.
