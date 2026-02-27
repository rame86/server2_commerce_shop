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

@Mapper
public interface ShopMapper {

    // ========== 상품 ==========

    @Select("""
            SELECT product_id, seller_id, seller_type, category, title,
                   description, price, stock_quantity, is_active, created_at
            FROM shop.products
            WHERE is_active = TRUE
              AND (#{category}::text IS NULL OR category = #{category}::shop.product_category)
              AND (#{sellerId}::bigint IS NULL OR seller_id = #{sellerId})
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    @Results(id = "ProductResultMap", value = {
            @Result(property = "productId",      column = "product_id"),
            @Result(property = "sellerId",       column = "seller_id"),
            @Result(property = "sellerType",     column = "seller_type"),
            @Result(property = "category",       column = "category"),
            @Result(property = "title",          column = "title"),
            @Result(property = "description",    column = "description"),
            @Result(property = "price",          column = "price"),
            @Result(property = "stockQuantity",  column = "stock_quantity"),
            @Result(property = "isActive",       column = "is_active"),
            @Result(property = "createdAt",      column = "created_at")
    })
    List<ProductResponseDto> findProducts(Map<String, Object> params);

    @Select("""
            SELECT product_id, seller_id, seller_type, category, title,
                   description, price, stock_quantity, is_active, created_at
            FROM shop.products
            WHERE product_id = #{productId}::uuid
            """)
    @ResultMap("ProductResultMap")
    ProductResponseDto findProductById(@Param("productId") String productId);

    @Insert("""
            INSERT INTO shop.products
                (seller_id, seller_type, category, title, description, price, stock_quantity)
            VALUES
                (#{sellerId}, #{sellerType}::shop.seller_type,
                 #{category}::shop.product_category,
                 #{title}, #{description}, #{price}, #{stockQuantity})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "productId", keyColumn = "product_id")
    void insertProduct(Map<String, Object> params);

    @Update("UPDATE shop.products SET is_active = FALSE WHERE product_id = #{productId}::uuid")
    void softDeleteProduct(@Param("productId") String productId);

    @Update("""
            UPDATE shop.products
            SET stock_quantity = stock_quantity - #{quantity}
            WHERE product_id = #{productId}::uuid
              AND stock_quantity >= #{quantity}
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

    // 주문 기본 정보 조회
    @Select("""
            SELECT order_id, member_id, total_amount, status,
                   shipping_address, created_at, updated_at
            FROM shop.orders
            WHERE order_id = #{orderId}::uuid
            """)
    @Results(id = "OrderResultMap", value = {
            @Result(property = "orderId",          column = "order_id"),
            @Result(property = "memberId",         column = "member_id"),
            @Result(property = "totalAmount",      column = "total_amount"),
            @Result(property = "status",           column = "status"),
            @Result(property = "shippingAddress",  column = "shipping_address"),
            @Result(property = "createdAt",        column = "created_at"),
            @Result(property = "updatedAt",        column = "updated_at"),
            @Result(property = "items",            column = "order_id",
                    many = @Many(select = "findOrderItemsByOrderId"))
    })
    OrderResponseDto findOrderById(@Param("orderId") String orderId);

    // 주문 상세 아이템 조회 (findOrderById 내부에서 자동 호출)
    @Select("""
            SELECT oi.order_item_id, oi.product_id, p.title AS product_title,
                   oi.quantity, oi.unit_price
            FROM shop.order_items oi
            JOIN shop.products p ON oi.product_id = p.product_id
            WHERE oi.order_id = #{orderId}::uuid
            """)
    @Results({
            @Result(property = "orderItemId",   column = "order_item_id"),
            @Result(property = "productId",     column = "product_id"),
            @Result(property = "productTitle",  column = "product_title"),
            @Result(property = "quantity",      column = "quantity"),
            @Result(property = "unitPrice",     column = "unit_price")
    })
    List<OrderItemResponseDto> findOrderItemsByOrderId(@Param("orderId") String orderId);

    // 내 주문 목록
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