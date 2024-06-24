package com.mytool.pizza_store.services;

import com.mytool.pizza_store.model.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PizzasRepository extends JpaRepository<Pizza, Integer> {
}
