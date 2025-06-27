package com.java.fashionshop.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.fashionshop.bean.AddressBean;
import com.java.fashionshop.dto.AddressDTO;
import com.java.fashionshop.entity.AddressEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaAddress;
import com.java.fashionshop.jpa.JpaUser;

@Service
public class AddressService {

    @Autowired
    private JpaAddress jpaAddress;

    @Autowired
    private JpaUser jpaUser;

    public AddressDTO createAddress(AddressBean bean) {
        UserEntity user = jpaUser.findById(bean.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        AddressEntity entity = new AddressEntity();
        entity.setUser(user);
        entity.setCustomerName(bean.getCustomerName());
        entity.setPhone(bean.getPhone());
        entity.setAddress(bean.getAddress());
        entity.setProvinceId(bean.getProvinceId());
        entity.setProvinceName(bean.getProvinceName());
        entity.setDistrictId(bean.getDistrictId());
        entity.setDistrictName(bean.getDistrictName());
        entity.setWardId(bean.getWardId());
        entity.setWardName(bean.getWardName());

        AddressEntity saved = jpaAddress.save(entity);

        return convertToDTO(saved);
    }

    public List<AddressDTO> getAddressesByUserId(Integer userId) {
        return jpaAddress.findByUserUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AddressDTO updateAddress(Integer addressId, AddressBean bean) {
        AddressEntity entity = jpaAddress.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        // Cập nhật thông tin
        entity.setCustomerName(bean.getCustomerName());
        entity.setPhone(bean.getPhone());
        entity.setAddress(bean.getAddress());
        entity.setProvinceId(bean.getProvinceId());
        entity.setProvinceName(bean.getProvinceName());
        entity.setDistrictId(bean.getDistrictId());
        entity.setDistrictName(bean.getDistrictName());
        entity.setWardId(bean.getWardId());
        entity.setWardName(bean.getWardName());

        AddressEntity saved = jpaAddress.save(entity);
        return convertToDTO(saved);
    }

    public void deleteAddress(Integer addressId) {
        if (!jpaAddress.existsById(addressId)) {
            throw new RuntimeException("Không tìm thấy địa chỉ để xóa");
        }
        jpaAddress.deleteById(addressId);
    }

    private AddressDTO convertToDTO(AddressEntity entity) {
        return new AddressDTO(
                entity.getAddressId(),
                entity.getUser().getUserId(),
                entity.getCustomerName(),
                entity.getPhone(),
                entity.getAddress(),
                entity.getProvinceId(),
                entity.getProvinceName(),
                entity.getDistrictId(),
                entity.getDistrictName(),
                entity.getWardId(),
                entity.getWardName()
        );
    }
}
