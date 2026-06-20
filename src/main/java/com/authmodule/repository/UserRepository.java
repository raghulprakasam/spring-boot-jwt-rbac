package com.authmodule.repository;

import com.authmodule.entity.Role;
import com.authmodule.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);
	
    @Query("SELECT u FROM User u WHERE u.department = :department AND :role MEMBER OF u.roles")
    List<User> findByDepartmentAndRole(@Param("department") String department, @Param("role") Role role);
}