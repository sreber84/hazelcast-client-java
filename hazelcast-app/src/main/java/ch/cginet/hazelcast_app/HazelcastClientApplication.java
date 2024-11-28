package ch.cginet.hazelcast_app;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@SpringBootApplication
public class HazelcastClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(HazelcastClientApplication.class, args);
    }

    @Value("${HAZELCAST_SERVICE:localhost:5701}")
    private String hazelcastService;

    @Value("${HAZELCAST_MAP:default-map}")
    private String hazelcastMap;

    @Bean
    public HazelcastInstance hazelcastInstance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(hazelcastService);
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    @Bean
    public CommandLineRunner runner(HazelcastInstance hazelcastInstance) {
        return args -> {
            IMap<String, String> map = hazelcastInstance.getMap(hazelcastMap);
            new Thread(() -> {
                while (true) {
                    try {
                        String key = UUID.randomUUID().toString();
                        String value = "Value-" + System.currentTimeMillis();
                        map.put(key, value);
                        System.out.printf("Put entry: %s -> %s%n", key, value);

                        Thread.sleep(50);

                        System.out.println("Current Map Entries: " + map.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        };
    }
}

@RestController
class HealthCheckController {

    private final HazelcastInstance hazelcastInstance;

    @Value("${HAZELCAST_MAP:default-map}")
    private String hazelcastMap;

    public HealthCheckController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @GetMapping("/health")
    public String healthCheck() {
        try {
            // Attempt to access the map to check connectivity
            hazelcastInstance.getMap(hazelcastMap).size();
            return "Hazelcast is available";
        } catch (Exception e) {
            return "Hazelcast is not available: " + e.getMessage();
        }
    }
}