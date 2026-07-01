import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class TestOpenApi {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://localhost:8080/v3/api-docs");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            
            InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is != null) {
                try (Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
                    System.out.println(scanner.hasNext() ? scanner.next() : "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
