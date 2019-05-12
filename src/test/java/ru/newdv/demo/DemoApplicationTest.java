package ru.newdv.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.newdv.demo.data.SourceLineData;
import ru.newdv.demo.data.SourceLineRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTest {

    @Autowired
    private SourceLineRepository repository;

    @Test
    void processSourceTest() {
        Optional<SourceLineData> data = repository.findById(2L);

        assertTrue(data.isPresent(), "Second record exist in db");
        SourceLineData line = data.get();
        assertEquals("AAB", line.getName());
        assertEquals(new BigDecimal("1.50"), line.getValue());
    }
}
