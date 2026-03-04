package com.example.shop.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.shop.dto.response.OrderItemResponseDto;
import com.example.shop.dto.response.OrderResponseDto;
import com.example.shop.dto.response.ProductResponseDto;
import com.example.shop.dto.response.ShopResponseDTO;

@Mapper
public interface ShopMapper {

        // [수정] product_name -> title, product_detail -> description 매핑 및 파라미터 타입(UUID
        // -> String) 수정
        @Select("SELECT product_id, title AS product_name, description AS product_detail FROM shop.products WHERE product_id = #{productId}::uuid")
        @Results(id = "ShopResultMap", value = {
                        @Result(property = "productId", column = "product_id"),
                        @Result(property = "productName", column = "product_name"),
                        @Result(property = "productDetail", column = "product_detail")
        })
        ShopResponseDTO findById(@Param("productId") String productId);
        // ========== 상품 ==========

        // [수정] base_price를 price로 매핑 (AS price)
        // [수정] DB에 없는 stock_quantity는 SQL 에러 방지를 위해 0으로 하드코딩 반환
        @Select("""
                        SELECT product_id, seller_id, seller_type, category, title,
                               description, base_price AS price, 0 AS stock_quantity, is_active, created_at
                        FROM shop.products
                        WHERE is_active = TRUE
                          AND (#{category}::text IS NULL OR category = #{category}::shop.product_category)
                          AND (#{sellerId}::bigint IS NULL OR seller_id = #{sellerId})
                        ORDER BY created_at DESC
                        LIMIT #{limit} OFFSET #{offset}
                        """)
        @Results(id = "ProductResultMap", value = {
                        @Result(property = "productId", column = "product_id"),
                        @Result(property = "sellerId", column = "seller_id"),
                        @Result(property = "sellerType", column = "seller_type"),
                        @Result(property = "category", column = "category"),
                        @Result(property = "title", column = "title"),
                        @Result(property = "description", column = "description"),
                        @Result(property = "price", column = "price"),
                        @Result(property = "stockQuantity", column = "stock_quantity"),
                        @Result(property = "isActive", column = "is_active"),
                        @Result(property = "createdAt", column = "created_at")
        })
        List<ProductResponseDto> findProducts(Map<String, Object> params);

        @Select("""
                        SELECT product_id, seller_id, seller_type, category, title,
                               description, base_price AS price, 0 AS stock_quantity, is_active, created_at
                        FROM shop.products
                        WHERE product_id = #{productId}::uuid
                        """)
        @ResultMap("ProductResultMap")
        ProductResponseDto findProductById(@Param("productId") String productId);

        // [수정] DB에 없는 stock_quantity 제거 및 base_price로 컬럼명 수정
        @Insert("""
                        INSERT INTO shop.products
                            (seller_id, seller_type, category, title, description, base_price)
                        VALUES
                            (#{sellerId}, #{sellerType}::shop.seller_type,
                             #{category}::shop.product_category,
                             #{title}, #{description}, #{price})
                        """)
        @Options(useGeneratedKeys = true, keyProperty = "productId", keyColumn = "product_id")
        void insertProduct(Map<String, Object> params);

        @Update("UPDATE shop.products SET is_active = FALSE WHERE product_id = #{productId}::uuid")
        void softDeleteProduct(@Param("productId") String productId);

        // [수정] DB에 stock_quantity 컬럼이 없으므로, 에러를 피하기 위해 더미 쿼리로 대체
        @Update("""
                        SELECT 1
                        /* DB에 재고 컬럼이 없어 실제 차감 동작은 비활성화되었습니다. */
                        """)
        void decreaseStock(@Param("productId") String productId, @Param("quantity") int quantity);

        // ========== 주문 ==========

        @Insert("""
                        INSERT INTO shop.orders (member_id, total_amount, shipping_address)
                        VALUES (#{memberId}, #{totalAmount}, #{shippingAddress})
                        """)
        @Options(useGeneratedKeys = true, keyProperty = "orderId", keyColumn = "order_id")
        void insertOrder(Map<String, Object> params);

        @Insert("""
                        INSERT INTO shop.order_items (order_id, product_id, quantity, unit_price)
                        VALUES (#{orderId}::uuid, #{productId}::uuid, #{quantity}, #{unitPrice})
                        """)
        void insertOrderItem(Map<String, Object> params);

        @Select("""
                        SELECT order_id, member_id, total_amount, status,
                               shipping_address, created_at, updated_at
                        FROM shop.orders
                        WHERE order_id = #{orderId}::uuid
                        """)
        @Results(id = "OrderResultMap", value = {
                        @Result(property = "orderId", column = "order_id"),
                        @Result(property = "memberId", column = "member_id"),
                        @Result(property = "totalAmount", column = "total_amount"),
                        @Result(property = "status", column = "status"),
                        @Result(property = "shippingAddress", column = "shipping_address"),
                        @Result(property = "createdAt", column = "created_at"),
                        @Result(property = "updatedAt", column = "updated_at"),
                        @Result(property = "items", column = "order_id", many = @Many(select = "findOrderItemsByOrderId"))
        })
        OrderResponseDto findOrderById(@Param("orderId") String orderId);

        @Select("""
                        SELECT oi.order_item_id, oi.product_id, p.title AS product_title,
                               oi.quantity, oi.unit_price
                        FROM shop.order_items oi
                        JOIN shop.products p ON oi.product_id = p.product_id
                        WHERE oi.order_id = #{orderId}::uuid
                        """)
        @Results({
                        @Result(property = "orderItemId", column = "order_item_id"),
                        @Result(property = "productId", column = "product_id"),
                        @Result(property = "productTitle", column = "product_title"),
                        @Result(property = "quantity", column = "quantity"),
                        @Result(property = "unitPrice", column = "unit_price")
        })
        List<OrderItemResponseDto> findOrderItemsByOrderId(@Param("orderId") String orderId);

        @Select("""
                        SELECT order_id, member_id, total_amount, status,
                               shipping_address, created_at, updated_at
                        FROM shop.orders
                        WHERE member_id = #{memberId}
                        ORDER BY created_at DESC
                        LIMIT #{limit} OFFSET #{offset}
                        """)
        @ResultMap("OrderResultMap")
        List<OrderResponseDto> findOrdersByMemberId(Map<String, Object> params);

        @Update("""
                        UPDATE shop.orders
                        SET status = #{status}::shop.order_status
                        WHERE order_id = #{orderId}::uuid
                        """)
        void updateOrderStatus(@Param("orderId") String orderId, @Param("status") String status);
}