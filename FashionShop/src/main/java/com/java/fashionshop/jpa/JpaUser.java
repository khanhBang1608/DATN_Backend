package com.java.fashionshop.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.fashionshop.entity.UserEntity;

public interface JpaUser extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);
    long countByRole(Integer role);

    @Query(value = """
        SELECT u FROM UserEntity u 
        WHERE u.role != 0
        AND (:name IS NULL OR u.fullName LIKE %:name%)
        AND (:email IS NULL OR u.email LIKE %:email%)
        AND (:fromDate IS NULL OR u.dateCreated >= :fromDate)
        AND (:toDate IS NULL OR u.dateCreated <= :toDate)
        AND (:status IS NULL OR u.status = :status)
    """)
    Page<UserEntity> findUsersPagedWithFilters(
        @Param("name") String name,
        @Param("email") String email,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("status") Boolean status,
        Pageable pageable
    );

    @Query(value = """
    	    SELECT 
    	        FORMAT(date_created, 'MM/yyyy') AS monthYear,
    	        COUNT(*) AS userCount
    	    FROM [User]
    	    GROUP BY FORMAT(date_created, 'MM/yyyy')
    	    ORDER BY MIN(date_created) ASC
    	    """, nativeQuery = true)
    	List<Map<String, Object>> countUsersByMonthYear();
    
    @Query("""
    	    SELECT COUNT(o)
    	    FROM OrderEntity o
    	    WHERE o.user.userId = :userId
    	""")
    	Integer countOrdersByUserId(@Param("userId") Integer userId);

}