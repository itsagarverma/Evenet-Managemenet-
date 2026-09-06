package com.sagar.eventmanagement.repository;

import com.sagar.eventmanagement.entity.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryRepository extends JpaRepository<Query, Long> {
}
