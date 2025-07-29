package com.java.fashionshop.controller;

import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.services.ProductViewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/product-views")
public class PublicProductViewController {

    @Autowired
    private ProductViewsService productViewsService;

   
}
