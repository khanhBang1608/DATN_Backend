package com.java.fashionshop.jpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.java.fashionshop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.java.fashionshop.entity.OrderEntity;
import org.springframework.data.repository.query.Param;

public interface JpaOrder extends JpaRepository<OrderEntity, Integer> {
	List<OrderEntity> findByUser(UserEntity user);
	List<OrderEntity> findByUser_UserIdOrderByOrderDateDesc(Integer userId);
	Page<OrderEntity> findByUser_UserId(Integer userId, Pageable pageable);
	
	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.paymentStatus = 1")
	BigDecimal getTotalRevenueWithPaymentStatus1();

	
	@Query(value = """
		    WITH Months AS (
		        SELECT 1 AS monthNum UNION ALL
		        SELECT 2 UNION ALL
		        SELECT 3 UNION ALL
		        SELECT 4 UNION ALL
		        SELECT 5 UNION ALL
		        SELECT 6 UNION ALL
		        SELECT 7 UNION ALL
		        SELECT 8 UNION ALL
		        SELECT 9 UNION ALL
		        SELECT 10 UNION ALL
		        SELECT 11 UNION ALL
		        SELECT 12
		    )
		    SELECT 
		        m.monthNum AS month,
		        COALESCE(SUM(o.total_amount), 0) AS revenue
		    FROM Months m
		    LEFT JOIN [Order] o 
		        ON MONTH(o.order_date) = m.monthNum
		        AND o.payment_status = 1 
		    GROUP BY m.monthNum
		    ORDER BY m.monthNum
		""", nativeQuery = true)
		List<Map<String, Object>> getMonthlyRevenueFullYear();

	
	@Query("""
		    SELECT p, SUM(od.quantity) as totalSold
		    FROM OrderEntity o
		    JOIN o.orderDetails od
		    JOIN od.productVariant pv
		    JOIN pv.product p
		    WHERE o.status = 3
		    GROUP BY p
		    ORDER BY totalSold DESC
		""")
		Page<Object[]> findBestSellingProducts(Pageable pageable);

	@Query("SELECT o FROM OrderEntity o WHERE o.user.email = :email AND o.paymentStatus = 1 AND o.orderDate >= :limitTime")
	Optional<OrderEntity> findRecentOrder(@Param("email") String email, @Param("limitTime") LocalDateTime limitTime);

	Optional<OrderEntity> findByTxnRef(String txnRef);


	OrderEntity findByIdempotencyKey(String idempotencyKey);
}
