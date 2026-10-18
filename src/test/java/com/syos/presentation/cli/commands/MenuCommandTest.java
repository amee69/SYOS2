package com.syos.presentation.cli.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuCommand Tests")
class MenuCommandTest {

    @Mock
    Runnable mockAction;

    @Test
    @DisplayName("execute() should run the associated action")
    void execute_RunsAction() {
        // Arrange
        MenuCommand command = new MenuCommand("Test Command", mockAction);

        // Act
        command.execute();

        // Assert
        // Verify that the runnable's .run() method was actually clicked
        verify(mockAction).run();
    }
}