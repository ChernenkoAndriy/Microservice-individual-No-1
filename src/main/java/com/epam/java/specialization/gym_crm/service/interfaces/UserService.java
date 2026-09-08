package com.epam.java.specialization.gym_crm.service.interfaces;

import com.epam.java.specialization.gym_crm.model.User;

public interface UserService {

    String prepareUserCredentials(User user);
    void toggleActivation(String username, boolean isActive);
}