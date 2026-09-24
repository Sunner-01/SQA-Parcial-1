package com.company.inventory.util;

import com.company.inventory.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExcelExportersTest {
    @Test void categoryWorkbookPreservesHeadersAndRows() throws Exception {
        var response=new MockHttpServletResponse();
        new CategoryExcelExporter(List.of(new Category(7L,"Lacteos","Leche"),new Category(8L,"Pan","Integral"))).export(response);
        try(var book=new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            var s=book.getSheet("Resultado"); assertEquals(3,s.getPhysicalNumberOfRows());
            assertEquals("ID",s.getRow(0).getCell(0).getStringCellValue()); assertEquals("Nombre",s.getRow(0).getCell(1).getStringCellValue());
            assertEquals("Descripción",s.getRow(0).getCell(2).getStringCellValue());
            assertEquals("7",s.getRow(1).getCell(0).getStringCellValue()); assertEquals("Lacteos",s.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Leche",s.getRow(1).getCell(2).getStringCellValue()); assertEquals("Pan",s.getRow(2).getCell(1).getStringCellValue());
        }
    }
    @Test void productWorkbookContainsNumericPriceQuantityAndCategory() throws Exception {
        Product p=new Product(); p.setId(8L); p.setName("Leche"); p.setPrice(12); p.setAccount(5); p.setCategory(new Category(7L,"Lacteos","Leche"));
        var response=new MockHttpServletResponse(); new ProductExcelExporter(List.of(p)).export(response);
        try(var book=new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            var s=book.getSheet("Resultado"); assertEquals(2,s.getPhysicalNumberOfRows());
            String[] headers={"ID","Nombre","Precio","Cantidad","Categoría"};
            for(int i=0;i<headers.length;i++) assertEquals(headers[i],s.getRow(0).getCell(i).getStringCellValue());
            var row=s.getRow(1); assertEquals("8",row.getCell(0).getStringCellValue()); assertEquals("Leche",row.getCell(1).getStringCellValue());
            assertEquals(12,row.getCell(2).getNumericCellValue()); assertEquals(5,row.getCell(3).getNumericCellValue()); assertEquals("Lacteos",row.getCell(4).getStringCellValue());
        }
    }
    @Test void emptyExportsStillHaveHeaders() throws Exception {
        var cats=new MockHttpServletResponse(); var prods=new MockHttpServletResponse();
        new CategoryExcelExporter(List.of()).export(cats); new ProductExcelExporter(List.of()).export(prods);
        for(var response:List.of(cats,prods)) try(var book=new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            assertEquals(1,book.getSheet("Resultado").getPhysicalNumberOfRows());
        }
    }
    @Test void outputFailurePropagatesForBothExporters() throws Exception {
        var response=mock(jakarta.servlet.http.HttpServletResponse.class); when(response.getOutputStream()).thenThrow(new IOException("Salida fallida"));
        assertThrows(IOException.class,()->new CategoryExcelExporter(List.of()).export(response));
        assertThrows(IOException.class,()->new ProductExcelExporter(List.of()).export(response));
    }
}
