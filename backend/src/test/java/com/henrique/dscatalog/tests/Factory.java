package com.henrique.dscatalog.tests;

import com.henrique.dscatalog.dto.CategoryDTO;
import com.henrique.dscatalog.dto.ProductDTO;
import com.henrique.dscatalog.entities.Category;
import com.henrique.dscatalog.entities.Product;

import java.time.Instant;

public class Factory {
    public static Product createProduct(){
        Product product = new Product(
                1L,
                "Phone",
                "Good Phone",
                800.0,
                "https://img.com/img.png",
                Instant.parse("2020-10-20T03:00:00Z")
        );
        product.getCategories().add(createCategory());
        return product;
    }

    public static ProductDTO createProductDTO(){
        Product product = createProduct();
        return new ProductDTO(product, product.getCategories());
    }

    public static Category createCategory(){
        return new Category(2L, "Electronics");
    }

    public static CategoryDTO createCategoryDTO(){
        Category category = createCategory();
        return new CategoryDTO(category);
    }
}
