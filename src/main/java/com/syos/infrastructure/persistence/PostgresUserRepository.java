package com.syos.infrastructure.persistence;

import com.syos.domain.entity.User;
import com.syos.domain.repository.UserRepository;
import com.syos.infrastructure.database.DatabaseManager;
import java.sql.*;
import java.util.Optional;

public class PostgresUserRepository implements UserRepository {

    @Override
    public Optional<User> authenticate(String username, String password) {
        // select the role and username to build the session object
        String sql = "SELECT username, role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getString("username"),
                            rs.getString("role")
                    ));
                }
            }
        } catch (SQLException e) {

            throw new RuntimeException("Database Auth Error: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
}