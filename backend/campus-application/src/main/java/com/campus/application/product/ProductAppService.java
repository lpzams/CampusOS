package com.campus.application.product;

import com.campus.application.shared.CampusAppService;
import com.campus.application.shared.Values;
import com.campus.common.api.PageResult;
import com.campus.domain.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductAppService {
    private final CampusAppService records;
    public ProductAppService(CampusAppService records) { this.records = records; }

    public PageResult<Map<String, Object>> page(Integer categoryId, String keyword, BigDecimal minPrice,
                                                 BigDecimal maxPrice, Integer status, int page, int size) {
        List<Map<String, Object>> rows = records.list("product").stream()
                .filter(p -> categoryId == null || categoryId.toString().equals(String.valueOf(p.get("categoryId"))))
                .filter(p -> keyword == null || keyword.isBlank() || String.valueOf(p.get("title")).toLowerCase().contains(keyword.toLowerCase()))
                .filter(p -> minPrice == null || Values.decimal(p.get("price")).compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || Values.decimal(p.get("price")).compareTo(maxPrice) <= 0)
                .filter(p -> status == null || (status == 0 ? "在售" : "已售").equals(p.get("status"))).toList();
        int safePage = Math.max(1, page), safeSize = Math.min(100, Math.max(1, size));
        int from = Math.min((safePage - 1) * safeSize, rows.size());
        return new PageResult<>(safePage, safeSize, rows.size(), rows.subList(from, Math.min(from + safeSize, rows.size())));
    }

    public Map<String, Object> detail(Long id) { return records.get("product", id); }

    @Transactional
    public Map<String, Object> create(Long userId, Map<String, Object> command) {
        for (String key : List.of("title", "price", "description", "categoryId", "contactPhone")) Values.required(command, key);
        Product product;
        try { product = new Product(userId, Values.decimal(command.get("price")), Product.Status.ON_SALE); }
        catch (RuntimeException e) { throw Values.invalid(e); }
        Map<String, Object> data = new LinkedHashMap<>(command);
        data.put("sellerId", product.sellerId()); data.put("status", "在售"); data.put("viewCount", 0);
        return records.create("product", data);
    }

    @Transactional
    public void favorite(Long userId, Long productId) {
        records.get("product", productId);
        boolean exists = records.listExact("productFavorite", "userId", userId).stream()
                .anyMatch(row -> productId.equals(Values.longValue(row.get("productId"))));
        if (!exists) records.create("productFavorite", Values.owned(userId, Map.of("productId", productId)));
    }

    @Transactional
    public Map<String, Object> order(Long userId, Map<String, Object> command) {
        Long id = Values.longValue(command.get("productId"));
        Map<String, Object> row = records.get("product", id);
        Product product;
        try {
            product = new Product(Values.longValue(row.get("sellerId")), Values.decimal(row.get("price")),
                    "已售".equals(row.get("status")) ? Product.Status.SOLD : Product.Status.ON_SALE);
            product.orderBy(userId);
        } catch (RuntimeException e) { throw Values.invalid(e); }
        Map<String, Object> data = new LinkedHashMap<>(command);
        data.put("status", "待确认");
        return records.create("productOrder", Values.owned(userId, data));
    }
}
