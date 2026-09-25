package com.company.inventory.services;

import com.company.inventory.dao.ICategoryDao;
import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponseRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceAdditionalTest {
    @Mock ICategoryDao dao;
    @InjectMocks CategoryServiceImpl service;
    private Category category() { return new Category(7L, "Lacteos", "Leche y queso"); }
    private void check(ResponseEntity<CategoryResponseRest> r, int status) {
        assertEquals(status, r.getStatusCode().value());
        assertNotNull(r.getBody());
        assertEquals(status == 200 ? "00" : "-1", r.getBody().getMetadata().get(0).get("code"));
    }
    @Test void findsExistingCategory() {
        Category c = category(); when(dao.findById(7L)).thenReturn(Optional.of(c));
        var r = service.searchById(7L); check(r, 200);
        assertSame(c, r.getBody().getCategoryResponse().getCategory().get(0));
        verify(dao).findById(7L);
    }
    @Test void missingCategoryReturns404() { when(dao.findById(7L)).thenReturn(Optional.empty()); check(service.searchById(7L),404); }
    @Test void lookupFailureReturns500() { when(dao.findById(7L)).thenThrow(new IllegalStateException("DB")); check(service.searchById(7L),500); }
    @Test void updatesFieldsAndPreservesId() {
        Category old = category(); Category input = new Category(null,"Nuevo","Descripcion nueva");
        when(dao.findById(7L)).thenReturn(Optional.of(old)); when(dao.save(old)).thenReturn(old);
        var r = service.update(input,7L); check(r,200);
        assertEquals(7L,old.getId()); assertEquals("Nuevo",old.getName()); assertEquals("Descripcion nueva",old.getDescription());
        assertSame(old,r.getBody().getCategoryResponse().getCategory().get(0)); verify(dao).save(old);
    }
    @Test void missingUpdateNeverSaves() { check(service.update(category(),7L),404); verify(dao,never()).save(any()); }
    @Test void nullUpdateResultReturns400() {
        when(dao.findById(7L)).thenReturn(Optional.of(category())); when(dao.save(any())).thenReturn(null);
        check(service.update(category(),7L),400);
    }
    @Test void updateLookupFailureReturns500() { when(dao.findById(7L)).thenThrow(new IllegalStateException("DB")); check(service.update(category(),7L),500); }
    @Test void updateSaveFailureReturns500() {
        when(dao.findById(7L)).thenReturn(Optional.of(category())); when(dao.save(any())).thenThrow(new IllegalStateException("DB"));
        check(service.update(category(),7L),500);
    }
    @Test void deletesRequestedId() { check(service.deleteById(7L),200); verify(dao).deleteById(7L); }
    @Test void deleteFailureReturns500() { doThrow(new IllegalStateException("DB")).when(dao).deleteById(7L); check(service.deleteById(7L),500); }
    @Test void emptyListIsSuccessful() { when(dao.findAll()).thenReturn(java.util.List.of()); var r=service.search(); check(r,200); assertTrue(r.getBody().getCategoryResponse().getCategory().isEmpty()); }
}
