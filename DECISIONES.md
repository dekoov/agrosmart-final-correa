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

```

**4.2** ¿Qué pasa **exactamente** si eliminas
`.subscribeOn(Schedulers.boundedElastic())` de ese método? Si lo probaste, indica qué
hilo aparecía en el log antes y después.

>

**4.3** ¿Por qué `Mono.fromCallable(...)` y no `Mono.just(repository.findAll())`?
(pista: cuándo se ejecuta cada uno)

>

**4.4** En **tu** código, ¿dónde usaste `defaultIfEmpty` y dónde `switchIfEmpty`, y por
qué no son intercambiables en esos dos lugares?

>

**4.5** ¿Por qué `doOnNext` no sirve para transformar el elemento, si aparentemente
"recibe" el producto?

>

---

## Fase 5 — Módulo de IA con LangChain4j

**5.1** Pega tu interfaz `AgroSmartAIService` completa.

```java

```

**5.2** ¿Qué hace `@V("producto")` y qué pasaría si lo quitaras dejando solo el
parámetro?

>

**5.3** ¿En qué archivo y con qué líneas configuraste el modelo? ¿Por qué **no** hizo
falta declarar un `@Bean`?

>

**5.4** ¿Por qué la llamada a la IA también necesita `boundedElastic`, si no es una
consulta a base de datos?

>

**5.5** Si tu proveedor devolvió un error durante el examen, pega el mensaje real y la
respuesta que produjo tu `onErrorResume`.

```

```

---

## Fase 6 — API reactiva con WebFlux

**6.1** Pega la salida real de tus cuatro `curl`.

```

```

**6.2** ¿Cómo lograste que el id inexistente responda **404** y no 500?

>

**6.3** ¿Qué pasaría si tu controlador devolviera `List<Producto>` en lugar de
`Flux<Producto>`? ¿Seguiría compilando? ¿Seguiría siendo no bloqueante?

>

---

## Fase 7 — Pruebas unitarias

**7.1** Pega la salida real de tus pruebas (`./mvnw test` o `./gradlew test`).

```

```

**7.2** ¿Cuántos productos espera tu `expectNextCount(...)` y por qué ese número
concreto? Relaciónalo con tu semilla.

>

**7.3** ¿Por qué mockeaste `ProductoRepository` en lugar de dejar que la prueba consulte
PostgreSQL?

>

**7.4** ¿Qué demuestra `assertNotSame` que `assertEquals` **no** demuestra en tu prueba
de copia defensiva?

>

**7.5** ¿Por qué una prueba de un `Flux` que no llama a `verifyComplete()` (o a
`verify()`) no está probando nada?

>

---

## Fase 8 — Integración y cierre

**8.1** Pega tu `git log --oneline --graph --all`.

```

```

**8.2** ¿Qué fase te tomó más tiempo del previsto y por qué?

>

**8.3** Si tuvieras 30 minutos más, ¿qué mejorarías **primero** de tu entrega y por qué
esa y no otra?

>

**8.4** Declara honestamente qué herramientas consultaste durante el examen
(documentación, apuntes, asistentes de IA) y para qué. **Esta declaración no descuenta
puntaje**; su omisión o falsedad sí constituye falta de honestidad académica.

>
