package com.eren.emlakcepteservice.service;

import com.eren.emlakcepteservice.converter.RealtyConverter;
import com.eren.emlakcepteservice.entity.Realty;
import com.eren.emlakcepteservice.entity.User;
import com.eren.emlakcepteservice.entity.enums.RealtyKind;
import com.eren.emlakcepteservice.entity.enums.RealtyStatus;
import com.eren.emlakcepteservice.entity.enums.RealtyType;
import com.eren.emlakcepteservice.repository.RealtyRepository;
import com.eren.emlakcepteservice.request.RealtyRequest;
import com.eren.emlakcepteservice.response.ProvinceResponse;
import com.eren.emlakcepteservice.response.RealtyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RealtyService {

    @Autowired
    private RealtyRepository realtyRepository;

    @Autowired
    private RealtyConverter realtyConverter;

    @Autowired
    private UserService userService;

    // Create Realty
    public RealtyResponse create(RealtyRequest realtyRequest) {
        User user = userService.getById(realtyRequest.getUserId());
        Realty newRealty = realtyConverter.convert(realtyRequest, user);
        realtyRepository.save(newRealty);
        return realtyConverter.convert(newRealty);
    }

    // Get All Realty
    public List<RealtyResponse> getAllRealtyResponse() {
        return realtyConverter.convert(realtyRepository.findAll());
    }

    // Get User's All Realty
    public List<RealtyResponse> getUserAll(Integer userId) {
        List<Realty> allRealty = userService.getAllRealty(userId);
        return realtyConverter.convert(allRealty);
    }

    // Get User's Active Realty
    public List<RealtyResponse> getUserActive(Integer userId) {
        User user = userService.getById(userId);
        List<Realty> activeRealty = realtyRepository.findRealtyByStatusAndUser(RealtyStatus.ACTIVE, user);
        return realtyConverter.convert(activeRealty);
    }

    // Get User's Passive Realty
    public List<RealtyResponse> getUserPassive(Integer userId) {
        User user = userService.getById(userId);
        List<Realty> passiveRealty = realtyRepository.findRealtyByStatusAndUser(RealtyStatus.PASSIVE, user);
        return realtyConverter.convert(passiveRealty);
    }

    // Get Realty By Province
    public List<Realty> getAllByProvince(String searchedProvince) {
        return realtyRepository.findAllByProvince(searchedProvince);
    }
    // Get Realty By District
    public List<Realty> getAllByDistrict(String searchedDistrict) {
        return realtyRepository.findAllByDistrict(searchedDistrict);
    }

    // Get Realty By Province And Realty Type ( SALE, RENT )
    public List<Realty> getProvinceRealtyCountByType(String province, RealtyType type) {
        return realtyRepository.findAllByProvinceAndRealtyType(province, type);
    }

    // Get Realty By Province And Realty Kind ( HOUSE, LAND )
    public List<Realty> getProvinceRealtyCountByKind(String province, RealtyKind kind) {
        return realtyRepository.findAllByProvinceAndRealtyKind(province, kind);
    }

    // Get Realty By Province, Realty Kind ( HOUSE, LAND ) And Realty Type ( SALE, RENT )
    public List<Realty> getProvinceRealtyCountByKindAndType(String province, RealtyKind kind, RealtyType type) {
        return realtyRepository.findAllByProvinceAndRealtyKindAndType(province, kind, type);
    }

    // Get Province Display ( 10 Realty )
    public List<RealtyResponse> getProvinceDisplay(String province) {
        List<Realty> display = realtyRepository.findAllByProvince(province).stream().limit(10).toList();
        return realtyConverter.convert(display);
    }

    // Get Realty Info of the Province ( Counts of the Realty Types )
    // Edit Here For Clean Code
    public ProvinceResponse getProvinceResponse(String province) {
        Integer realtyCount = getAllByProvince(province).size();
        Integer saleRealtyCount = getProvinceRealtyCountByType(province, RealtyType.SALE).size();
        Integer saleHouseCount = getProvinceRealtyCountByKindAndType(province, RealtyKind.HOUSE, RealtyType.SALE).size();
        Integer saleLandCount = getProvinceRealtyCountByKindAndType(province, RealtyKind.LAND, RealtyType.SALE).size();
        Integer rentRealtyCount = getProvinceRealtyCountByType(province, RealtyType.RENT).size();
        Integer rentHouseCount = getProvinceRealtyCountByKindAndType(province, RealtyKind.HOUSE, RealtyType.RENT).size();
        Integer rentLandCount = getProvinceRealtyCountByKindAndType(province, RealtyKind.LAND, RealtyType.RENT).size();

        ProvinceResponse provinceResponse = new ProvinceResponse();

        provinceResponse.setProvince(province);
        provinceResponse.setRealtyCount(realtyCount);
        provinceResponse.setSaleRealtyCount(saleRealtyCount);
        provinceResponse.setSaleHouseCount(saleHouseCount);
        provinceResponse.setSaleLandCount(saleLandCount);
        provinceResponse.setRentRealtyCount(rentRealtyCount);
        provinceResponse.setRentHouseCount(rentHouseCount);
        provinceResponse.setRentLandCount(rentLandCount);

        return provinceResponse;
    }
}
