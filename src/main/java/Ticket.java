import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class Ticket {

    @SerializedName("departure_date")
    String departureDate;

    @SerializedName("arrival_date")
    String arrivalDate;

    @SerializedName("departure_time")
    String departureTime;

    @SerializedName("arrival_time")
    String arrivalTime;

}
