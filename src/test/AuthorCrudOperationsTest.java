package test;

import dao.AuthorCrudOperations;
import dao.Criteria;
import entity.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

class AuthorCrudOperationsTest {
    // Always rename the class to test to 'subject'
    AuthorCrudOperations subject = new AuthorCrudOperations();

    @BeforeEach
    void setUp() {
        subject = new AuthorCrudOperations();
    }


    @Test
    void read_all_authors_ok() {
        // Test for data and potential mock
        Author expectedAuthor = authorJJR();
        subject.saveAll(List.of(expectedAuthor));

        // Subject and the function to test
        List<Author> actual = subject.getAll(1, 10);

        // Assertions : verification to be made automatically
        assertTrue(actual.contains(expectedAuthor));
    }

    @Test
    void read_author_by_id_ok() {
        Author expectedAuthor = authorJJR();

        Author actual = subject.findById(expectedAuthor.getId());

        assertEquals(expectedAuthor, actual);
    }

    @Test
    void create_then_update_author_ok() throws SQLException {
        var authors = newAuthor("author6_id", "JK Rowling", LocalDate.of(1972, 1, 1));

        var actual = subject.saveAll(List.of(authors));

        var existingAuthors = subject.getAll(1, 10);
        assertEquals(List.of(authors), actual);
        assertTrue(existingAuthors.containsAll(actual));
    }


    @Test
    void read_authors_filter_by_name_or_birthday_between_intervals() {
        ArrayList<Criteria> criteria = new ArrayList<>();
        criteria.add(new Criteria("name", "rado"));
        criteria.add(new Criteria("birth_date", LocalDate.of(2000, 1, 1)));
        List<Author> expected = List.of(
                authorJJR(),
                authorRado());

        List<Author> actual = subject.findByCriteria(criteria);

        assertEquals(expected, actual);
        assertTrue(actual.stream()
                .allMatch(author -> author.getName().toLowerCase().contains("rado")
                || author.getBirthDate().equals(LocalDate.of(2000, 1, 1))));

    }

    private Author authorRado() {
        return newAuthor("author2_id", "Rado", LocalDate.of(1990, 1, 1));
    }


    @Test
    void read_authors_order_by_name_or_birthday_or_both() {
        Author author1 = newAuthor("author3_id", "Clarisse R", LocalDate.of(1920, 1, 1));
        Author author2 = authorJJR();
        Author author3 = newAuthor("author6_id", "JK Rowling", LocalDate.of(1972, 1, 1));
        Author author4 = newAuthor("author4_id", "Machiavelli", LocalDate.of(1700, 2, 2));
        Author author5 = newAuthor("author5_id", "Plato", LocalDate.of(300, 3, 3));
        Author author6 = authorRado();



        List<Author> actual = subject.findAllSortedBy("name", "ASC", 1, 10);

        List<Author> expected = List.of(author1, author2, author3, author4, author5, author6);
        assertEquals(expected, actual);
    }

    @Test
    void read_filtered_ordered_and_paginated_ok() {
        Author author1 = newAuthor("author5_id", "Plato", LocalDate.of(300, 3, 3));
        Author author2 = newAuthor("author4_id", "Machiavelli", LocalDate.of(1700, 2, 2));
        Author author3 = newAuthor("author3_id", "Clarisse R", LocalDate.of(1920, 1, 1));
        Author author4 = newAuthor("author2_id", "Rado", LocalDate.of(1990, 1, 1));

        List<Criteria> criteria = List.of(new Criteria("name", "%a%"));

        List<Author> actual = subject.findByCriteriaSortedAndPaginated(criteria, "birth_date", "asc", 1, 5);
        List<Author> expected = List.of(author1, author2, author3, author4);
        assertEquals(expected, actual);
    }


    private Author authorJJR() {
        Author expectedAuthor = new Author();
        expectedAuthor.setId("author1_id");
        expectedAuthor.setName("JJR");
        expectedAuthor.setBirthDate(LocalDate.of(2000, 1, 1));
        return expectedAuthor;
    }

    private Author newAuthor(String id, String name, LocalDate birthDate) {
        Author author = new Author();
        author.setId(id);
        author.setName(name);
        author.setBirthDate(birthDate);
        return author;
    }

    @Test
    void delete_author_by_id_ok() {
        Author author = newAuthor("author_7", "JRR Tolkien", LocalDate.of(1985, 5, 6));
        subject.saveAll(List.of(author));

        boolean deleteById = subject.deleteById(author.getId());
        assertTrue(deleteById);

        Author fetched = subject.findById(author.getId());
        assertNull(fetched);
    }
}

