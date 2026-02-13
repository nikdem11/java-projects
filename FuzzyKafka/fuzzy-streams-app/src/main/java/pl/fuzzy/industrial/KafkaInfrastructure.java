package pl.fuzzy.industrial;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.common.serialization.Serdes;
import java.util.*;

public class KafkaInfrastructure {
    public static void createTopicsIfNeeded(String bootstrapServers) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(props)) {
            List<NewTopic> topics = Arrays.asList(
                    new NewTopic("industrial-raw-data", 1, (short) 1),
                    new NewTopic("fuzzy-alerts", 1, (short) 1)
            );
            adminClient.createTopics(topics).all().get();
            System.out.println("[System] Topiki utworzone.");
        } catch (Exception e) {
            if (e.getCause() instanceof TopicExistsException){
                System.out.println("[System] Topiki juz istnieja.");
            }
            else System.out.println(e.getMessage());
        }
    }

    public static Properties getStreamsConfig(String appId, String bootstrapServers) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        return props;
    }
}
