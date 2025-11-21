package es.daw.foodexpressapi.service;

import es.daw.foodexpressapi.dto.RestaurantDTO;
import es.daw.foodexpressapi.entity.Restaurant;
import es.daw.foodexpressapi.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<RestaurantDTO> getAllRestaurants(){
        return restaurantRepository.findAll().stream()
                .map(this::toDTO)
                .toList();

    }

    public Optional<RestaurantDTO> create(RestaurantDTO restaurantDTO){
        Restaurant restaurant = toEntity(restaurantDTO);
        Restaurant saved = restaurantRepository.save(restaurant);
        return Optional.of(this.toDTO(saved));
    }


    // ---------------- UTILIDADES TIPO MAPSTRUCT ------------------
    public RestaurantDTO toDTO(Restaurant restaurant){
//        RestaurantDTO restaurantDTO = new RestaurantDTO();
//        restaurantDTO.setName(restaurant.getName());
//        restaurantDTO.setAddress(restaurant.getAddress());
//        restaurantDTO.setPhone(restaurant.getPhone());
//        return restaurantDTO;
        return RestaurantDTO.builder()
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .build();
    }

    public Restaurant toEntity(RestaurantDTO dto){
        Restaurant restaurant = new Restaurant();
        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setPhone(dto.getPhone());
        return restaurant;
    }


}
