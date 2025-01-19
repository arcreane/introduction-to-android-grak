package api;

import com.example.headsup.models.CardResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface CardService {
    // these are the headers for the api call, and the access key is added here (maybe add conf file for sensitive info)
    @Headers({
            "Content-Type: application/json",
            "X-Access-Key: $2a$10$OzFQFAckTrvP7dqykffHDOrqrGL/knp6E1I8zxbUWZ645FJ4p1qu2"
    })
//    the json bin path with path variable for the bin id
    @GET("v3/b/{binId}/")
    Call<CardResponse> getCards(@Path("binId") String binId);
}

