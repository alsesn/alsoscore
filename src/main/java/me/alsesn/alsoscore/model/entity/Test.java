package me.alsesn.alsoscore.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.alsesn.alsoscore.model.enums.TestStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private TestStatus status;

    @OneToMany(mappedBy = "test",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore
    private List<Question> questions = new ArrayList<>();
}