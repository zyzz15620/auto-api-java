package api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CountryPagination {
    private int page;
    private int size;
    private int total;
    private List<CountryVerTwo> data;
}
