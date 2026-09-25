# Plan y casos de prueba

> **Responsable:** Jesus Zeballos  
> **Rol:** Persona 5 — Tester funcional y documentación (Postman, casos e informe)  
> **Fecha:** Septiembre 2026  
> **Estado:** Ejecutado y verificado (57/57 aserciones aprobadas)

## Objetivo y alcance

Verificar la gestión de categorías y productos de Inventory y su exportación, detectando regresiones en resultados, códigos HTTP y tratamiento de errores. Se seleccionan los dos controladores porque exponen los casos de uso; los dos servicios porque coordinan repositorios y respuestas; y las tres utilidades porque transforman imágenes y generan los archivos descargables.

El alcance incluye `CategoryRestController`, `ProductRestController`, `CategoryServiceImpl`, `ProductServiceImpl`, `Util`, `CategoryExcelExporter` y `ProductExcelExporter`. No incluye interfaz gráfica, carga, seguridad ni cambios de reglas de negocio. El comportamiento existente sirve como contrato de caracterización; los comportamientos dudosos se registran como hallazgos y se consultan con el docente.

## Estrategia

JUnit 5 organiza las pruebas. Mockito sustituye DAOs en servicios y servicios en controladores. `MockMvc` standalone prueba el enlace HTTP y multipart sin servidor ni base real. `ArgumentCaptor` comprueba datos enviados al servicio y `verify` verifica operaciones, incluidas las que no deben ejecutarse. Las pruebas de utilidades usan transformaciones y libros Apache POI reales en memoria. Se preservan las pruebas originales de DAO/contexto con H2: son de integración, no unitarias puras.

Postman/Newman ejecuta la API completa sobre MySQL con datos temporales. No se usan mocks para afirmar que la persistencia real funciona. La meta sugerida de 85–95 % se evalúa con líneas y se reportan también instrucciones y ramas, explicando el alcance. Los resultados exactos están en `RESULTADOS.md` y no deben confundirse con la cobertura de una ejecución parcial.

## Historias derivadas de los métodos HTTP

| Historia | Como encargado de inventario quiero… | Endpoints y aceptación |
|---|---|---|
| HU01 | Crear una categoría y consultarla | POST/GET `/categories`, GET `/categories/{id}`; devuelve datos creados |
| HU02 | Corregir y retirar categorías | PUT/DELETE `/categories/{id}`; cambio visible y consulta posterior al borrado 404 |
| HU03 | Descargar categorías | GET `/categories/export/excel`; archivo XLSX con ID, nombre y descripción |
| HU04 | Registrar productos en una categoría con imagen | POST `/products` multipart; valores y relación correctos; categoría inexistente 404 |
| HU05 | Consultar y filtrar productos | GET `/products`, `/products/{id}`, `/products/filter/{name}`; contenido e imagen correctos |
| HU06 | Actualizar y eliminar productos | PUT/DELETE `/products/{id}`; cambios persistidos, borrado comprobable |
| HU07 | Exportar productos | GET `/products/export/excel`; ID, nombre, precio, cantidad y categoría |
| HU08 | Recibir una respuesta comprensible ante errores | JSON o tipos inválidos 400; recursos ausentes 404; errores de DAO simulados 500 |

## Casos unitarios y de componente

Precondición común: JDK 17 y dependencias Maven. En cada test se preparan nuevos mocks/datos; no se depende del orden. Resultado real: ejecutar y consultar Surefire. Los casos siguientes agrupan escenarios, no equivalen uno a uno al número de tests.

| ID / HU | Entrada o condición y pasos | Resultado esperado | Responsable / evidencia |
|---|---|---|---|
| CU01 / HU01 | DAO retorna lista de categorías; llamar search | 200, lista exacta; lista vacía también 200 | 2 / CategoryServiceImplTest y Additional |
| CU02 / HU01 | findById retorna categoría o Optional.empty | 200 y objeto, o 404 con código -1 | 2 / CategoryServiceAdditionalTest |
| CU03 / HU01 | save retorna entidad, null o lanza excepción | 200 con entidad, 400 o 500 respectivamente | 2 / CategoryServiceImplTest |
| CU04 / HU02 | Actualizar categoría existente con nombre/descripcion nuevos | Conserva ID y persiste ambos campos | 2 / CategoryServiceAdditionalTest |
| CU05 / HU02 | Actualizar ausente, save null o DAO falla | 404 sin save, 400 o 500 | 2 / CategoryServiceAdditionalTest |
| CU06 / HU02 | deleteById termina o lanza excepción | 200/500 y llamada al ID solicitado | 2 / CategoryServiceAdditionalTest |
| CU07 / HU04 | Producto válido y categoría existente | 200, asociación correcta y save verificado | 3 / ProductServiceImplTest |
| CU08 / HU04 | Categoría ausente; save null; fallo de DAO | 404 sin guardar, 400, 500 | 3 / ProductServiceImplTest |
| CU09 / HU05 | Buscar/listar/filtrar productos con imagen comprimida | 200 y bytes originales descomprimidos | 3 / ProductServiceImplTest |
| CU10 / HU05 | Producto/lista/filtro ausente o DAO falla | 404 o 500 según escenario | 3 / ProductServiceImplTest |
| CU11 / HU06 | Actualizar nombre/precio/cantidad/imagen/categoría | Todos los campos cambian y el ID se conserva | 3 / ProductServiceImplTest |
| CU12 / HU06 | Actualización con categoría/producto ausente o save null | 404 sin save indebido, o 400 | 3 / ProductServiceImplTest |
| CU13 / HU06 | Borrado normal o fallo de DAO | 200 o 500, ID verificado | 3 / ProductServiceImplTest |
| CU14 / HU01–08 | Invocar rutas con servicio simulado | Estado y JSON del servicio propagados | 2 y 3 / tests controller |
| CU15 / HU04,06 | POST y PUT multipart con datos válidos | Captura de campos y compresión reversible | 3 / ProductRestControllerTest |
| CU16 / HU08 | Sin archivo, precio abc, ID abc o JSON mal formado | 400 sin llamar al servicio | 2 y 3 / tests controller |
| CU17 / HU03,07 | Exportar listas con datos y abrir XLSX con POI | Hojas/cabeceras/filas/celdas y números exactos | 4 / ExcelExportersTest |
| CU18 / HU03,07 | Exportar listas vacías o salida que lanza IOException | Cabeceras sin datos; excepción propagada | 4 / ExcelExportersTest |
| CU19 / HU04,05 | Comprimir/descomprimir datos binarios de 8192 bytes y vacío | Recuperación exacta, varios bloques | 4 / UtilAdditionalTest |
| CU20 / HU08 | Entrada null o cabecera ZLib corrupta | NPE para null; corrupta devuelve vacío (caracterización, ver BUGS) | 4 / UtilAdditionalTest |

## Casos funcionales Postman

Precondición: Docker, MySQL con `db_inventory`, API en puerto 8080 y fixture PNG seleccionado. Ejecutar toda la colección en orden; los IDs se obtienen de POST. Nunca usar IDs manuales de registros ajenos.

| ID / requests | Pasos y datos | Resultado esperado |
|---|---|---|
| CF01 / 01–03 | Crear categoría con nombre único, listar, consultar | 200, ID capturado, categoría presente y nombre persistido |
| CF02 / 04–05 | Actualizar categoría y volver a leer | 200 y nuevo nombre/descripción |
| CF03 / 06–09 | Crear producto multipart, consultar, listar y filtrar | 200, asociación, precio 12, cantidad 5, PNG original |
| CF04 / 10–11 | Actualizar a Queso SQA, precio 25, cantidad 9 y releer | 200 y valores persistidos |
| CF05 / 12–13 | Descargar Excel de ambas entidades | 200, Content-Disposition y firma ZIP; celdas verificadas en tests POI |
| CF06 / 14–17 | Consultar ID 0; crear sin categoría; actualizar ID 0 | 404 con metadata -1 |
| CF07 / 18–21 | Omitir archivo, precio abc, ID abc, JSON truncado | 400 |
| CF08 / 22–25 | Borrar producto propio y luego categoría; consultar ambos | DELETE 200; cada GET posterior 404 |

## Registro de ejecución y cierre

### Datos de ejecución (Persona 5)
- **Ejecutor:** Jesus Zeballos
- **Fecha de ejecución:** 24/09/2026
- **Entorno:** Local (MySQL vía XAMPP en puerto 3306, API en puerto 8080)
- **Comando ejecutado:** `npx --yes newman run postman/examen-inventory.postman_collection.json -e postman/local.postman_environment.json --working-dir .`
- **Resultado:** 25 peticiones ejecutadas, 57 aserciones aprobadas, 0 fallos (100% Passed).
- **Evidencias adjuntas:** `evidencias/persona-5/captura-postman.png`, `evidencias/newman-verificado.json`, `evidencias/postman-verificado.log`.