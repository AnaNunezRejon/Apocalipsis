Documentación técnica — USO DE LA IA
Proyecto: Apocalipsis Granada

# Introducción general

Durante el desarrollo del proyecto Apocalipsis, diseñé toda la arquitectura siguiendo el patrón MVC (Modelo–Vista–Controlador), buscando mantener un código limpio, coherente y totalmente documentado en español.

La inteligencia artificial (ChatGPT) se utilizó como apoyo conceptual y técnico en fases iniciales del desarrollo (por ejemplo, para explorar estructuras base o patrones de control).
Sin embargo, la estructura definitiva, la nomenclatura, la división modular y la implementación completa son el resultado de mi propio trabajo tras un proceso de análisis, refactorización y ajuste a las directrices del profesorado.

Siguiendo las recomendaciones de la profesora, finalmente opté por dividir la parte lógica y la parte visual del controlador, separando responsabilidades y mejorando la claridad estructural del proyecto.
De este modo, surgieron dos clases independientes:

Controlador.java → lógica y flujo interno de la simulación.

ManejadorVistas.java → manejo visual común y configuración de interfaz.

## Arquitectura de Vistas (VistaPrincipal, VistaHistorial, VistaGuia, VistaServicios)

| **Clase**          | **Objetivo Principal**                                                             | **Petición a la IA (inicio del proyecto)**                         | **Decisión Arquitectónica Final**                                                                                                                                                      |
| ------------------ | ---------------------------------------------------------------------------------- | ------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **VistaPrincipal** | Coordinar y presentar los mensajes diarios (alertas + guías) en un `RecyclerView`. | Sugerencia base para estructura y conexión con modelo/controlador. | Mantengo la vista centrada en la presentación y traslado toda la lógica de orden, índice y avance al nuevo **Controlador**. El manejo visual (cabecera, menú, modo desarrollador) pasa al **ManejadorVistas**. |
| **VistaHistorial** | Mostrar el registro completo de alertas anteriores.                                | Propuesta para listar cronológicamente las alertas directametne desde mensajes.java.     | La lógica de filtrado y carga queda en el **Controlador**, mientras la vista solo muestra los datos formateados. Implementé el orden descendente de días como mejora funcional.                                |
| **VistaGuia**      | Presentar consejos y recomendaciones sincronizados con el día actual.              | Sugerencia para mostrar guías diarias según fase o día.            | Centralizo el filtrado de datos en el **Controlador** y limito la vista a la representación. Se sincroniza automáticamente con el avance diario.                                                               |
| **VistaServicios** | Ofrecer recursos prácticos y contactos oficiales de emergencia.                    | Estructura básica para listar servicios.                           | Implementé la carga modular de servicios y su presentación autónoma. Aunque no depende del flujo del juego, respeta la misma identidad visual definida por el **ManejadorVistas**.                             |


## Separación lógica y visual (Controlador ↔ ManejadorVistas)

Una de las decisiones clave fue la división del antiguo controlador monolítico en dos archivos diferenciados, según las recomendaciones de la profesora y el análisis posterior de responsabilidades:

| **Archivo**            | **Rol actual**                                                                                                          |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| `Controlador.java`     | Gestiona la **lógica del juego** (avance de días, alertas, notificaciones, linterna SOS, reinicio de simulación, etc.). |
| `ManejadorVistas.java` | Administra la **parte visual** (menú inferior, cabecera, colores, modo desarrollador y saludos).                        |


Esta separación mejora la mantenibilidad, evita duplicación de código entre pantallas y garantiza una clara correspondencia con el modelo MVC tradicional.

## Propuestas iniciales de la IA descartadas
Durante el desarrollo, la IA propuso una arquitectura más compleja y jerárquica, que finalmente descarté para ajustarme a los criterios académicos y de simplicidad del proyecto:
BaseActivity: descartada para evitar herencia innecesaria y acoplamiento entre vistas.
ManejadorAlertas.java (modelo auxiliar): reemplazado por una lógica centralizada en Controlador.java.
Controladores múltiples: sustituido por un único Controlador principal con funciones bien definidas.
Gestión visual dentro del Controlador: movida a ManejadorVistas.java para respetar el principio de separación de responsabilidades.

## Decisiones de implementación propias
Centralización de la lógica: todo el control narrativo y de simulación reside en Controlador.java.
Modularidad visual: cualquier cambio estético (colores, cabeceras, botones) se controla exclusivamente desde ManejadorVistas.java.
Código robusto y limpio: eliminación de valores null, uso de for indexados, nombres en español, enumeraciones y valores por defecto.
Persistencia clara: gestión del progreso del juego y usuario mediante la clase Preferencias.java.
Separación total entre lógica y vista, manteniendo coherencia y evitando dependencias circulares.
AdaptadorMensajes (interfaz del RecyclerView)

## Objetivo de la clase
AdaptadorMensajes conecta el modelo Mensaje con la vista RecyclerView para mostrar las alertas y guías en las diferentes pantallas.

Decisión clave (vs. IA)
La IA propuso un booleano simple para diferenciar tipos de mensaje.
Yo amplié el enfoque, implementando un atributo “tipo” dentro del modelo Mensaje y un switch que adapta color, icono y estilo visual según si el mensaje es una alerta, una guía o un aviso genérico.
Esto permite escalar fácilmente el diseño sin duplicar código.

Controlador (núcleo lógico)

Objetivo
Coordinar la simulación completa del juego, gestionando el avance diario, los mensajes, las notificaciones y los sonidos.

Evolución
La versión inicial de la IA agrupaba todo en un único bloque, mezclando lógica y visualización.
En mi versión final, la lógica pura (alertas, JSON, linterna, progreso, reinicio, sonido, notificaciones) quedó en Controlador.java,
mientras que la gestión estética pasó a ManejadorVistas.java.

Esto generó una estructura más clara, escalable y acorde al patrón MVC, tal como recomendó la profesora en la revisión final.

Conclusión general

El proyecto Apocalipsis Granada demuestra la correcta aplicación del patrón MVC y un proceso de desarrollo iterativo donde la IA funcionó como apoyo conceptual, no como autor del código.

Las últimas decisiones —como la separación entre Controlador y ManejadorVistas, la centralización del modelo Mensaje y la documentación detallada— fueron el resultado de criterios propios y revisión académica, no de generación automática.

La IA sirvió de punto de partida técnico, pero el código final, la estructura, los nombres, la documentación y la organización del proyecto son resultado directo de mi trabajo, pruebas y decisiones personales.
El resultado es una aplicación funcional, organizada y coherente, tanto a nivel lógico como visual, fiel al espíritu institucional y narrativo del proyecto.


![vistaguia](https://github.com/user-attachments/assets/1d24921b-cd09-4bba-99ea-a0d1d1a70141)
![vistahistorial](https://github.com/user-attachments/assets/64afc1c0-6f91-4c94-80fe-0e736706b45b)
![vistaprincipal](https://github.com/user-attachments/assets/2420cf2a-3324-4641-a0d1-c63cb2aa1bbb)
![vistaservicios](https://github.com/user-attachments/assets/3203c2cc-aa11-4fb3-94d3-a4020ffc00ab)
![adaptadormensajes](https://github.com/user-attachments/assets/0840a2b2-d907-4667-b305-2786f25a9745)
![configurarbotondesarrollador](https://github.com/user-attachments/assets/4aa698fa-579c-4f56-9367-6b41d04d77c9)


------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

 # GREMLINS

![Apocalipsis](https://github.com/user-attachments/assets/bee64e69-faa7-44a5-ac01-fe76d81e3d51)


## Descripción general

Apocalipsis Granada es una aplicación Android desarrollada en Java, ambientada en una historia ficticia donde una invasión de Gremlins amenaza la ciudad de Granada.
El usuario recibe diariamente alertas oficiales del Gobierno de España 🏛️ y guías de supervivencia para resistir el apocalipsis.

Cada día trae nuevos mensajes, sonidos, notificaciones y eventos, simulando una historia interactiva que evoluciona con el tiempo.

##  Características principales

### Simulación diaria:
Cada día desbloquea nuevas alertas y guías vinculadas a la narrativa.

### Modo desarrollador oculto:
Toca el escudo 🛡️ cinco veces para activarlo.
Permite avanzar días manualmente, reiniciar la simulación y ver cambios visuales (colores, botones y barra informativa superior).

### Persistencia de datos:
Guarda el progreso, el nombre del usuario y el estado del modo desarrollador mediante SharedPreferences.

### Notificaciones y sonido:
Cada alerta importante genera una notificación del sistema y reproduce un sonido distintivo.

### Evento especial:
El día 14 a las 23:00, la app activa automáticamente la linterna en modo SOS 🔦.

### Arquitectura modular (MVC):
Estructura limpia y bien separada entre modelo, lógica y vista.

### Vista de servicios oficiales:
Acceso rápido a teléfonos y páginas web de organismos públicos, fuerzas de seguridad y emergencia.

##  Arquitectura del proyecto

El proyecto sigue el patrón Modelo - Vista - Controlador (MVC), con la lógica y la interfaz separadas en dos capas bien definidas:

com.example.apocalipsisgranada  
│  
├── modelo  
│   └── Mensaje.java  
│   └── Usuario.java  
│  
├── vista  
│   ├── LoginActivity.java  
│   ├── VistaPrincipal.java  
│   ├── VistaGuia.java  
│   ├── VistaHistorial.java  
│   ├── VistaServicios.java  
│   ├── AdaptadorMensajes.java  
│   └── ManejadorVistas.java  
│   
└── controlador   
    ├── Controlador.java  
    └── Preferencias.java    


## Tecnologías utilizadas  
Componente	Descripción  
Lenguaje	Java  
Entorno	Android Studio  
Arquitectura	MVC (Modelo-Vista-Controlador)  
UI	XML + RecyclerView  
Persistencia	SharedPreferences  
Recursos	JSON (alertas y guías), sonidos, drawables  
APIs Android	Notificaciones, MediaPlayer, CameraManager (linterna)  

## Objetivos educativos

Proyecto desarrollado en el marco del ciclo Desarrollo de Aplicaciones Multiplataforma (DAM) con fines didácticos.

Objetivos principales:

Aplicar el patrón MVC en Android.

Aprender a usar RecyclerView y adaptadores personalizados.

Implementar persistencia local con SharedPreferences.

Gestionar notificaciones del sistema y reproducción de sonido.

Controlar intents y navegación entre vistas.

Desarrollar una aplicación narrativa e interactiva con eventos dinámicos.

## Flujo general de funcionamiento

El usuario inicia sesión introduciendo su nombre.

La aplicación carga las alertas y guías desde los archivos JSON.

Se muestran los mensajes correspondientes al día actual.

En modo desarrollador, el usuario puede avanzar manualmente de día o reiniciar la simulación.

Cada nueva alerta genera una notificación y un sonido.

En el día 14 a las 23:00, la app activa la linterna SOS como evento especial.

## Interfaz principal

VistaPrincipal: muestra las alertas y guías del día actual.

VistaGuia: muestra los consejos desbloqueados de días anteriores.

VistaHistorial: lista todas las alertas emitidas en orden cronológico.

VistaServicios: acceso directo a teléfonos y webs oficiales.

LoginActivity: pantalla inicial donde el usuario introduce su nombre.

## Modo desarrollador

Activando el modo desarrollador (cinco toques en el escudo 🛡️):

Se habilitan los botones “Avanzar día” y “Reiniciar simulación”.

Cambian los colores del entorno (verde y rosa).

Aparece una barra superior con el texto:

## Modo desarrollador — Día X

Permite probar la simulación y depurar sin esperar el paso real del tiempo.

## Permisos necesarios
Permiso	Función
POST_NOTIFICATIONS	Mostrar alertas del Gobierno en forma de notificación.
CAMERA	Activar la linterna para el evento SOS.
INTERNET	Acceso a enlaces oficiales en la vista de servicios.
VIBRATE	Vibración al recibir notificaciones.
🧾 Datos de ejemplo

Los archivos alertas.json y guias.json se encuentran en la carpeta /assets y contienen entradas como:

{
  "dia": 14,
  "mensaje": "Los Gremlins han cortado la electricidad. Usa la linterna y evita salir de casa.",
  "sonido": "true"
}

## Persistencia de datos

Los datos se almacenan en SharedPreferences bajo el archivo configuracion.

Clave	Uso
nombreUsuario	Guarda el nombre introducido en el login.
diaActual	Día actual de la simulación.
indiceMensajeDia	Índice del mensaje mostrado dentro del día.
modoDesarrollador	Estado del modo desarrollador.
fechaInicio	Fecha base de inicio de simulación.
primer_arranque	Marca la primera ejecución del juego.

##  Evento especial — Día 14

Linterna SOS automática a las 23:00

El controlador detecta:

if (diaActual == 14 && hora == 23) {
    activarLinternaSOS(context);
}


La linterna parpadea en código Morse “SOS” (... --- ...), cerrando la narrativa principal del juego.

## Pruebas básicas
Caso	Resultado esperado
Primer inicio	Muestra la pantalla de login y guarda el nombre.
Avanzar día (modo dev)	Incrementa el día y muestra nuevos mensajes.
Recibir alerta	Reproduce sonido y muestra notificación.
Reiniciar simulación	Restablece día 1 y mensajes iniciales.
Día 14 a las 23:00	La linterna parpadea en modo SOS.

## Posibles mejoras

Integrar base de datos Room para guardar el historial completo.

Añadir modo oscuro 🌙.

Incorporar animaciones de transición entre vistas.

Sincronizar eventos con un servidor remoto.

Reemplazar SharedPreferences por ViewModel + LiveData.

## Autor

Ana Núñez Rejón
Estudiante de Desarrollo de Aplicaciones Multiplataforma (DAM)
📍 Granada, España

Proyecto educativo desarrollado con fines didácticos.
Libre para uso, adaptación y modificación con propósitos de aprendizaje.

“Cuando los Gremlins atacan… el conocimiento es la mejor defensa.” 🔦😈
