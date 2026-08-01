# 🧭 DECISIONES.md — Bitácora de diseño

> **Instrucciones.** Completa **una entrada por fase**, en **primera persona** y
> **refiriéndote a tu propio código**: nombres reales de tus clases, tu tabla, tus
> líneas, tu salida real de terminal.
>
> ❌ **No puntúa** una justificación genérica que podría pegarse en cualquier proyecto
> (ej.: *"usé boundedElastic porque es una buena práctica para operaciones bloqueantes"*).
> ✅ **Sí puntúa** una justificación anclada a tu código (ej.: *"en `ProductoService`
> línea 34 envolví `productoRepository.findAll()` porque Hibernate abre la conexión
> JDBC en el hilo llamante; al probarlo sin `subscribeOn` vi en el log el hilo
> `reactor-http-nio-2`, que es el event loop de Netty"*).
>
> Estas mismas preguntas se te harán en la **defensa oral**.

---

## Datos

- **Nombre:** David Leandro Correa Beltrán
- **Cédula:** 0944321249
- **NN (dos últimos dígitos):** 49
- **Categoría asignada (según el último dígito):** Quinua

---

## Fase 1 — Configuración y perfiles

**1.1** ¿Qué archivo activa el perfil `prod` y qué línea exacta lo hace?

> Este archivito `application.properties` activa el perfil cuando cree el otro archivo `application-prod.properties` y le asigne la misma nomenclatura que esta despues del guion, en la linea: `spring.profiles.active=prod`

**1.2** Pega la línea del log de arranque donde se ve tu puerto y el perfil activo.

```bash
2026-07-30T22:54:43.084-05:00  INFO 19776 --- [agrosmart] [           main] o.s.boot.reactor.netty.NettyWebServer    : Netty started on port 8149 (http)
```

**1.3** ¿Qué habría pasado si dejabas `ddl-auto=create-drop` en lugar de `update`?
Responde pensando en tus datos sembrados.

> Si hubiera dejado el `ddl-auto` en create and drop, al detener mi aplicación Hibernate hubiera eliminado mi base de datos, ya que en el siguiente inicio la borraria y la volveria a crear y a insertar mis datos sembrados, que si bien es util cuando se hacen pruebas en las entidades, rompe el proposito de tener una base de datos persistente

**1.4** ¿Levantaste PostgreSQL con `compose.yaml` (Opción A) o con una instalación local
(Opción B)? ¿Qué ventaja tiene la que elegiste?

> Levante mi base de datos con `compose.yaml` ya que ademas de no requerir ninguna instalación en mi maquina, esto hace que pueda "exportar" la configuración de mi base de datos e infraestructura para que cualquier persona pueda replicar mi entorno de pruebas rapidamente

---

## Fase 2 — Persistencia con JPA/Hibernate

**2.1** ¿Cuál es el nombre exacto de tu tabla y de dónde salió ese nombre?

> El nombre que me toco ponerle a la tabla fue `tbl_productos_base_49` y este se le asigno en base a los dos ultimos digitos de mi numero de cedula

**2.2** Pega la salida de `psql -d agrosmart_db -c "\d tbl_productos_base_NN"` y
señala dónde se ve la restricción `unique` y el `length` de 120.

```sql
agrosmart_db=# \dt
                   List of tables
 Schema |         Name          | Type  |   Owner
--------+-----------------------+-------+-----------
 categoria           | character varying(40)  |           |          |
 correo_notificacion | character varying(500) |           |          |
 nombre_producto     | character varying(120) |           | not null |       //Length de 120
 precio_usd          | numeric(10,2)          |           |          |
 stock_kg            | integer                |           | not null |
Indexes:
    "tbl_productos_base_49_pkey" PRIMARY KEY, btree (id_producto)
    "ukmrqayey9imjuq3rrbumqg9xv7" UNIQUE CONSTRAINT, btree (nombre_producto) //Restriccion UNIQUE

```

**2.3** ¿Por qué usaste `BigDecimal` y no `double` para `precio_usd`? Relaciónalo con el
tipo que generó Hibernate en PostgreSQL.

> Elegi BigDecimal por sobre double ya que este es un valor de tipo flotante, y tengo entendido que puede causar perdidas de precisión y errores de redondeo en operacioens financieras como sumas o divisiones. Con BigDecimal Hibernate genera de forma automatica el tipo de dato exacto de tipo `numeric(10,2)` lo que resuelve ese problema

**2.4** ¿Cómo hiciste idempotente tu siembra y qué pasaría en el segundo arranque si no
lo fuera? (piensa en la restricción `unique` de `nombre_producto`)

> Esto lo logre envolviendo el codigo de insersión dentro de la condición `if (repository.count() == 0`, esto permite insertar datos solo si latabla esta vacia

---

## Fase 3 — Modelo inmutable y lógica funcional

**3.1** ¿Por qué tienes **dos** clases (`ProductoEntity` y `Producto`) en lugar de una?
¿Qué te impide hacer inmutable directamente la entidad de Hibernate?

> Realice dos clases para separar la capa de persistencia del ORM de la capa de dominio, una esta acoplada a Hibernate y mapea directamente en la BD, la otra esta libre de anotaciones e infraestructura, pura logica de negocio

**3.2** Escribe el código exacto de **tus dos** copias defensivas e indica en qué línea
está cada una.

```java
public Producto(Long id, String nombre, String categoria, BigDecimal precioUsd, List<String> correosNotificacion) {
    this.id = id;
    this.nombre = nombre;
    this.categoria = categoria;
    this.precioUsd = precioUsd;
    // LÍNEA DE COPIA DEFENSIVA DE ENTRADA:
    this.correosNotificacion = (correosNotificacion != null) 
            ? new ArrayList<>(correosNotificacion) 
            : new ArrayList<>();
}

public List<String> getCorreosNotificacion() {
    // LÍNEA DE COPIA DEFENSIVA DE SALIDA:
    return Collections.unmodifiableList(new ArrayList<>(correosNotificacion));
}
```

**3.3** ¿Por qué la copia defensiva **solo en el getter** no sería suficiente? Describe
el ataque concreto que quedaría abierto sobre **tu** clase.

> Si solo pongo la copia defensiva en el getter, el constructor mantendría la referencia a la lista original pasada desde afuera `(this.correosNotificacion = correosNotificacion;)`.
> Ataque concreto: Alguien podría crear un Producto usando una lista local (misCorreos), guardar esa referencia en una variable externa y luego alterarla directamente (`misCorreos.clear()` o `misCorreos.add("hacker@malicioso.com")`).

**3.4** ¿Cómo implementaste `A_MAYUSCULAS` para no mutar el `Producto` recibido?

> Trate que en lugar de intentar modificar el campo nombre de la instancia recibida, la funcion evalua el nombre actual, aplica mayusculas y retorna un objeto producto nuevo

```java
public static final Function<Producto, Producto> A_MAYUSCULAS = producto -> 
        new Producto(
            producto.getId(),
            producto.getNombre() != null ? producto.getNombre().toUpperCase() : null,
            producto.getCategoria(),
            producto.getPrecioUsd(),
            producto.getCorreosNotificacion()
        );
```

---

## Fase 4 — Servicio reactivo y aislamiento del bloqueo

**4.1** Pega tu método `obtenerProductosComercializables()` completo.

```java
public Flux<Producto> obtenerProductosComercializables() {
        return Mono.fromCallable(repository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(ProductoMapper::toDominio)
                .map(ProductoFilters.A_MAYUSCULAS)
                .filter(ProductoFilters.IS_VALID)
                .doOnNext(ProductoFilters.LOG_PRODUCTO)
                .defaultIfEmpty(PRODUCTO_GENERICO);
    }
```

**4.2** ¿Qué pasa **exactamente** si eliminas
`.subscribeOn(Schedulers.boundedElastic())` de ese método? Si lo probaste, indica qué
hilo aparecía en el log antes y después.

> Si esa linea se elimina, la consulta bloqueante de JPA se ejecuta directamente sobre el hilo main, bloqueando el servidor e impidiendo que procese solicitudes nuevas

```java
// Antes
2026-07-31T21:18:31.048-05:00  INFO 22052 --- [agrosmart] [boundedElastic-1] reactor.Flux.DefaultIfEmpty.1            : onNext(com.dlcorrea.agrosmart.domain.Produc...

// Despues
2026-07-31T21:20:42.106-05:00  INFO 15196 --- [agrosmart] [           main] reactor.Flux.DefaultIfEmpty.1            : onNext(com.dlcorrea.agrosmart.domain.Produc...
```

**4.3** ¿Por qué `Mono.fromCallable(...)` y no `Mono.just(repository.findAll())`?
(pista: cuándo se ejecuta cada uno)

> Porque `Mono.just()` evalua su contenido de forma inmediata, lo que bloquearia el hilo principal antes de que exista un suscriptor

**4.4** En **tu** código, ¿dónde usaste `defaultIfEmpty` y dónde `switchIfEmpty`, y por
qué no son intercambiables en esos dos lugares?

> Use `defaultIfEmpty` en la `obtenerProductosComercializables` para emitir un valor estatico de respaldo si el flujo se vacia al aplicar filtros, y `switchIfEmpty` en `buscarPorId` para desviar la ejecución hacia un nuevo publicador que lance mi excepción cuando la BD no encuentre el ID

**4.5** ¿Por qué `doOnNext` no sirve para transformar el elemento, si aparentemente
"recibe" el producto?

> Porque fue diseñado para ejecutar efectos secundarios, como imprimir logs, enviar metricas, etc. Ya que los flujos reactivos son inmutables `doOnNext` no tiene capacidad de insertar un objeto modificado al pipeline

---

## Fase 5 — Módulo de IA con LangChain4j

**5.1** Pega tu interfaz `AgroSmartAIService` completa.

```java
                               AiService
public interface AgroSmartAIService {

    @UserMessage("""
            Redacta una frase publicitaria de máximo 100 caracteres para vender \
            {{producto}} dirigido a {{audiencia}}.""")
    String generarPublicidad(@V("producto") String producto,
                             @V("audiencia") String audiencia);
}
```

**5.2** ¿Qué hace `@V("producto")` y qué pasaría si lo quitaras dejando solo el
parámetro?

> Esta direcciona el parametro del método Java con la variable {{producto}} definida dentro del prompt. Si se omite la IA perderia el vinculo

**5.3** ¿En qué archivo y con qué líneas configuraste el modelo? ¿Por qué **no** hizo
falta declarar un `@Bean`?

> lo configure en `application-prod.properties` y no fue necesario el Bean porque utilice el Starter de Spring Boot de LangChain4j, este implementa la autoconfiguración, escanea el properties e inyecta el bean al modelo

**5.4** ¿Por qué la llamada a la IA también necesita `boundedElastic`, si no es una
consulta a base de datos?

> Porque la interfaz de LangChain4j realiza peticiones HTTP SINCRONAS hacia la API de OpenAI, aunque no es una BD cualqueir operacion de red que ponga el hilo en espera causara el mismo problema

**5.5** Si tu proveedor devolvió un error durante el examen, pega el mensaje real y la
respuesta que produjo tu `onErrorResume`.

```

```

---

## Fase 6 — API reactiva con WebFlux

**6.1** Pega la salida real de tus cuatro `curl`.

```bash
PS C:\Users\dcobe\Code_\Java\PROGAV\agrosmart-final-correa> curl -s http://localhost:8149/api/productos
[{"id":1,"nombre":"QUINUA BLANCA ORGANICA","categoria":"Quinua","precioUsd":3.50,"correosNotificacion":["exportaciones@agrosmart.com.ec"]},{"id":2,"nombre":"QUINUA ROJA DE ALTURA","categoria":"Quinua","precioUsd":4.20,"correosNotificacion":["exportaciones@agrosmart.com.ec"]},{"id":3,"nombre":"QUINUA NEGRA PREMIUM","categoria":"Quinua","precioUsd":5.00,"correosNotificacion":["premium@agrosmart.com.ec"]}]

PS C:\Users\dcobe\Code_\Java\PROGAV\agrosmart-final-correa> curl -s http://localhost:8149/api/productos/1
{"id":1,"nombre":"Quinua Blanca Organica","categoria":"Quinua","precioUsd":3.50,"correosNotificacion":["exportaciones@agrosmart.com.ec"]}

PS C:\Users\dcobe\Code_\Java\PROGAV\agrosmart-final-correa> curl -i http://localhost:8149/api/productos/9999
HTTP/1.1 404 Not Found
Content-Type: application/json
Content-Length: 152
{"codigo":"PRODUCTO_NO_ENCONTRADO","mensaje":"El producto con ID 9999 no fue encontrado en el catálogo.","marcaDeTiempo":"2026-07-31T22:04:09.5807544"}

PS C:\Users\dcobe\Code_\Java\PROGAV\agrosmart-final-correa> curl -s "http://localhost:8149/api/agrosmart/publicidad?producto=Quinua%20organica%20de%20altura&audiencia=tiendas%20de%20alimentacion%20saludable"
"Eleva tu oferta: Quinua orgánica de altura, el superalimento que tus clientes adorarán."
```

**6.2** ¿Cómo lograste que el id inexistente responda **404** y no 500?

> Eso me fue posible ya que use el operador `switchIfEmpty` para emitir una excepcion personalizada, y capture esa excepción a nivel global con un `@RestControllerAdvice` el cual se encarga de interceptarlo y establecer el codigo HTTP

**6.3** ¿Qué pasaría si tu controlador devolviera `List<Producto>` en lugar de
`Flux<Producto>`? ¿Seguiría compilando? ¿Seguiría siendo no bloqueante?

> Primero no compilaria por incompatibilidad de tipos, y definitivamente seria bloqueante, devolver un `List<Producto>` obliga a la aplicación a recolectar todos los elementos en memoria una sola vez antes de armar el HTTP, bloqueando el hilo actual

---

## Fase 7 — Pruebas unitarias

**7.1** Pega la salida real de tus pruebas (`./mvnw test` o `./gradlew test`).

```
 [INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.dlcorrea.agrosmart.domain.ProductoFiltersTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.108 s -- in com.dlcorrea.agrosmart.domain.ProductoFiltersTest
[INFO] Running com.dlcorrea.agrosmart.domain.ProductoTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.014 s -- in com.dlcorrea.agrosmart.domain.ProductoTest
[INFO] Running com.dlcorrea.agrosmart.service.ProductoServiceTest
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your bu
ild as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
WARNING: A Java agent has been loaded dynamically (C:\Users\dcobe\.m2\repository\net\bytebuddy\byte-buddy-agent\1.18.10\byte-buddy-agent-1.18.10.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
Procesando Producto [ID: 1, Nombre: P1]
Procesando Producto [ID: 2, Nombre: P2]
Procesando Producto [ID: 3, Nombre: P3]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.723 s -- in com.dlcorrea.agrosmart.service.ProductoServiceTest
[INFO] Running com.dlcorrea.agrosmart.service.PublicidadServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.128 s -- in com.dlcorrea.agrosmart.service.PublicidadServiceTest
[INFO] 
[INFO] Results:
[INFO]
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.659 s
[INFO] Finished at: 2026-07-31T22:47:08-05:00
[INFO] ------------------------------------------------------------------------

```

**7.2** ¿Cuántos productos espera tu `expectNextCount(...)` y por qué ese número
concreto? Relaciónalo con tu semilla.

> Espera exactamente 3. Esto se debe a que, en la fase "Arrange" de la prueba, simulamos el comportamiento del repositorio para que devuelva una lista inicial de 5 productos: 3 con datos correctos y 2 inválidos (uno con precio cero y otro sin correo)

**7.3** ¿Por qué mockeaste `ProductoRepository` en lugar de dejar que la prueba consulte
PostgreSQL?

> Porque el objetivo de una prueba unitaria es aislar la lógica de negocio (en este caso, ProductoService) de cualquier dependencia externa. Si usara PostgreSQL, la prueba se convertiría en una prueba de integración, haciéndola dependiente de que el motor de base de datos esté encendido

**7.4** ¿Qué demuestra `assertNotSame` que `assertEquals` **no** demuestra en tu prueba
de copia defensiva?

> únicamente verifica que el contenido de las dos listas sea idéntico, pero assertNotSame verifica la identidad de memoria (osea que no sean el mismo objeto). Esto demuestra de forma irrefutable que la lista devuelta por el getter es una instancia completamente nueva en la memoria

**7.5** ¿Por qué una prueba de un `Flux` que no llama a `verifyComplete()` (o a
`verify()`) no está probando nada?

> Un Flux o Mono no hace nada de nada hasta que alguien se suscribe a él. En el entorno de pruebas, verify() y verifyComplete() actúan como la llamada de suscripción y gatillan la evaluación. Sin ellos, el flujo nunca se ejecuta

---

## Fase 8 — Integración y cierre

**8.1** Pega tu `git log --oneline --graph --all`.

```bash
PS C:\Users\dcobe\Code_\Java\PROGAV\agrosmart-final-correa> git log --oneline --graph --all
* 4d1d4b7 (HEAD -> feature/pruebas, origin/feature/pruebas) test: agrega pruebas del modelo, logica funcional, flujo reactivo e ia
* fe004bf (origin/feature/api-reactiva, feature/api-reactiva) feat: expone endpoints reactivos y de publicidad
* 51b3e6c (origin/feature/ia-langchain4j, feature/ia-langchain4j) feat: integra langchain4j para publicidad de productos
* c4d32a5 (origin/feature/servicio-reactivo, feature/servicio-reactivo) feat: implementa servicio reactivo con boundedElastic y operadores + bonus manejo errores     
* 37a12e7 (origin/feature/modelo-inmutable, feature/modelo-inmutable) feat: agrega modelo inmutable de producto y logica funcional
* 51f7ff7 (origin/feature/persistencia-jpa, feature/persistencia-jpa) feat: agrega entidad jpa de productos y siembra de datos
* 1983405 (origin/feature/config-perfiles, feature/config-perfiles) chore: configura perfil prod con postgresql y puerto propio
* 58a6aff (origin/main, origin/HEAD, main) chore: inicializa proyecto agrosmart y registra identidad del examen
* fc62fdf Initial commit
```

**8.2** ¿Qué fase te tomó más tiempo del previsto y por qué?

> La fase 4 definitivamente me demore en implementar ya que ademas de que no terminaba muy bien de comprender bien los hilos y el bloqueo, tambien tuve un error con git y las ramas y casi hago un push en una rama que no era, entonces entre en panico y demore mas por eso

**8.3** Si tuvieras 30 minutos más, ¿qué mejorarías **primero** de tu entrega y por qué
esa y no otra?

> Si tuviera 30 minutos más mejoraria el manejo de errores en la aplicación siento que no la probe lo suficiente, adicional tambien me gustaria hacer mas pruebas a la base de datos para el manejo de excepciones

**8.4** Declara honestamente qué herramientas consultaste durante el examen
(documentación, apuntes, asistentes de IA) y para qué. **Esta declaración no descuenta
puntaje**; su omisión o falsedad sí constituye falta de honestidad académica.

> Para resolver el error con git y para guiarme durante este proyecto con los commits y las branch use Gemini en la web
