package com.moonlight.project.airBnbApp.repository;

import com.moonlight.project.airBnbApp.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCodeAndActiveTrue(String code);

    List<PromoCode> findByActiveTrue();

    /** Case-insensitive lookup - matches regardless of letter casing */
    @Query("SELECT p FROM PromoCode p WHERE UPPER(TRIM(p.code)) = UPPER(TRIM(:code)) AND p.active = true")
    Optional<PromoCode> findByCodeIgnoreCaseAndActiveTrue(@Param("code") String code);
}