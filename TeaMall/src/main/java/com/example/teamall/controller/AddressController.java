package com.example.teamall.controller;

import com.example.teamall.common.Result;
import com.example.teamall.model.Address;
import com.example.teamall.model.User;
import com.example.teamall.service.AddressService;
import com.example.teamall.service.UserService;
import com.example.teamall.dto.AddressDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public Result<Address> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddressDTO addressDTO) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(addressService.create(user, addressDTO));
    }
    
    @PutMapping("/{id}")
    public Result<Address> update(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddressDTO addressDTO) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(addressService.update(id, user, addressDTO));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        addressService.delete(id, user);
        return Result.success();
    }
    
    @GetMapping("/{id}")
    public Result<Address> getById(@PathVariable Long id) {
        return Result.success(addressService.findById(id));
    }
    
    @GetMapping
    public Result<List<Address>> getList(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(addressService.findByUser(user));
    }
    
    @PostMapping("/{id}/default")
    public Result<Address> setDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(addressService.setDefault(id, user));
    }
} 