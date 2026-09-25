package com.company.inventory.controller;

import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponseRest;
import com.company.inventory.services.ICategoryService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerAdditionalTest {
    @Mock ICategoryService service;
    @InjectMocks CategoryRestController controller;
    MockMvc mvc;
    @BeforeEach void setup() { mvc=MockMvcBuilders.standaloneSetup(controller).build(); }
    @Test void exportsWorkbook() throws Exception {
        CategoryResponseRest r=new CategoryResponseRest(); r.getCategoryResponse().setCategory(List.of(new Category(7L,"Lacteos","Leche")));
        when(service.search()).thenReturn(ResponseEntity.ok(r));
        byte[] bytes=mvc.perform(get("/api/v1/categories/export/excel")).andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition","attachment; filename=result_category.xlsx"))
            .andExpect(content().contentType("application/octet-stream")).andReturn().getResponse().getContentAsByteArray();
        try(XSSFWorkbook book=new XSSFWorkbook(new ByteArrayInputStream(bytes))) { assertEquals("Lacteos",book.getSheet("Resultado").getRow(1).getCell(1).getStringCellValue()); }
        verify(service).search();
    }
    @Test void missingCategoryPropagates404() throws Exception {
        when(service.searchById(7L)).thenReturn(ResponseEntity.status(404).body(new CategoryResponseRest()));
        mvc.perform(get("/api/v1/categories/7")).andExpect(status().isNotFound());
    }
    @Test void malformedJsonIs400() throws Exception {
        mvc.perform(post("/api/v1/categories").contentType("application/json").content("{bad"))
            .andExpect(status().isBadRequest()); verifyNoInteractions(service);
    }
    @Test void invalidIdIs400() throws Exception { mvc.perform(get("/api/v1/categories/abc")).andExpect(status().isBadRequest()); verifyNoInteractions(service); }
}
