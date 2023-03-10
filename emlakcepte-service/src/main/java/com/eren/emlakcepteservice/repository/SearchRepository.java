package com.eren.emlakcepteservice.repository;

import com.eren.emlakcepteservice.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRepository extends JpaRepository<Search, Integer> {
}
