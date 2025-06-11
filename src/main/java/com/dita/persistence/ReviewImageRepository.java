package com.dita.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dita.domain.ReviewImage;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Integer> {}
