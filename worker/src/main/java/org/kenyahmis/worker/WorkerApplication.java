package org.kenyahmis.worker;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableAsync
public class WorkerApplication {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
    // Events Consumer configs
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Integer, String> eventsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventsConsumerFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(2);
        return factory;
    }

    @Bean
    public ConsumerFactory<Integer, String> eventsConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(eventsConsumerConfigs());
    }

    @Bean
    public Map<String, Object> eventsConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TYPE_MAPPINGS, "eventBaseMessage:org.kenyahmis.shared.dto.EventBaseMessage" );
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }


    // Reporting Manifest Consumer configs
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Integer, String> manifestKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(manifestConsumerFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public ConsumerFactory<Integer, String> manifestConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(manifestConsumerConfigs());
    }

    @Bean
    public Map<String, Object> manifestConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TYPE_MAPPINGS, "manifestMessage:org.kenyahmis.shared.dto.ManifestMessage" );
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    // Upload Metrics Consumer configs
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> uploadMetricsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(uploadMetricsConsumerFactory());
        factory.setBatchListener(true);
        factory.setConcurrency(3);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> uploadMetricsConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(uploadMetricsConsumerConfigs());
    }

    @Bean
    public Map<String, Object> uploadMetricsConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TYPE_MAPPINGS, "uploadMetricsMessage:org.kenyahmis.shared.dto.UploadMetricsMessage");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

}
