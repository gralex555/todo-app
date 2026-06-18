package com.todoproject.todo_app;

import com.todoproject.todo_app.event.TaskEventConsumer;
import com.todoproject.todo_app.event.TaskEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TodoAppApplicationTests {

	@MockitoBean
	private TaskEventProducer taskEventProducer;

	@MockitoBean
	private TaskEventConsumer taskEventConsumer;

	@Test
	void contextLoads() {
	}

}
