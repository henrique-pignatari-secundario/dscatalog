package com.henrique.dscatalog.services;

import com.henrique.dscatalog.dto.ProductDTO;
import com.henrique.dscatalog.entities.Product;
import com.henrique.dscatalog.repositories.CategoryRepository;
import com.henrique.dscatalog.repositories.ProductRepository;
import com.henrique.dscatalog.services.exceptions.DatabaseException;
import com.henrique.dscatalog.services.exceptions.ResourceNotFoundException;
import com.henrique.dscatalog.tests.Factory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private PageImpl<Product> page;
    private Product product;

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1;
        nonExistingId = 1000L;
        dependentId = 3L;
        product = Factory.createProduct();
        page = new PageImpl<>(List.of(product));

        Mockito.when(productRepository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        Mockito.when(productRepository.getReferenceById(existingId)).thenReturn(product);
        Mockito.when(productRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);

        Mockito.when(productRepository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Mockito.when(productRepository.existsById(existingId)).thenReturn(true);
        Mockito.when(productRepository.existsById(nonExistingId)).thenReturn(false);
        Mockito.when(productRepository.existsById(dependentId)).thenReturn(true);

        Mockito.doNothing().when(productRepository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(productRepository).deleteById(dependentId);

        Mockito.when(categoryRepository.getReferenceById(ArgumentMatchers.any())).thenReturn(Factory.createCategory());
    }

    @Test
    public void findAllPagedShouldReturnPage(){
        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductDTO> result = productService.findAllPaged(pageable);

        Assertions.assertNotNull(result);
        Mockito.verify(productRepository).findAll(pageable);
    }

    @Test
    public void findByIdShouldReturnProductDtoWhenExistingId(){
        ProductDTO productDTO = productService.findById(existingId);

        Assertions.assertNotNull(productDTO);
        Assertions.assertEquals(productDTO.getName(), product.getName());
        Assertions.assertEquals(productDTO.getDate(), product. getDate());
        Assertions.assertEquals(productDTO.getImgUrl(), product.getImgUrl());
        Assertions.assertEquals(productDTO.getPrice(), product.getPrice());
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenNonExistingId(){
        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> productService.findById(nonExistingId)
        );
    }

    @Test
    public void updateShouldReturnProductDtoWhenExistingId(){
        ProductDTO productDTO = productService.update(existingId, Factory.createProductDTO());

        Assertions.assertNotNull(productDTO);
        Mockito.verify(productRepository).save(ArgumentMatchers.any());
        Mockito.verify(categoryRepository).getReferenceById(Factory.createCategory().getId());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenNonExistingId(){
        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> productService.update(nonExistingId, Factory.createProductDTO())
        );
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenNonExistingId(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.deleteById(nonExistingId));

        Mockito.verify(productRepository, Mockito.times(1)).existsById(nonExistingId);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId(){
        Assertions.assertThrows(DatabaseException.class, () -> productService.deleteById(dependentId));

        Mockito.verify(productRepository, Mockito.times(1)).existsById(dependentId);
        Mockito.verify(productRepository, Mockito.times(1)).deleteById(dependentId);
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists(){
        Assertions.assertDoesNotThrow(() -> productService.deleteById(existingId));

        Mockito.verify(productRepository, Mockito.times(1)).existsById(existingId);
        Mockito.verify(productRepository, Mockito.times(1)).deleteById(existingId);
    }
}
