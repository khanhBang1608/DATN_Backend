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
	
	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.paymentStatus = 1")
	BigDecimal getTotalRevenueWithPaymentStatus1();

	
	@Query("SELECT NEW map(FUNCTION('MONTH', o.orderDate) AS month, SUM(o.totalAmount) AS revenue) " +
		       "FROM OrderEntity o " +
		       "WHERE o.status = 3 OR (LOWER(o.paymentMethod) = 'vnpay' AND o.status <> 3) " +
		       "GROUP BY FUNCTION('MONTH', o.orderDate)")
		List<Map<String, Object>> getMonthlyRevenue();
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

}
