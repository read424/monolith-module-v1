package com.walrex.user.module_users.domain.model;

public record User(String id, String name, String lastName, String email, String password, Boolean status, String roles) {}
