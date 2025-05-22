package ru.testing.testSistem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.testing.testSistem.models.Test;
import ru.testing.testSistem.models.User;

import java.util.List;

@Data
@AllArgsConstructor
public class TestPermissionsDto {
    private Test test;
    private List<User> usersToAdd;
    private List<User> allowedUsers;
}