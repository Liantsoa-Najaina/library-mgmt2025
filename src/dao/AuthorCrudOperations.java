package dao;

import db.DataSource;
import entity.Author;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AuthorCrudOperations implements CrudOperations<Author> {
    private final DataSource dataSource = new DataSource();
    Logger logger = Logger.getLogger("AuthorCrudOperations");

    @Override
    public List<Author> getAll(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("page must be greater than 0 but actual is " + page);
        }
        String sql = "select a.id, a.name, a.birth_date from author a order by a.id limit ? offset ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, size);
            preparedStatement.setInt(2, size * (page - 1));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return mapAuthorFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Author> mapAuthorFromResultSet(ResultSet resultSet) throws SQLException {
        List<Author> authors = new ArrayList<>();
        while (resultSet.next()) {
            Author author = new Author();
            author.setId(resultSet.getString("id"));
            author.setName(resultSet.getString("name"));
            author.setBirthDate(resultSet.getDate("birth_date").toLocalDate());
            authors.add(author);
        }
        return authors;
    }

    @Override
    public List<Author> findByCriteria(List<Criteria> criteria) {
        List<Author> authors = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select a.id, a.name, a.birth_date from author a where 1=1");
        for (Criteria c : criteria) {
            if ("name".equals(c.getColumn())) {
                sql.append(" and a.").append(c.getColumn()).append(" ilike '%").append(c.getValue().toString()).append("%'");
            } else if ("birth_date".equals(c.getColumn())) {
                sql.append(" or a.").append(c.getColumn()).append(" = '").append(c.getValue().toString()).append("'");
            }
        }
        sql.append(" order by a.id asc");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql.toString())) {
            return mapAuthorFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Author findById(String id) {
        String sql = "select a.id, a.name, a.birth_date, a.sex from author a where id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                Author author = new Author();
                while (resultSet.next()) {
                    author.setId(resultSet.getString("id"));
                    author.setName(resultSet.getString("name"));
                    author.setBirthDate(resultSet.getDate("birth_date").toLocalDate());
                }
                return author;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Author findById(Connection connection, String id) throws SQLException {
        String sql = "select a.id, a.name, a.birth_date from author a where id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Author author = new Author();
                    author.setId(resultSet.getString("id"));
                    author.setName(resultSet.getString("name"));
                    author.setBirthDate(resultSet.getDate("birth_date").toLocalDate());
                    return author;
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public List<Author> saveAll(List<Author> entities)  {
        List<Author> newAuthors = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            for (Author author : entities) {
                Author existing = findById(connection,author.getId());
                if (existing == null) {
                    try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO author (id, name, birth_date) VALUES (?, ?, ?)")) {
                        insertStatement.setString(1, author.getId());
                        insertStatement.setString(2, author.getName());
                        insertStatement.setDate(3, Date.valueOf(author.getBirthDate()));

                        insertStatement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE author SET name = ?, birth_date = ? WHERE id = ?")) {
                        updateStatement.setString(1, author.getName());
                        updateStatement.setDate(2, Date.valueOf(author.getBirthDate()));
                        updateStatement.setString(3, author.getId());

                        updateStatement.executeUpdate();
                    }
                }
                newAuthors.add(findById(connection,author.getId()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return newAuthors;
    }

    @Override
    public Author update(String id, Author updatedEntity) {
        String sql = "UPDATE author SET name = ?, birth_date = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, updatedEntity.getName());
            statement.setDate(2, Date.valueOf(updatedEntity.getBirthDate()));
            statement.setString(3, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                return updatedEntity;
            } else {
                logger.warning("No author found with ID: " + id);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean deleteById(String id) {
        String sql = "delete from author where id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
