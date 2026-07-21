package com.campus.application.product.service;

import com.campus.common.api.PageResult;
import com.campus.common.api.ResultCode;
import com.campus.common.exception.BusinessException;
import com.campus.domain.product.entity.*;
import com.campus.domain.product.repository.ProductRepository;
import com.campus.domain.user.entity.User;
import com.campus.domain.user.entity.UserProfile;
import com.campus.domain.user.repository.UserProfileRepository;
import com.campus.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAppService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 12.1 获取商品列表 ====================

    public PageResult<Map<String, Object>> getProductList(Integer categoryId, String keyword,
                                                          Double minPrice, Double maxPrice,
                                                          Integer status, int page, int size) {
        // 默认只展示在售商品
        if (status == null) status = Product.STATUS_ON_SALE;
        int offset = (page - 1) * size;

        List<Product> products = productRepository.findList(categoryId, keyword, minPrice, maxPrice, status, offset, size);
        long total = productRepository.countList(categoryId, keyword, minPrice, maxPrice, status);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Product p : products) {
            User seller = userRepository.findById(p.getSellerId());
            ProductCategory category = productRepository.findCategoryById(p.getCategoryId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("title", p.getTitle());
            item.put("price", p.getPrice());
            item.put("originalPrice", p.getOriginalPrice());
            item.put("description", p.getDescription());
            item.put("coverImage", p.getCoverImage());
            item.put("category", category != null ? category.getName() : null);
            item.put("categoryId", p.getCategoryId());
            item.put("status", p.getStatusDesc());
            item.put("statusCode", p.getStatus());
            item.put("viewCount", p.getViewCount());
            item.put("createTime", p.getCreateTime() != null ? p.getCreateTime().format(FORMATTER) : null);
            item.put("sellerId", p.getSellerId());
            item.put("sellerName", seller != null ? seller.getRealName() : null);
            item.put("sellerAvatar", seller != null ? seller.getAvatar() : null);
            list.add(item);
        }

        return PageResult.of(total, list, page, size);
    }

    // ==================== 12.2 获取商品详情 ====================

    public Map<String, Object> getProductDetail(Long id, Long currentUserId) {
        Product product = productRepository.findById(id);
        if (product == null) throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);

        // 增加浏览次数
        product.incrementViewCount();
        productRepository.update(product);

        // 详情页需要重新查询以获取最新 viewCount
        product = productRepository.findById(id);

        User seller = userRepository.findById(product.getSellerId());
        UserProfile profile = userProfileRepository.findByUserId(product.getSellerId());
        ProductCategory category = productRepository.findCategoryById(product.getCategoryId());
        List<ProductImage> images = productRepository.findImagesByProductId(id);

        boolean isFavorite = false;
        if (currentUserId != null) {
            isFavorite = productRepository.existsFavorite(currentUserId, id);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", product.getId());
        result.put("title", product.getTitle());
        result.put("price", product.getPrice());
        result.put("originalPrice", product.getOriginalPrice());
        result.put("description", product.getDescription());
        result.put("images", images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList()));
        result.put("category", category != null ? category.getName() : null);
        result.put("categoryId", product.getCategoryId());
        result.put("status", product.getStatusDesc());
        result.put("statusCode", product.getStatus());
        result.put("viewCount", product.getViewCount());
        result.put("favoriteCount", product.getFavoriteCount());
        result.put("isFavorite", isFavorite);
        result.put("createTime", product.getCreateTime() != null ? product.getCreateTime().format(FORMATTER) : null);
        result.put("updateTime", product.getUpdateTime() != null ? product.getUpdateTime().format(FORMATTER) : null);

        Map<String, Object> sellerInfo = new LinkedHashMap<>();
        sellerInfo.put("userId", seller != null ? seller.getId() : null);
        sellerInfo.put("realName", seller != null ? seller.getRealName() : null);
        sellerInfo.put("avatar", seller != null ? seller.getAvatar() : null);
        sellerInfo.put("phone", seller != null ? seller.getPhone() : null);
        sellerInfo.put("wechat", product.getWechat());
        sellerInfo.put("studentId", profile != null ? profile.getStudentId() : null);
        result.put("seller", sellerInfo);

        return result;
    }

    // ==================== 12.3 发布商品 ====================

    @Transactional
    public Map<String, Object> publishProduct(Long userId, Map<String, Object> command) {
        String title = (String) command.get("title");
        BigDecimal price = new BigDecimal(command.get("price").toString());
        String description = (String) command.get("description");
        Integer categoryId = ((Number) command.get("categoryId")).intValue();
        String contactPhone = (String) command.get("contactPhone");
        String wechat = (String) command.get("wechat");

        // 提取图片列表，取第一张作为封面图
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) command.get("images");
        String coverImage = (images != null && !images.isEmpty()) ? images.get(0) : null;

        // 创建商品
        Product product = Product.create(userId, title, price, description, categoryId, coverImage, contactPhone, wechat);
        productRepository.save(product);

        // 保存图片
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                ProductImage pi = ProductImage.create(product.getId(), images.get(i), i);
                productRepository.saveImage(pi);
            }
        }

        log.info("发布商品: productId={}, title={}, sellerId={}", product.getId(), title, userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", product.getId());
        result.put("title", product.getTitle());
        result.put("price", product.getPrice());
        result.put("status", product.getStatusDesc());
        result.put("createTime", product.getCreateTime() != null ? product.getCreateTime().format(FORMATTER) : null);
        result.put("auditStatus", product.getAuditStatusDesc());
        result.put("expectedAuditTime", product.getExpectedAuditTime() != null
                ? product.getExpectedAuditTime().format(FORMATTER) : null);
        return result;
    }

    // ==================== 12.4 收藏商品 ====================

    @Transactional
    public Map<String, Object> favoriteProduct(Long userId, Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);

        if (productRepository.existsFavorite(userId, productId)) {
            throw new BusinessException(ResultCode.PRODUCT_ALREADY_FAVORITED);
        }

        ProductFavorite favorite = ProductFavorite.create(userId, productId);
        productRepository.saveFavorite(favorite);

        log.info("收藏商品: userId={}, productId={}, favoriteId={}", userId, productId, favorite.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("favoriteId", favorite.getId());
        result.put("productId", productId);
        result.put("favoriteTime", favorite.getCreateTime() != null ? favorite.getCreateTime().format(FORMATTER) : null);
        return result;
    }

    // ==================== 12.5 创建订单 ====================

    @Transactional
    public Map<String, Object> createOrder(Long buyerId, Map<String, Object> command) {
        Long productId = ((Number) command.get("productId")).longValue();
        String message = (String) command.get("message");

        Product product = productRepository.findById(productId);
        if (product == null) throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        if (product.getStatus() == Product.STATUS_SOLD) throw new BusinessException(ResultCode.PRODUCT_ALREADY_SOLD);
        if (product.getStatus() == Product.STATUS_OFF_SHELF) throw new BusinessException(ResultCode.PRODUCT_OFFLINE);

        // 不能买自己的商品
        if (product.getSellerId().equals(buyerId)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID.getCode(), "不能购买自己的商品");
        }

        User seller = userRepository.findById(product.getSellerId());

        ProductOrder order = ProductOrder.create(productId, buyerId, product.getSellerId(), product.getPrice(), message);
        productRepository.saveOrder(order);

        log.info("创建订单: orderId={}, productId={}, buyerId={}, sellerId={}",
                order.getOrderId(), productId, buyerId, product.getSellerId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("productId", productId);
        result.put("productTitle", product.getTitle());
        result.put("price", order.getPrice());
        result.put("buyerId", buyerId);
        result.put("sellerId", product.getSellerId());
        result.put("sellerPhone", seller != null ? seller.getPhone() : null);
        result.put("status", order.getStatusDesc());
        result.put("statusCode", order.getStatus());
        result.put("createTime", order.getCreateTime() != null ? order.getCreateTime().format(FORMATTER) : null);
        result.put("message", message);
        return result;
    }

    // ==================== 12.6 下架商品 ====================

    @Transactional
    public Map<String, Object> offShelfProduct(Long userId, Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_OWNER.getCode(), "只能下架自己的商品");
        }

        product.offShelf();
        productRepository.update(product);

        log.info("下架商品: productId={}, sellerId={}", productId, userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("title", product.getTitle());
        result.put("status", product.getStatusDesc());
        result.put("statusCode", product.getStatus());
        result.put("updateTime", product.getUpdateTime() != null ? product.getUpdateTime().format(FORMATTER) : null);
        return result;
    }

    // ==================== 12.7 重新上架商品 ====================

    @Transactional
    public Map<String, Object> onShelfProduct(Long userId, Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_OWNER.getCode(), "只能上架自己的商品");
        }

        product.onShelf();
        productRepository.update(product);

        log.info("上架商品: productId={}, sellerId={}", productId, userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("title", product.getTitle());
        result.put("status", product.getStatusDesc());
        result.put("statusCode", product.getStatus());
        result.put("updateTime", product.getUpdateTime() != null ? product.getUpdateTime().format(FORMATTER) : null);
        return result;
    }

    // ==================== 12.8 标记已售 ====================

    @Transactional
    public Map<String, Object> markSoldProduct(Long userId, Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_OWNER.getCode(), "只能标记自己的商品");
        }

        product.markSold();
        productRepository.update(product);

        log.info("标记已售: productId={}, sellerId={}", productId, userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("title", product.getTitle());
        result.put("status", product.getStatusDesc());
        result.put("statusCode", product.getStatus());
        result.put("updateTime", product.getUpdateTime() != null ? product.getUpdateTime().format(FORMATTER) : null);
        return result;
    }

    // ==================== 12.9 删除商品（管理员） ====================

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        productRepository.deleteById(productId);
        log.info("管理员删除商品: productId={}", productId);
    }

    // ==================== 12.10 我的商品列表 ====================

    public PageResult<Map<String, Object>> getMyProducts(Long userId, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<Product> products = productRepository.findBySellerId(userId, status, offset, size);
        long total = productRepository.countBySellerId(userId, status);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("title", p.getTitle());
            item.put("price", p.getPrice());
            item.put("coverImage", p.getCoverImage());
            item.put("status", p.getStatusDesc());
            item.put("statusCode", p.getStatus());
            item.put("viewCount", p.getViewCount());
            item.put("favoriteCount", p.getFavoriteCount());
            item.put("createTime", p.getCreateTime() != null ? p.getCreateTime().format(FORMATTER) : null);
            list.add(item);
        }

        return PageResult.of(total, list, page, size);
    }
}
