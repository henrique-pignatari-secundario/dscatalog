package com.henrique.dscatalog.services;

import com.henrique.dscatalog.dto.ProductDTO;
import com.henrique.dscatalog.repositories.ProductRepository;
import com.henrique.dscatalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ProductServiceIT {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;

    private Long existingId;
    private Long nonExistingId;
    private Long countTotalProducts;

    @BeforeEach
    void setUp() throws Exception{
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }

    @Test
    public void deleteShouldDeleteResourceWhenExistingId(){
        productService.deleteById(existingId);

        Assertions.assertEquals(countTotalProducts - 1, productRepository.count());
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenNonExistingId(){
        Assertions.assertThrows(
            ResourceNotFoundException.class,
            () -> productService.deleteById(nonExistingId)
        );
    }

    @Test
    public void findAllPagedShouldReturnPageWhenPage0Size10(){
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ProductDTO> result = productService.findAllPaged(pageRequest);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals(10, result.getSize());
        Assertions.assertEquals(countTotalProducts, result.getTotalElements());
    }

    @Test
    public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExists(){
        PageRequest pageRequest = PageRequest.of(50, 10);

        Page<ProductDTO> result = productService.findAllPaged(pageRequest);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void findAllPagedShouldReturnSortedPageWhenSortByName(){
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name"));

        Page<ProductDTO> result = productService.findAllPaged(pageRequest);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("Macbook Pro", result.getContent().get(0).getName());
        Assertions.assertEquals("PC Gamer", result.getContent().get(1).getName());
        Assertions.assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());
    }
}
