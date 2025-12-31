package com.example.shopbackend.demo.product;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ProductService productService;

    @Test
    void getById_existingId_shouldReturn200() throws Exception {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(productService.getById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_invalidTypeId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/products/b"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByName_withQuery_shouldReturn200() throws Exception {
        when(productService.getByName("Shirt")).thenReturn(List.of());

        mockMvc.perform(get("/products")
                .param("query", "Shirt"))
                .andExpect(status().isOk());
    }

    @Test
    void getInStock_withBoolean_shouldReturn200() throws Exception {
        when(productService.getInStock(true)).thenReturn(List.of());

        mockMvc.perform(get("/products")
                .param("inStock", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getPriceBetween_validToFrom_shouldReturn200() throws Exception {
        when(productService.getPriceBetween(1, 2)).thenReturn(List.of());

        mockMvc.perform(get("/products")
                .param("from", "1")
                .param("to", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void getPriceBetween_toMissing_shouldReturn400() throws Exception {
        mockMvc.perform(get("/products")
                .param("from", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(productService.getAll()).thenReturn(List.of());
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_existingProduct_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_existingProduct_shouldReturn200() throws Exception {
        Product product = mock(Product.class);
        UpdateProductRequest req = new UpdateProductRequest("name", 1, 1);
        when(product.getId()).thenReturn(1L);
        when(productService.update(product.getId(), req)).thenReturn(product);

        mockMvc.perform(put("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

}
