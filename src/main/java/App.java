import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class App {

    private static final int PERCENT = 90;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Wrong parameter");
            System.out.println("Usage: java -jar tickets.jar <filepath>");
            System.out.println("<filepath> - path to the valid json file.");
            return;
        }

        try {
            FlightArchive flightArchive = readFlightArchiveFromFile(args[0]);

            if (flightArchive == null) {
                System.out.println("File is empty");
                return;
            }

            long[] durations = computeFlightDurationsInMinutes(flightArchive.getTickets());

            long avg = (long) average(durations);
            System.out.printf("Average = %d h, %d min%n", avg / 60, avg % 60);

            Arrays.sort(durations);
            long percentile = (long) percentile(durations, PERCENT, true);
            System.out.printf("90-percentile = %d h, %d min%n", percentile / 60, percentile % 60);

        } catch (IOException | IllegalArgumentException e) {
            System.out.print("Error! -- ");
            System.out.println(e.getMessage());
        }
    }

    private static FlightArchive readFlightArchiveFromFile(String path) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(new FileReader(path, StandardCharsets.UTF_8), FlightArchive.class);
    }

    private static long[] computeFlightDurationsInMinutes(List<Ticket> tickets) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
        long[] durations = new long[tickets.size()];
        for (int i = 0; i < tickets.size(); ++i) {
            Ticket ticket = tickets.get(i);
            LocalDateTime departureDateTime = LocalDateTime.parse(ticket.getDepartureDate() + " " + ticket.getDepartureTime(), dateTimeFormatter);
            LocalDateTime arrivalDateTime = LocalDateTime.parse(ticket.getArrivalDate() + " " + ticket.getArrivalTime(), dateTimeFormatter);
            ZonedDateTime zonedDeptDateTime = ZonedDateTime.of(departureDateTime, ZoneId.of("Asia/Vladivostok"));
            ZonedDateTime zonedArrivalDateTime = ZonedDateTime.of(arrivalDateTime, ZoneId.of("Asia/Tel_Aviv"));
            Duration duration = Duration.between(zonedDeptDateTime, zonedArrivalDateTime);
            durations[i] = duration.toMinutes();
        }

        return durations;
    }

    private static double average(long[] numbers) {
        return Arrays.stream(numbers).average().orElseThrow();
    }

    private static double percentile(long[] durations, int percent, boolean sorted) throws IllegalArgumentException {
        if (percent > 100) {
            throw new IllegalArgumentException(String.format("percent can't be greater than 100 (provided %d)%n", percent));
        }
        if (!sorted) {
            Arrays.sort(durations);
        }
        double n = (double) durations.length * percent / 100.0 - 1; //"индекс" процентиля
        if (n < 0) {
            return durations[0];
        }
        if (n == durations.length - 1) {
            return durations[durations.length - 1];
        }

        int integerPart = (int)n;
        double fractionalPart = n % 1;
        return durations[integerPart] + (durations[integerPart + 1] - durations[integerPart]) * fractionalPart;
    }

}
