package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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

}
