package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShareItTests {

	@Test
	void contextLoads() {
		//assertTrue(true);
		System.out.println("Spring контекст успешно загружен!");
	}

	@Test
	void mainMethodStarts() {
		ShareItApp.main(new String[] {});
		System.out.println("Приложение успешно запущено!");
	}

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void testDatabaseConnection() throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			assertThat(connection).isNotNull();
			assertThat(connection.isValid(1)).isTrue();
			System.out.println("Подключение к базе данных успешно!");
			System.out.println("База данных: " + connection.getMetaData().getDatabaseProductName());
		}
	}

	@Test
	public void testJdbcTemplate() {
		Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		assertThat(result).isEqualTo(1);
		System.out.println("JdbcTemplate работает!");
	}
}
