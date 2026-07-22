package com.campus.api.controller.product;

import com.campus.api.auth.AuthSession; import com.campus.api.auth.Authenticated; import com.campus.api.auth.RoleAllowed;
import com.campus.application.product.ProductAppService; import com.campus.common.api.PageResult; import com.campus.common.api.Result;
import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.util.Map;

@RestController @RequestMapping("/api/product")
public class ProductController {
    private final ProductAppService app; private final AuthSession session;
    public ProductController(ProductAppService app,AuthSession session){this.app=app;this.session=session;}
    @GetMapping("/list") public Result<PageResult<Map<String,Object>>> list(@RequestParam(required=false) Integer categoryId,@RequestParam(required=false) String keyword,@RequestParam(required=false) BigDecimal minPrice,@RequestParam(required=false) BigDecimal maxPrice,@RequestParam(required=false) Integer status,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="10") int size){return Result.success(app.page(categoryId,keyword,minPrice,maxPrice,status,page,size));}
    @GetMapping("/detail/{id}") public Result<Map<String,Object>> detail(@PathVariable Long id){return Result.success(app.detail(id));}
    @Authenticated @RoleAllowed({1, 3}) @PostMapping public Result<Map<String,Object>> create(@RequestBody Map<String,Object> body){return Result.success(app.create(session.userId(),body));}
    @Authenticated @RoleAllowed({1, 3}) @PostMapping("/favorite/{id}") public Result<Void> favorite(@PathVariable Long id){app.favorite(session.userId(),id);return Result.success();}
    @Authenticated @RoleAllowed({1, 3}) @PostMapping("/order") public Result<Map<String,Object>> order(@RequestBody Map<String,Object> body){return Result.success(app.order(session.userId(),body));}
}
