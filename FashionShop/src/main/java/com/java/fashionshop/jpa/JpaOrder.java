package com.java.fashionshop.jpa;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.java.fashionshop.entity.UserEntity;
import org.hibernate.query.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.java.fashionshop.entity.OrderEntity;

public interface JpaOrder extends JpaRepository<OrderEntity, Integer> {
	List<OrderEntity> findByUser(UserEntity user);
	List<OrderEntity> findByUser_UserIdOrderByOrderDateDesc(Integer userId);
	
	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o " +
		       "WHERE o.status = 3 OR (LOWER(o.paymentMethod) = 'vnpay' AND o.status <> 3)")
		BigDecimal getTotalRevenueFromVNPayOrVNPayPending();
	
	@Query("SELECT NEW map(FUNCTION('MONTH', o.orderDate) AS month, SUM(o.totalAmount) AS revenue) " +
		       "FROM OrderEntity o " +
		       "WHERE o.status = 3 OR (LOWER(o.paymentMethod) = 'vnpay' AND o.status <> 3) " +
		       "GROUP BY FUNCTION('MONTH', o.orderDate)")
		List<Map<String, Object>> getMonthlyRevenue();

}
