package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase
class ShareItTests {

	@Test
	void contextLoads() {
		log.info("Spring контекст успешно загружен!");
	}

	@Test
	void mainMethodStarts() {
		ShareItServer.main(new String[] {});
		log.info("Приложение успешно запущено!");
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
			log.info("Подключение к базе данных успешно!");
			log.info("База данных: " + connection.getMetaData().getDatabaseProductName());
		}
	}

	@Test
	public void testJdbcTemplate() {
		Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		assertThat(result).isEqualTo(1);
		log.info("JdbcTemplate работает!");
	}
}
