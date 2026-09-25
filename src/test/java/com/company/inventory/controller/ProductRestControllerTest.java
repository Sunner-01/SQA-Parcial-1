package com.company.inventory.controller;

import com.company.inventory.model.*;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.services.IProductService;
import com.company.inventory.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.io.*;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductRestControllerTest {
    @Mock IProductService service;
    @InjectMocks ProductRestController controller;
    MockMvc mvc;
    @BeforeEach void setup() { mvc=MockMvcBuilders.standaloneSetup(controller).build(); }
    private ResponseEntity<ProductResponseRest> response(int status) {
        Product p=new Product(); p.setId(8L); p.setName("Leche"); p.setPrice(12); p.setAccount(5); p.setCategory(new Category(4L,"Lacteos","Leche"));
        ProductResponseRest r=new ProductResponseRest(); r.getProduct().setProducts(List.of(p)); r.setMetadata("Resultado",status==200?"00":"-1","Prueba");
        return ResponseEntity.status(status).body(r);
    }
    @Test void listReturnsServiceData() throws Exception {
        when(service.search()).thenReturn(response(200)); mvc.perform(get("/api/v1/products")).andExpect(status().isOk()).andExpect(jsonPath("$.product.products[0].name").value("Leche")); verify(service).search();
    }
    @Test void listPropagates500() throws Exception { when(service.search()).thenReturn(response(500)); mvc.perform(get("/api/v1/products")).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.metadata[0].code").value("-1")); }
    @Test void findsById() throws Exception { when(service.searchById(8L)).thenReturn(response(200)); mvc.perform(get("/api/v1/products/8")).andExpect(status().isOk()).andExpect(jsonPath("$.product.products[0].id").value(8)); verify(service).searchById(8L); }
    @Test void missingIdReturns404() throws Exception { when(service.searchById(8L)).thenReturn(response(404)); mvc.perform(get("/api/v1/products/8")).andExpect(status().isNotFound()); }
    @Test void filtersByName() throws Exception { when(service.searchByName("Leche")).thenReturn(response(200)); mvc.perform(get("/api/v1/products/filter/Leche")).andExpect(status().isOk()).andExpect(jsonPath("$.product.products[0].name").value("Leche")); verify(service).searchByName("Leche"); }
    @Test void missingNameReturns404() throws Exception { when(service.searchByName("x")).thenReturn(response(404)); mvc.perform(get("/api/v1/products/filter/x")).andExpect(status().isNotFound()); }
    @Test void deletesById() throws Exception { when(service.deleteById(8L)).thenReturn(response(200)); mvc.perform(delete("/api/v1/products/8")).andExpect(status().isOk()); verify(service).deleteById(8L); }
    @Test void deletePropagatesFailure() throws Exception { when(service.deleteById(8L)).thenReturn(response(500)); mvc.perform(delete("/api/v1/products/8")).andExpect(status().isInternalServerError()); }
    @Test void multipartSaveMapsFieldsAndCompressesPicture() throws Exception {
        when(service.save(any(),eq(4L))).thenReturn(response(200)); byte[] bytes={1,2,3};
        mvc.perform(multipart("/api/v1/products").file(new MockMultipartFile("picture","test.png","image/png",bytes))
            .param("name","Leche").param("price","12").param("account","5").param("categoryId","4"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.product.products[0].id").value(8));
        ArgumentCaptor<Product> cap=ArgumentCaptor.forClass(Product.class); verify(service).save(cap.capture(),eq(4L));
        assertEquals("Leche",cap.getValue().getName()); assertEquals(12,cap.getValue().getPrice()); assertEquals(5,cap.getValue().getAccount());
        assertArrayEquals(bytes,Util.decompressZLib(cap.getValue().getPicture()));
    }
    @Test void multipartUpdateMapsFieldsAndId() throws Exception {
        when(service.update(any(),eq(4L),eq(8L))).thenReturn(response(200)); byte[] bytes={5,6};
        mvc.perform(multipart(HttpMethod.PUT,"/api/v1/products/8").file(new MockMultipartFile("picture",bytes))
            .param("name","Queso").param("price","25").param("account","9").param("categoryId","4"))
            .andExpect(status().isOk());
        ArgumentCaptor<Product> cap=ArgumentCaptor.forClass(Product.class); verify(service).update(cap.capture(),eq(4L),eq(8L));
        assertEquals("Queso",cap.getValue().getName()); assertEquals(25,cap.getValue().getPrice()); assertEquals(9,cap.getValue().getAccount());
        assertArrayEquals(bytes,Util.decompressZLib(cap.getValue().getPicture()));
    }
    @Test void missingFileIs400WithoutCallingService() throws Exception {
        mvc.perform(multipart("/api/v1/products").param("name","Leche").param("price","12").param("account","5").param("categoryId","4"))
            .andExpect(status().isBadRequest()); verifyNoInteractions(service);
    }
    @Test void invalidPriceIs400WithoutCallingService() throws Exception {
        mvc.perform(multipart("/api/v1/products").file(new MockMultipartFile("picture",new byte[]{1})).param("name","Leche").param("price","abc").param("account","5").param("categoryId","4"))
            .andExpect(status().isBadRequest()); verifyNoInteractions(service);
    }
    @Test void invalidIdIs400() throws Exception { mvc.perform(get("/api/v1/products/abc")).andExpect(status().isBadRequest()); verifyNoInteractions(service); }
    @Test void exportsRealWorkbookAndHeaders() throws Exception {
        when(service.search()).thenReturn(response(200));
        byte[] bytes=mvc.perform(get("/api/v1/products/export/excel")).andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition","attachment; filename=result_product.xlsx"))
            .andExpect(content().contentType("application/octet-stream")).andReturn().getResponse().getContentAsByteArray();
        try(XSSFWorkbook book=new XSSFWorkbook(new ByteArrayInputStream(bytes))) { assertEquals("Leche",book.getSheet("Resultado").getRow(1).getCell(1).getStringCellValue()); }
    }
    @Test void fileReadFailureDoesNotCallService() throws Exception {
        org.springframework.web.multipart.MultipartFile file=mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("Lectura fallida"));
        assertThrows(IOException.class,()->controller.save(file,"Leche",12,5,4L)); verifyNoInteractions(service);
    }
}
