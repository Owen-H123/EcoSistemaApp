# EcoApp Hannah

Aplicación Android nativa en **Kotlin** para **Hannah Lab**, empresa peruana de tecnología. Presenta la empresa, permite contactarla y ofrece un panel privado donde cada trabajador ve sus horas registradas y su planilla. El administrador ve los datos de todo el equipo.

> Los trabajadores, tarifas, proyectos y horas que trae la app son **datos de demostración ficticios**.

## Funcionalidades

**Zona pública** (menú lateral)

- **Splash** de 5 segundos con la marca.
- **Bienvenida** con acceso como invitado o con inicio de sesión.
- **Inicio**: presentación de la empresa y sus seis líneas de servicio.
- **Nosotros**: forma de trabajo, valores y fortalezas.
- **Contactos**: oficina, correo y web (abren Maps, la app de correo y el navegador) y formulario con validación que guarda el mensaje en la base local.
- **Login** y **recuperar contraseña**.

**Panel del trabajador** (después de iniciar sesión)

| Pestaña | Developer | Admin |
| --- | --- | --- |
| Inicio | Sus horas del mes, neto estimado, avance contra su meta y sus registros (hoy, semana, mes) | Horas y planilla de todo el equipo, y los registros de cada persona |
| Análisis | Gráfico de sus horas por semana contra la meta y horas por proyecto | Lo mismo para el equipo, con filtro por persona y horas por persona |
| Planilla | Su cálculo de salario | Planilla de todos; puede editar la tarifa de cada trabajador |
| Perfil | Sus datos y cerrar sesión | Además, alta de nuevos trabajadores |

La planilla se calcula por horas: `bruto = horas × tarifa`, menos un descuento referencial de 13 % (ONP).

## Tecnologías

- Kotlin, Views con XML y ViewBinding
- Material Components 3 (Navigation Drawer, barra inferior, campos de texto)
- Room (SQLite) con KSP para la base local
- Coroutines y Flow
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) para los gráficos
- Fuentes Oswald e Inter incluidas en `res/font`

Versiones: Android Gradle Plugin 8.11, Kotlin 2.1, Gradle 8.14, `minSdk` 26, `targetSdk` 36.

## Cómo ejecutarla

1. Clonar el repositorio.
2. Abrir la carpeta en **Android Studio** (File → Open).
3. Esperar a que termine la sincronización de Gradle. Si Android Studio ofrece actualizar Gradle o el plugin de Android, no lo aceptes: las versiones del proyecto ya son compatibles entre sí.
4. Elegir un emulador o un teléfono con depuración USB y presionar **Run**.

También desde la terminal, con un emulador abierto:

```bash
./gradlew installDebug
```

## Usuarios de prueba

Se crean solos la primera vez que se abre la app.

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Admin | `admin@ecoapp.test` | `admin123` |
| Developer | `ana@ecoapp.test` | `dev123` |
| Developer | `luis@ecoapp.test` | `dev123` |
| Developer | `carla@ecoapp.test` | `dev123` |

Para volver a los datos iniciales: Ajustes → Apps → EcoApp Hannah → Almacenamiento → Borrar datos.

## Estructura

```
app/src/main/java/com/hannahlab/ecoapp/
├── EcoApp.kt                 Application: base de datos, sesión y datos semilla
├── data/
│   ├── local/                Room: entidades, DAOs, AppDatabase y SeedData
│   ├── repository/           Acceso a datos del formulario de contacto
│   └── session/              Sesión del usuario (SharedPreferences)
├── util/                     Fechas, formato de moneda, cálculo de planilla y hash
└── ui/
    ├── splash/  welcome/  recuperar/
    ├── main/                 Drawer público y sus Fragments
    ├── panel/                Panel del trabajador: inicio, análisis, planilla y perfil
    ├── planilla/             Formulario de salario
    └── trabajador/           Alta de trabajadores (solo admin)
```

Las contraseñas se guardan como hash SHA-256, nunca en texto plano.
