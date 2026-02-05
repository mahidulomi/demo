package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserStoreTest {

    @AfterEach
    void cleanup() {
        // isolate tests from each other; disk persistence is handled by UserStore itself
        UserStore.clearInMemoryForTestOnly();
    }

    @Test
    void resetPassword_success_whenRecoveryDataMatches() {
        assertTrue(UserStore.createUser("Alice", "oldpass", "blue"));

        assertTrue(UserStore.resetPassword("Alice", "blue", "newpass"));
        assertTrue(UserStore.validateLogin("Alice", "newpass"));
        assertFalse(UserStore.validateLogin("Alice", "oldpass"));
    }

    @Test
    void resetPassword_fails_whenRecoveryDataWrong() {
        assertTrue(UserStore.createUser("Bob", "oldpass", "cat"));

        assertFalse(UserStore.resetPassword("Bob", "dog", "newpass"));
        assertTrue(UserStore.validateLogin("Bob", "oldpass"));
        assertFalse(UserStore.validateLogin("Bob", "newpass"));
    }

    @Test
    void resetPassword_fails_whenPersonalDataMissing() {
        // legacy account creation without recovery data
        assertTrue(UserStore.createUser("Charlie", "pass"));

        assertFalse(UserStore.resetPassword("Charlie", "anything", "newpass"));
        assertTrue(UserStore.validateLogin("Charlie", "pass"));
    }
}
