package test;

import dao.AuthorCrudOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class AuthorTest {
    @BeforeEach
    void setUpConnection() {
        AuthorCrudOperations subject = new AuthorCrudOperations();
    }

    @Test
    void create_then_update_author() throws SQLException {

    }
}
