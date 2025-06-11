package com.dita.service;

import com.dita.domain.Menu;
import com.dita.dto.MenuDTO;
import com.dita.persistence.MenuRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    public List<MenuDTO> getMenusByRestaurantId(int rstId) {
        List<Menu> menus = menuRepository.findByRestaurant_RstId(rstId);
        return menus.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private MenuDTO toDto(Menu menu) {
        MenuDTO dto = new MenuDTO();
        dto.setMenuId(menu.getMenuId());
        dto.setName(menu.getName());
        dto.setContent(menu.getContent());
        dto.setPrice(menu.getPrice());
        dto.setImage(menu.getImage());
        dto.setRestaurantId(menu.getRestaurant().getRstId());
        return dto;
    }
}
