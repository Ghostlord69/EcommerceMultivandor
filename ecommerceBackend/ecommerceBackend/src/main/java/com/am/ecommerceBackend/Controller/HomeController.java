package com.am.ecommerceBackend.Controller;

import com.am.ecommerceBackend.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public ApiResponse HomeController(){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Welcome to Ecommerce Multivendor Application");
        return apiResponse;
    }
}
