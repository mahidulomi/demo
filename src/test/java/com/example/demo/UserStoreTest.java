package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

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

    @Test
    void createUser_persistsAcrossReload() {
        String username = "persist_" + UUID.randomUUID().toString().replace("-", "");
        String password = "12345";

        assertTrue(UserStore.createUser(username, password, "river"));
        assertTrue(Files.exists(UserStore.storePathForTestOnly()));

        UserStore.clearInMemoryForTestOnly();
        // Store auto-refresh now repopulates from disk on demand.
        assertTrue(UserStore.validateLogin(username, password));

        UserStore.reloadFromDiskForTestOnly();
        assertTrue(UserStore.validateLogin(username, password));
    }

    @Test
    void createUser_withPipeInPassword_persistsAcrossReload() {
        String username = "persist_pipe_" + UUID.randomUUID().toString().replace("-", "");
        String password = "pa|ss=word:1";
        String personalData = "city|blue";

        assertTrue(UserStore.createUser(username, password, personalData));
        assertTrue(UserStore.validateLogin(username, password));

        UserStore.clearInMemoryForTestOnly();
        UserStore.reloadFromDiskForTestOnly();

        assertTrue(UserStore.validateLogin(username, password));
        assertTrue(UserStore.verifyRecoveryData(username, personalData));
    }

    @Test
    void validateLogin_autoRefreshesAfterExternalStoreChange() throws Exception {
        String teammate = "teammate_" + UUID.randomUUID().toString().replace("-", "");
        String teammatePass = "pass123";
        Path storePath = UserStore.storePathForTestOnly();

        assertTrue(UserStore.createUser("seed_" + UUID.randomUUID().toString().replace("-", ""), "12345", "x"));
        assertFalse(UserStore.validateLogin(teammate, teammatePass));

        // Ensure filesystem timestamp advances so change detection picks up this write.
        Thread.sleep(1100);

        Properties props = new Properties();
        if (Files.exists(storePath)) {
            try (InputStream in = Files.newInputStream(storePath)) {
                props.load(in);
            }
        }
        props.setProperty(teammate, teammatePass + "|blue");
        try (OutputStream out = Files.newOutputStream(storePath)) {
            props.store(out, "external update simulation");
        }

        assertTrue(UserStore.validateLogin(teammate, teammatePass));
    }
}
