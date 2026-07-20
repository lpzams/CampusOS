package com.campus.infrastructure.persistence.product;

import com.campus.domain.product.entity.*;
import com.campus.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductFavoriteMapper productFavoriteMapper;
    private final ProductOrderMapper productOrderMapper;
    private final ProductCategoryMapper productCategoryMapper;

    // ===== 转换方法 =====
    private Product toProduct(ProductPO po) { Product p = new Product(); BeanUtils.copyProperties(po, p); return p; }
    private ProductPO toProductPO(Product p) { ProductPO po = new ProductPO(); BeanUtils.copyProperties(p, po); return po; }
    private ProductImage toImage(ProductImagePO po) { ProductImage i = new ProductImage(); BeanUtils.copyProperties(po, i); return i; }
    private ProductImagePO toImagePO(ProductImage i) { ProductImagePO po = new ProductImagePO(); BeanUtils.copyProperties(i, po); return po; }
    private ProductFavorite toFavorite(ProductFavoritePO po) { ProductFavorite f = new ProductFavorite(); BeanUtils.copyProperties(po, f); return f; }
    private ProductFavoritePO toFavoritePO(ProductFavorite f) { ProductFavoritePO po = new ProductFavoritePO(); BeanUtils.copyProperties(f, po); return po; }
    private ProductOrder toOrder(ProductOrderPO po) { ProductOrder o = new ProductOrder(); BeanUtils.copyProperties(po, o); return o; }
    private ProductOrderPO toOrderPO(ProductOrder o) { ProductOrderPO po = new ProductOrderPO(); BeanUtils.copyProperties(o, po); return po; }
    private ProductCategory toCategory(ProductCategoryPO po) { ProductCategory c = new ProductCategory(); BeanUtils.copyProperties(po, c); return c; }

    // ===== Product CRUD =====

    @Override public void save(Product product) { ProductPO po = toProductPO(product); productMapper.insert(po); product.setId(po.getId()); }
    @Override public void update(Product product) { productMapper.updateById(toProductPO(product)); }
    @Override public Product findById(Long id) { return toProduct(productMapper.selectById(id)); }
    @Override public void deleteById(Long id) { productMapper.deleteById(id); }

    @Override
    public List<Product> findList(Integer categoryId, String keyword, Double minPrice, Double maxPrice,
                                  Integer status, int offset, int size) {
        return productMapper.selectPage(categoryId, keyword, minPrice, maxPrice, status, offset, size)
                .stream().map(this::toProduct).collect(Collectors.toList());
    }

    @Override
    public long countList(Integer categoryId, String keyword, Double minPrice, Double maxPrice, Integer status) {
        return productMapper.count(categoryId, keyword, minPrice, maxPrice, status);
    }

    @Override
    public List<Product> findBySellerId(Long sellerId, Integer status, int offset, int size) {
        return productMapper.selectBySellerId(sellerId, status, offset, size)
                .stream().map(this::toProduct).collect(Collectors.toList());
    }

    @Override
    public long countBySellerId(Long sellerId, Integer status) {
        return productMapper.countBySellerId(sellerId, status);
    }

    // ===== ProductImage =====

    @Override public void saveImage(ProductImage image) { productImageMapper.insert(toImagePO(image)); }

    @Override
    public List<ProductImage> findImagesByProductId(Long productId) {
        return productImageMapper.selectByProductId(productId)
                .stream().map(this::toImage).collect(Collectors.toList());
    }

    // ===== ProductCategory =====

    @Override
    public List<ProductCategory> findAllCategories() {
        return productCategoryMapper.selectAll().stream().map(this::toCategory).collect(Collectors.toList());
    }

    @Override
    public ProductCategory findCategoryById(Integer id) {
        return toCategory(productCategoryMapper.selectById(id));
    }

    // ===== ProductFavorite =====

    @Override public void saveFavorite(ProductFavorite favorite) { productFavoriteMapper.insert(toFavoritePO(favorite)); }

    @Override
    public void deleteFavorite(Long userId, Long productId) {
        productFavoriteMapper.deleteByUserAndProduct(userId, productId);
    }

    @Override
    public ProductFavorite findFavorite(Long userId, Long productId) {
        return toFavorite(productFavoriteMapper.selectByUserAndProduct(userId, productId));
    }

    @Override
    public boolean existsFavorite(Long userId, Long productId) {
        return productFavoriteMapper.countByUserAndProduct(userId, productId) > 0;
    }

    // ===== ProductOrder =====

    @Override public void saveOrder(ProductOrder order) { ProductOrderPO po = toOrderPO(order); productOrderMapper.insert(po); order.setId(po.getId()); }

    @Override public ProductOrder findOrderById(Long id) { return toOrder(productOrderMapper.selectById(id)); }

    @Override public ProductOrder findOrderByOrderId(String orderId) { return toOrder(productOrderMapper.selectByOrderId(orderId)); }
}
