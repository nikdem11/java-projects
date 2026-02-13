package pl.fuzzy.industrial;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.sourceforge.jFuzzyLogic.FIS;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import java.util.Set;


public class App {
    private static final String FCL_PATH = "src/main/resources/logic.fcl";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        FuzzyService fuzzyService = new FuzzyService(FCL_PATH);
        if (fuzzyService.getFis() == null) return;

        KafkaInfrastructure.createTopicsIfNeeded(BOOTSTRAP_SERVERS);

        RuleGui.show(fuzzyService);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> source = builder.stream("industrial-raw-data");

        source.mapValues(value -> {
            try {
                JsonNode json = mapper.readTree(value);

                String productId = json.has("product_id") ? json.get("product_id").asText() : "UNKNOWN";

                FIS fis = fuzzyService.getFis();

                // Pobranie listęy zmiennych wejciowych z pliku .fcl
                Set<String> expectedInputs = fuzzyService.getInputVariables();

                // Przypisanie do nich wartosci ze streamu
                for (String varName : expectedInputs) {
                    if (json.has(varName)) {
                        double val = json.get(varName).asDouble();
                        fis.setVariable(varName, val);
                    }
                }

                fis.evaluate();

                double riskValue = fis.getVariable("risk").getValue();

                ObjectNode result = mapper.createObjectNode();

                result.put("risk", (Math.round(riskValue * 100.0) / 100.0));
                result.put("status", riskValue > 70 ? "CRITICAL" : "NORMAL");
                result.put("product_id", productId);

                return result.toString();
            } catch (Exception e) {
                return "{\"error\": \"Data processing error\"}";
            }
        }).to("fuzzy-alerts");

        KafkaStreams streams = new KafkaStreams(builder.build(),
                KafkaInfrastructure.getStreamsConfig("predictive-maintenance-engine", BOOTSTRAP_SERVERS));
        streams.start();
    }
}
