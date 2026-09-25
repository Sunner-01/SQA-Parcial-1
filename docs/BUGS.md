# Hallazgos y reporte de bugs

No se entregó la plantilla oficial del docente; trasladar estos campos a ella. No se modificó el código de producción. Los estados distinguen lo ejecutado de lo observado en revisión.

## ENV01 Incompatibilidad con Java 21

- Tipo: entorno; prioridad alta para iniciar el trabajo.
- Precondición: proyecto original con Lombok administrado por Spring Boot 3.1.0; JAVA_HOME apunta a JDK 21.
- Pasos: ejecutar `mvnw.cmd test`.
- Esperado: compilación con el JDK solicitado (17).
- Real: usando 21, compilación falla con `NoSuchFieldError JCTree$JCImport qualid`.
- Estado: reproducido; resuelto para la ejecución configurando JDK 17, sin modificar dependencias.
- Evidencia: ejecución inicial de preparación; ejecución con 17 en `evidencias/maven-verificado.log`.

## BUG01 Cabecera ZLib corrupta se silencia

- Módulo: `Util.decompressZLib`; prioridad propuesta media.
- Entrada: bytes `{1,2,3,4}`.
- Pasos: ejecutar `UtilAdditionalTest.malformedHeaderReturnsEmptyInsteadOfReportingError`.
- Real: retorna un arreglo vacío; captura `DataFormatException` sin comunicar el fallo.
- Esperado propuesto: informar entrada inválida y definir cómo debe responder la API; criterio pendiente de aprobación del docente.
- Estado: comportamiento reproducido por test de caracterización; no corregido.
- Solución propuesta: contrato explícito de error y liberación de `Inflater` en `finally`, con tests de regresión.

## RIESGO02 Descompresión sin progreso

- Módulo: bucle `while (!inflater.finished())` de `Util.decompressZLib`; prioridad propuesta alta.
- Condición: flujo vacío o truncado que deja `inflate()` en cero sin `finished()`.
- Hallazgo: no se comprueba `needsInput`, `needsDictionary` ni falta de progreso. El bucle puede no terminar.
- Estado: identificado por inspección del código; no ejecutado sobre la API para no bloquear una petición. No confundir vacío **comprimido correctamente**, que sí se prueba, con cero bytes de entrada ZLib.
- Reproducción pendiente: proceso Java separado con límite externo de tiempo; no introducir un test que cuelgue la suite.
- Solución propuesta: detectar estado sin progreso, lanzar error controlado y liberar recursos.

## RIESGO03 Exportación no maneja fallo del servicio

- Módulo: ambos métodos `exportToExcel` de controladores; prioridad propuesta media.
- Condición: `service.search()` devuelve 500 o lista nula.
- Hallazgo: el controlador accede directamente al cuerpo/lista sin validar estado y luego inicia exportación.
- Estado: revisión estática; pendiente prueba específica de regresión y decisión del contrato HTTP.
- Solución propuesta: comprobar estado y lista antes de escribir el archivo; propagar un error consistente.

## Observaciones de contrato

Categorías vacías retornan 200; productos vacíos retornan 404. DELETE no verifica previamente existencia. Esas diferencias no se declaran bugs automáticamente: hace falta acordar criterios de aceptación. No se probaron reglas de precios negativos, autenticación o límites de carga como si ya fueran requisitos documentados.

## Campos para nuevos bugs

ID, título, ejecutor, fecha, versión/commit, módulo, severidad, prioridad, precondiciones, datos, pasos, esperado, real, evidencia, responsable, estado, solución propuesta y resultado de reejecución. Completar con datos reales; no marcar como resuelto un hallazgo solo por haberlo documentado.
