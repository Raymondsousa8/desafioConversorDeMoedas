import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.*;

public class ExchangeRateAPIClient {
    private static final String API_KEY = "sua-chave-aqui";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private static final Map<String, String> currencies = new HashMap<>();
    private static final List<String> history = new ArrayList<>();

    static {
        currencies.put("USD", "Dólar dos Estados Unidos (Estados Unidos)");
        currencies.put("EUR", "Euro (União Europeia)");
        currencies.put("GBP", "Libra Esterlina (Reino Unido)");
        currencies.put("JPY", "Iene (Japão)");
        currencies.put("CAD", "Dólar Canadense (Canadá)");
        currencies.put("AUD", "Dólar Australiano (Austrália)");
        currencies.put("CHF", "Franco Suíço (Suíça)");
        currencies.put("CNY", "Yuan Chinês (China)");
        currencies.put("BRL", "Real (Brasil)");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Converter moeda");
            System.out.println("2. Ver histórico de conversões");
            System.out.println("3. Sair");

            int choice = scanner.nextInt();
            scanner.nextLine();  // Consumir a nova linha após o número

            switch (choice) {
                case 1:
                    convertCurrency(scanner);
                    break;
                case 2:
                    showHistory();
                    break;
                case 3:
                    System.out.println("Saindo...");
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private static void convertCurrency(Scanner scanner) {
        System.out.println("Moedas suportadas:");
        int index = 1;
        for (Map.Entry<String, String> entry : currencies.entrySet()) {
            System.out.printf("%d. %s - %s%n", index++, entry.getKey(), entry.getValue());
        }

        System.out.println("Escolha o número da moeda de origem:");
        int sourceIndex = scanner.nextInt();
        System.out.println("Escolha o número da moeda de destino:");
        int targetIndex = scanner.nextInt();
        System.out.println("Digite o valor a ser convertido:");
        double amount = scanner.nextDouble();

        String sourceCurrency = getCurrencyCode(sourceIndex);
        String targetCurrency = getCurrencyCode(targetIndex);

        if (sourceCurrency == null || targetCurrency == null) {
            System.out.println("Escolha inválida de moeda.");
            return;
        }

        double conversionRate = getConversionRate(sourceCurrency, targetCurrency);
        if (conversionRate != -1) {
            double result = amount * conversionRate;
            System.out.printf("Valor convertido de %s para %s: %.2f%n", sourceCurrency, targetCurrency, result);

            String logEntry = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
                    " - " + sourceCurrency + " -> " + targetCurrency + ": " + amount + " = " + result;
            history.add(logEntry);
        } else {
            System.out.println("Erro ao obter taxa de câmbio.");
        }
    }

    private static String getCurrencyCode(int index) {
        int i = 1;
        for (String code : currencies.keySet()) {
            if (i == index) {
                return code;
            }
            i++;
        }
        return null;
    }

    private static double getConversionRate(String sourceCurrency, String targetCurrency) {
        String url = BASE_URL + API_KEY + "/pair/" + sourceCurrency + "/" + targetCurrency;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                return jsonObject.get("conversion_rate").getAsDouble();
            } else {
                return -1; // Indica erro na obtenção da taxa de câmbio
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Indica erro na obtenção da taxa de câmbio
        }
    }

    private static void showHistory() {
        System.out.println("Histórico de conversões:");
        for (String entry : history) {
            System.out.println(entry);
        }
    }
}
