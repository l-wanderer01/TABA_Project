package com.example.taba_project.model;

import lombok.Data;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
public class Info {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String age;

    private String gender;

    private String emotion;

    private String percentage;

    // Getter and Setter methods (Lombok @Data가 자동으로 생성해 줌)

    // 만약 JSON에서 "idInfo"라는 이름을 계속 쓰고 싶다면, 아래와 같이 @JsonProperty 사용
    @JsonProperty("idInfo")
    public Long getId() {
        return id;
    }

//    public void setId(Long id) {
//        this.id = id;
//    }
}