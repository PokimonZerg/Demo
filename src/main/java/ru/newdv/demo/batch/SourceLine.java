package ru.newdv.demo.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static ru.newdv.demo.batch.SourceLine.SOURCE_LINE_BEAN_NAME;

/**
 * Класс является prototype bean, потому что мы используем {@link BeanWrapperFieldSetMapper}.
 */
@Component(SOURCE_LINE_BEAN_NAME)
@Scope(SCOPE_PROTOTYPE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceLine {

    static final String SOURCE_LINE_BEAN_NAME = "sourceLine";

    private int id;
    private String name;
    private BigDecimal value;
}
