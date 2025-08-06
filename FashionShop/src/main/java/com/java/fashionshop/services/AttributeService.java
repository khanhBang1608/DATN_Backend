package com.java.fashionshop.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.java.fashionshop.entity.ColorsEntity;
import com.java.fashionshop.entity.SizesEntity;
import com.java.fashionshop.jpa.JpaColors;
import com.java.fashionshop.jpa.JpaSizes;

@Service
public class AttributeService {

    @Autowired
    private JpaColors jpaColors;

    @Autowired
    private JpaSizes jpaSizes;

    // Color Methods
    public Page<ColorsEntity> getAllColors(Pageable pageable) {
        return jpaColors.findAll(pageable);
    }

    public Optional<ColorsEntity> getColorById(Integer id) {
        return jpaColors.findById(id);
    }

    public ColorsEntity addColor(ColorsEntity color) {
        return jpaColors.save(color);
    }

    public ColorsEntity updateColor(Integer id, ColorsEntity updatedColor) {
        updatedColor.setColorId(id);
        return jpaColors.save(updatedColor);
    }
    
    public void deleteColor(ColorsEntity color) {
        jpaColors.delete(color);
    }


    // Size Methods
    public Page<SizesEntity> getAllSizes(Pageable pageable) {
        return jpaSizes.findAll(pageable);
    }

    public Optional<SizesEntity> getSizeById(Integer id) {
        return jpaSizes.findById(id);
    }

    public SizesEntity addSize(SizesEntity size) {
        return jpaSizes.save(size);
    }

    public SizesEntity updateSize(Integer id, SizesEntity updatedSize) {
        updatedSize.setSizeId(id);
        return jpaSizes.save(updatedSize);
    }
    
    public void deleteSize(SizesEntity size) {
        jpaSizes.delete(size);
    }

 // AttributeService.java

    public boolean existsColorName(String colorName) {
        return jpaColors.existsByColorNameIgnoreCase(colorName.trim());
    }

    public boolean existsSizeName(String sizeName) {
        return jpaSizes.existsBySizeNameIgnoreCase(sizeName.trim());
    }

}
