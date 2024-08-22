package api.model.country;

public class Country {
    private String name;
    private String code;

    public Country(){

    }

    public Country(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
