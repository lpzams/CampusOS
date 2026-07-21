package com.campus.infrastructure.persistence.product;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.Data;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ===== POs =====

@Data @TableName("t_product_category")
class ProductCategoryPO {
    @TableId(type = IdType.AUTO) private Integer id;
    private String name;
    @TableField("create_time") private LocalDateTime createTime;
}

@Data @TableName("t_product")
class ProductPO {
    @TableId(type = IdType.AUTO) private Long id;
    private String title;
    private BigDecimal price;
    @TableField("original_price") private BigDecimal originalPrice;
    private String description;
    @TableField("cover_image") private String coverImage;
    @TableField("category_id") private Integer categoryId;
    private Integer status;
    @TableField("view_count") private Integer viewCount;
    @TableField("favorite_count") private Integer favoriteCount;
    @TableField("seller_id") private Long sellerId;
    @TableField("contact_phone") private String contactPhone;
    private String wechat;
    @TableField("audit_status") private String auditStatus;
    @TableField("expected_audit_time") private LocalDateTime expectedAuditTime;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
    @TableLogic private Integer deleted;
}

@Data @TableName("t_product_image")
class ProductImagePO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("product_id") private Long productId;
    @TableField("image_url") private String imageUrl;
    @TableField("sort_order") private Integer sortOrder;
    @TableField("create_time") private LocalDateTime createTime;
}

@Data @TableName("t_product_favorite")
class ProductFavoritePO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("user_id") private Long userId;
    @TableField("product_id") private Long productId;
    @TableField("create_time") private LocalDateTime createTime;
}

@Data @TableName("t_product_order")
class ProductOrderPO {
    @TableId(type = IdType.AUTO) private Long id;
    @TableField("order_id") private String orderId;
    @TableField("product_id") private Long productId;
    @TableField("buyer_id") private Long buyerId;
    @TableField("seller_id") private Long sellerId;
    private BigDecimal price;
    private String message;
    private String status;
    @TableField("create_time") private LocalDateTime createTime;
    @TableField("update_time") private LocalDateTime updateTime;
}

// ===== Mappers =====

@Mapper
interface ProductCategoryMapper extends BaseMapper<ProductCategoryPO> {
    @Select("SELECT * FROM t_product_category ORDER BY id ASC")
    List<ProductCategoryPO> selectAll();

    @Select("SELECT * FROM t_product_category WHERE id = #{id}")
    ProductCategoryPO selectById(Integer id);
}

@Mapper
interface ProductMapper extends BaseMapper<ProductPO> {
    @Select("<script>SELECT * FROM t_product WHERE deleted = 0 " +
            "<if test='categoryId != null'> AND category_id = #{categoryId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND title LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='minPrice != null'> AND price &gt;= #{minPrice} </if>" +
            "<if test='maxPrice != null'> AND price &lt;= #{maxPrice} </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}</script>")
    List<ProductPO> selectPage(@Param("categoryId") Integer categoryId,
                               @Param("keyword") String keyword,
                               @Param("minPrice") Double minPrice,
                               @Param("maxPrice") Double maxPrice,
                               @Param("status") Integer status,
                               @Param("offset") int offset,
                               @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_product WHERE deleted = 0 " +
            "<if test='categoryId != null'> AND category_id = #{categoryId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND title LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='minPrice != null'> AND price &gt;= #{minPrice} </if>" +
            "<if test='maxPrice != null'> AND price &lt;= #{maxPrice} </if>" +
            "<if test='status != null'> AND status = #{status} </if></script>")
    long count(@Param("categoryId") Integer categoryId,
               @Param("keyword") String keyword,
               @Param("minPrice") Double minPrice,
               @Param("maxPrice") Double maxPrice,
               @Param("status") Integer status);

    @Select("<script>SELECT * FROM t_product WHERE deleted = 0 AND seller_id = #{sellerId} " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}</script>")
    List<ProductPO> selectBySellerId(@Param("sellerId") Long sellerId,
                                     @Param("status") Integer status,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_product WHERE deleted = 0 AND seller_id = #{sellerId} " +
            "<if test='status != null'> AND status = #{status} </if></script>")
    long countBySellerId(@Param("sellerId") Long sellerId, @Param("status") Integer status);

    @Update("UPDATE t_product SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE t_product SET favorite_count = favorite_count + 1 WHERE id = #{id}")
    void incrementFavoriteCount(@Param("id") Long id);

    @Update("UPDATE t_product SET favorite_count = GREATEST(favorite_count - 1, 0) WHERE id = #{id}")
    void decrementFavoriteCount(@Param("id") Long id);
}

@Mapper
interface ProductImageMapper extends BaseMapper<ProductImagePO> {
    @Select("SELECT * FROM t_product_image WHERE product_id = #{productId} ORDER BY sort_order ASC")
    List<ProductImagePO> selectByProductId(@Param("productId") Long productId);
}

@Mapper
interface ProductFavoriteMapper extends BaseMapper<ProductFavoritePO> {
    @Select("SELECT * FROM t_product_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    ProductFavoritePO selectByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Delete("DELETE FROM t_product_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    void deleteByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT COUNT(*) FROM t_product_favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    int countByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}

@Mapper
interface ProductOrderMapper extends BaseMapper<ProductOrderPO> {
    @Select("SELECT * FROM t_product_order WHERE order_id = #{orderId}")
    ProductOrderPO selectByOrderId(@Param("orderId") String orderId);
}
