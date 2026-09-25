# Primer Parcial SQA - Sistema de Gestión de Inventarios (inventory-sp3)

Proyecto Spring Boot para la gestión de inventarios (categorías y productos), desarrollado para el marco del Primer Parcial de Software Quality Assurance (SQA) de la Universidad Privada del Valle.

## Requisitos del Sistema

- JDK 17 (OpenJDK 17)
- Apache Maven 3.9+
- Postman (para pruebas funcionales E2E)
- Navegador web (para visualizar el reporte de cobertura JaCoCo)

## Instrucciones para Ejecutar el Proyecto

1. **Clonar o descargar el repositorio:**
   Navegar a la carpeta del proyecto `inventory-sp3`.

2. **Configurar las variables de entorno (Java 17):**
   En PowerShell (Windows):
   ```powershell
   $env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
   $env:PATH="$env:JAVA_HOME\bin;$env:PATH"
   ```

3. **Compilar el proyecto:**
   ```bash
   mvn clean compile
   ```

4. **Iniciar la aplicación Spring Boot:**
   ```bash
   mvn spring-boot:run
   ```
   El servidor se iniciará en el puerto 8080. La API REST estará disponible en:
   `http://localhost:8080/api/v1`

---

## Instrucciones para Ejecutar las Pruebas

### 1. Pruebas Unitarias e Integración (JUnit 5 + Mockito)
Para ejecutar los 98 métodos de prueba automatizada y generar el reporte de cobertura de código JaCoCo:

```bash
mvn clean test jacoco:report
```

Al finalizar la ejecución, el resultado en consola indicará `BUILD SUCCESS` (98 pruebas pasadas, 0 fallos, 0 errores).

### 2. Visualización del Reporte de Cobertura JaCoCo
El reporte en formato HTML se genera en la siguiente ruta relativa:

`target/site/jacoco/index.html`

Para abrir el reporte en el navegador predeterminado (PowerShell):
```powershell
Start-Process "target/site/jacoco/index.html"
```

### 3. Pruebas Funcionales End-to-End (Postman)
1. Iniciar la aplicación (`mvn spring-boot:run`).
2. Abrir la herramienta Postman.
3. Importar la colección ubicada en `postman/examen-inventory.postman_collection.json`.
4. Importar el entorno ubicado en `postman/local.postman_environment.json`.
5. Ejecutar la colección utilizando el Collection Runner. Las 25 peticiones HTTP deben finalizar con estado PASS (57 afirmaciones verificadas).

---

## Estructura del Proyecto

```text
inventory-sp3/
├── src/
│   ├── main/java/com/company/inventory/
│   │   ├── controller/      # Controladores REST (CategoryRestController, ProductRestController)
│   │   ├── dao/             # Repositorios JPA (ICategoryDao, IProductDao)
│   │   ├── model/           # Entidades (Category, Product)
│   │   ├── respnose/        # Clases de respuesta (ResponseRest)
│   │   ├── services/        # Lógica de negocio (CategoryServiceImpl, ProductServiceImpl)
│   │   └── util/            # Utilitarios (ZLib y exportación Excel)
│   └── test/java/com/company/inventory/  # Pruebas unitarias e integración (89 casos)
├── postman/                 # Colección y entorno de Postman (25 casos E2E)
├── pom.xml                  # Configuración Maven y dependencias
└── README.md
```

---

## Resumen de Cobertura de Código (JaCoCo)

- Capa Controller: 100% de cobertura
- Capa Service: 100% de cobertura
- Capa Util / Exporters: 100% de cobertura
- Capa Dao / Models / Responses: 100% de cobertura
- Cobertura Global del Proyecto: 99.00% (BUILD SUCCESS)

---

## Documentos Anexos Entregables

- Informe_Parcial_1_Actualizado.docx: Informe principal consolidador.
- Plan_de_Pruebas_Inventory_Final.docx: Plan de Pruebas oficial en formato Light (HU01-HU08 Gherkin).
- Casos_de_Prueba_Inventory.xlsx: Anexo A con 114 casos de prueba detallados.
- Reporte_de_Bugs_Inventory.docx: Anexo B con reporte de defectos y riesgos.
