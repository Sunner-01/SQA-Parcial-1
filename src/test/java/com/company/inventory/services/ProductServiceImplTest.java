package com.company.inventory.services;

import com.company.inventory.dao.*;
import com.company.inventory.model.*;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.util.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock ICategoryDao categories;
    @Mock IProductDao products;
    @InjectMocks ProductServiceImpl service;
    private final byte[] picture = {1,2,3,4};
    private Category category() { return new Category(4L,"Lacteos","Leche"); }
    private Product product() {
        Product p = new Product(); p.setId(8L); p.setName("Leche"); p.setPrice(12); p.setAccount(5);
        p.setCategory(category()); p.setPicture(Util.compressZLib(picture)); return p;
    }
    private void check(ResponseEntity<ProductResponseRest> r,int status) {
        assertEquals(status,r.getStatusCode().value()); assertNotNull(r.getBody());
        assertEquals(status==200?"00":"-1",r.getBody().getMetadata().get(0).get("code"));
    }
    private void foundCategory() { when(categories.findById(4L)).thenReturn(Optional.of(category())); }
    @Test void savesWithCategory() {
        foundCategory(); Product p=product(); when(products.save(p)).thenReturn(p);
        var r=service.save(p,4L); check(r,200); assertEquals(4L,p.getCategory().getId());
        assertSame(p,r.getBody().getProduct().getProducts().get(0)); verify(products).save(p);
    }
    @Test void missingCategoryDoesNotSave() { check(service.save(product(),4L),404); verifyNoInteractions(products); }
    @Test void nullSaveReturns400() { foundCategory(); check(service.save(product(),4L),400); }
    @Test void saveExceptionReturns500() { foundCategory(); when(products.save(any())).thenThrow(new IllegalStateException("DB")); check(service.save(product(),4L),500); }
    @Test void categoryLookupExceptionReturns500() { when(categories.findById(4L)).thenThrow(new IllegalStateException("DB")); check(service.save(product(),4L),500); verifyNoInteractions(products); }
    @Test void findsAndDecompressesImage() {
        when(products.findById(8L)).thenReturn(Optional.of(product())); var r=service.searchById(8L); check(r,200);
        assertArrayEquals(picture,r.getBody().getProduct().getProducts().get(0).getPicture()); verify(products).findById(8L);
    }
    @Test void missingProductReturns404() { check(service.searchById(8L),404); }
    @Test void findExceptionReturns500() { when(products.findById(8L)).thenThrow(new IllegalStateException("DB")); check(service.searchById(8L),500); }
    @Test void listsAndDecompressesAllPictures() {
        when(products.findAll()).thenReturn(List.of(product(),product())); var r=service.search(); check(r,200);
        assertEquals(2,r.getBody().getProduct().getProducts().size());
        r.getBody().getProduct().getProducts().forEach(p->assertArrayEquals(picture,p.getPicture()));
    }
    @Test void emptyListReturns404() { when(products.findAll()).thenReturn(List.of()); check(service.search(),404); }
    @Test void listFailureReturns500() { when(products.findAll()).thenThrow(new IllegalStateException("DB")); check(service.search(),500); }
    @Test void filtersByNameAndDecompresses() {
        when(products.findByNameContainingIgnoreCase("lech")).thenReturn(List.of(product()));
        var r=service.searchByName("lech"); check(r,200); assertEquals("Leche",r.getBody().getProduct().getProducts().get(0).getName());
        assertArrayEquals(picture,r.getBody().getProduct().getProducts().get(0).getPicture()); verify(products).findByNameContainingIgnoreCase("lech");
    }
    @Test void noNameMatchesReturns404() { check(service.searchByName("missing"),404); }
    @Test void filterFailureReturns500() { when(products.findByNameContainingIgnoreCase("x")).thenThrow(new IllegalStateException("DB")); check(service.searchByName("x"),500); }
    @Test void deletesRequestedId() { check(service.deleteById(8L),200); verify(products).deleteById(8L); }
    @Test void deletionFailureReturns500() { doThrow(new IllegalStateException("DB")).when(products).deleteById(8L); check(service.deleteById(8L),500); }
    @Test void updatesEveryFieldAndKeepsId() {
        foundCategory(); Product old=product(), input=product(); input.setId(null); input.setName("Queso"); input.setPrice(25); input.setAccount(9); input.setPicture(new byte[]{8,9});
        when(products.findById(8L)).thenReturn(Optional.of(old)); when(products.save(old)).thenReturn(old);
        var r=service.update(input,4L,8L); check(r,200); assertSame(old,r.getBody().getProduct().getProducts().get(0));
        assertEquals(8L,old.getId()); assertEquals("Queso",old.getName()); assertEquals(25,old.getPrice()); assertEquals(9,old.getAccount());
        assertEquals(4L,old.getCategory().getId()); assertArrayEquals(input.getPicture(),old.getPicture()); verify(products).save(old);
    }
    @Test void updateMissingCategoryDoesNotTouchProducts() { check(service.update(product(),4L,8L),404); verifyNoInteractions(products); }
    @Test void updateMissingProductDoesNotSave() { foundCategory(); check(service.update(product(),4L,8L),404); verify(products,never()).save(any()); }
    @Test void updateNullSaveReturns400() { foundCategory(); when(products.findById(8L)).thenReturn(Optional.of(product())); check(service.update(product(),4L,8L),400); }
    @Test void updateCategoryFailureReturns500() { when(categories.findById(4L)).thenThrow(new IllegalStateException("DB")); check(service.update(product(),4L,8L),500); }
    @Test void updateSaveFailureReturns500() {
        foundCategory(); when(products.findById(8L)).thenReturn(Optional.of(product())); when(products.save(any())).thenThrow(new IllegalStateException("DB"));
        check(service.update(product(),4L,8L),500);
    }
}
