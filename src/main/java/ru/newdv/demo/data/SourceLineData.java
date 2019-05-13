package ru.newdv.demo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "sources")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceLineData {

    @Id
    private long id;
    private String name;
    private BigDecimal value;
}
