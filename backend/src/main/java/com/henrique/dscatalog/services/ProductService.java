package com.henrique.dscatalog.services;

import com.henrique.dscatalog.dto.CategoryDTO;
import com.henrique.dscatalog.dto.ProductDTO;
import com.henrique.dscatalog.entities.Category;
import com.henrique.dscatalog.entities.Product;
import com.henrique.dscatalog.projections.ProductProjection;
import com.henrique.dscatalog.repositories.CategoryRepository;
import com.henrique.dscatalog.repositories.ProductRepository;
import com.henrique.dscatalog.services.exceptions.DatabaseException;
import com.henrique.dscatalog.services.exceptions.ResourceNotFoundException;
import com.henrique.dscatalog.util.Utils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(String name, String categoryId, Pageable pageable){
        List<Long> categoryIds = List.of();
        if(!"0".equals(categoryId)){
            categoryIds = Arrays.stream(categoryId.split(",")).map(Long::parseLong).toList();
        }

        Page<ProductProjection> page = productRepository.searchProducts(categoryIds, name, pageable);
        List<Long> productIds = page.map(ProductProjection::getId).toList();

        List<Product> entities = productRepository.searchProductsWithCategories(productIds);
        entities = (List<Product>) Utils.replace(page.getContent(), entities);

        List<ProductDTO> dtos = entities.stream().map(p -> new ProductDTO(p, p.getCategories())).toList();

        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id){
        Product entity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();
        copyDtoToEntity(dto, entity);
        entity = productRepository.save(entity);

        return  new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try{
            Product entity = productRepository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = productRepository.save(entity);

            return  new ProductDTO(entity);
        }catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found");
        }
        try {
            productRepository.deleteById(id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Data integrity violation");
        }
    }

    private void copyDtoToEntity(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setImgUrl(dto.getImgUrl());
        entity.setDate(dto.getDate());

        entity.getCategories().clear();
        for(CategoryDTO catDto : dto.getCategories()){
            Category category = categoryRepository.getReferenceById(catDto.getId());
            entity.getCategories().add(category);
        }
    }
}
