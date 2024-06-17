package com.henrique.dscatalog.services;

import com.henrique.dscatalog.dto.CategoryDTO;
import com.henrique.dscatalog.entities.Category;
import com.henrique.dscatalog.repositories.CategoryRepository;
import com.henrique.dscatalog.services.exceptions.DatabaseException;
import com.henrique.dscatalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll(){
        List<Category> result = categoryRepository.findAll();
        return result.stream().map(CategoryDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id){
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new CategoryDTO(entity);
    }

    @Transactional
    public CategoryDTO insert(CategoryDTO dto) {
        Category entity = new Category();
        entity.setName(dto.getName());

        entity = categoryRepository.save(entity);

        return  new CategoryDTO(entity);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        try{
            Category entity = categoryRepository.getReferenceById(id);
            entity.setName(dto.getName());

            entity = categoryRepository.save(entity);

            return  new CategoryDTO(entity);
        }catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found");
        }
        try {
            categoryRepository.deleteById(id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Data integrity violation");
        }
    }

}
