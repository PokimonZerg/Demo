package ru.newdv.demo.batch;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static ru.newdv.demo.batch.SourceLine.SOURCE_LINE_BEAN_NAME;

@Component(SOURCE_LINE_BEAN_NAME)
@Scope(SCOPE_PROTOTYPE)
public class SourceLine {

    static final String SOURCE_LINE_BEAN_NAME = "sourceLine";

    private int id;
    private String name;
    private BigDecimal value;

    public SourceLine() {
    }

    public SourceLine(int id, String name, BigDecimal value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SourceLine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
