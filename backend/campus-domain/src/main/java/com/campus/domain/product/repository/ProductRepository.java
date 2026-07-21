package com.campus.domain.product.repository;

import com.campus.domain.product.entity.*;

import java.util.List;

public interface ProductRepository {

    // ===== Product CRUD =====
    void save(Product product);
    void update(Product product);
    Product findById(Long id);
    void deleteById(Long id);

    // 商品列表（游客/用户通用）
    List<Product> findList(Integer categoryId, String keyword, Double minPrice, Double maxPrice,
                           Integer status, int offset, int size);
    long countList(Integer categoryId, String keyword, Double minPrice, Double maxPrice, Integer status);

    // 我的商品列表
    List<Product> findBySellerId(Long sellerId, Integer status, int offset, int size);
    long countBySellerId(Long sellerId, Integer status);

    // ===== ProductImage =====
    void saveImage(ProductImage image);
    List<ProductImage> findImagesByProductId(Long productId);

    // ===== ProductCategory =====
    List<ProductCategory> findAllCategories();
    ProductCategory findCategoryById(Integer id);

    // ===== ProductFavorite =====
    void saveFavorite(ProductFavorite favorite);
    void deleteFavorite(Long userId, Long productId);
    ProductFavorite findFavorite(Long userId, Long productId);
    boolean existsFavorite(Long userId, Long productId);

    // ===== ProductOrder =====
    void saveOrder(ProductOrder order);
    ProductOrder findOrderById(Long id);
    ProductOrder findOrderByOrderId(String orderId);
}
