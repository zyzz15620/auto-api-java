package apiCountry.model.country;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Country {
    private String name;
    private String code;
    private float gdp;
    @JsonProperty("private")
    private int fieldPrivate;
}
