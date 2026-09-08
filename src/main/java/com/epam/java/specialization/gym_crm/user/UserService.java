package com.epam.java.specialization.gym_crm.user;

public interface UserService {

    String prepareUserCredentials(User user);
    void toggleActivation(String username, boolean isActive);
}