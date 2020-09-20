package cr.ac.ucr.apiconsumer.api;

import cr.ac.ucr.apiconsumer.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface WeatherService {

    //weather?lat={lat}&lon={lon}

    @Headers("Content-Type: application/json")
    @GET("weather")
    Call<WeatherResponse> getWeatherByCoordinates(@Query("lat") double lat, @Query("lon") double lon);
}
