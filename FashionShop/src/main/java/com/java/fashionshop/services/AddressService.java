package com.java.fashionshop.services;

import java.util.List;
import java.util.Objects;
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

    public AddressDTO createAddress(AddressBean bean, UserEntity user) {
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

    public AddressDTO updateAddress(Integer addressId, AddressBean bean, UserEntity user) {
        AddressEntity entity = jpaAddress.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        // Đảm bảo địa chỉ này thuộc về user hiện tại
        if (!entity.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Không có quyền cập nhật địa chỉ này");
        }

        // So sánh dữ liệu cũ và mới
        boolean isChanged =
                !Objects.equals(entity.getCustomerName(), bean.getCustomerName()) ||
                !Objects.equals(entity.getPhone(), bean.getPhone()) ||
                !Objects.equals(entity.getAddress(), bean.getAddress()) ||
                !Objects.equals(entity.getProvinceId(), bean.getProvinceId()) ||
                !Objects.equals(entity.getProvinceName(), bean.getProvinceName()) ||
                !Objects.equals(entity.getDistrictId(), bean.getDistrictId()) ||
                !Objects.equals(entity.getDistrictName(), bean.getDistrictName()) ||
                !Objects.equals(entity.getWardId(), bean.getWardId()) ||
                !Objects.equals(entity.getWardName(), bean.getWardName());

        if (!isChanged) {
            throw new RuntimeException("NO_CHANGE"); // báo hiệu front-end biết
        }

        // Cập nhật nếu có thay đổi
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



    public void deleteAddress(Integer addressId, UserEntity user) {
        AddressEntity entity = jpaAddress.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ để xóa"));

        if (!entity.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Không có quyền xóa địa chỉ này");
        }

        jpaAddress.deleteById(addressId);
    }


    private AddressDTO convertToDTO(AddressEntity entity) {
        AddressDTO dto = new AddressDTO(
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
                entity.getWardName(),
                null // fullAddress sẽ set sau
        );

        String fullAddress = entity.getAddress() + ", " +
                             entity.getWardName() + ", " +
                             entity.getDistrictName() + ", " +
                             entity.getProvinceName();

        dto.setFullAddress(fullAddress);
        return dto;
    }

}
