package cr.ac.ucr.apiconsumer.models;

import java.util.List;

public class WeatherResponse {

    private Main main;
    private List<Weather> weather;
    private String name;
    private Sys sys;

    public WeatherResponse() {

    }

    public WeatherResponse(Main main, List<Weather> weather, String name, Sys sys) {
        this.main = main;
        this.weather = weather;
        this.name = name;
        this.sys = sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public Sys getSys() {
        return sys;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "main=" + main +
                ", weather=" + weather +
                ", name='" + name + '\'' +
                ", sys=" + sys +
                '}';
    }
}
