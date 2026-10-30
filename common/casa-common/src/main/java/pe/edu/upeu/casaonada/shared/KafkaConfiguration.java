package pe.edu.upeu.casaonada.shared;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.*;
import org.springframework.util.backoff.FixedBackOff;
@Configuration
public class KafkaConfiguration {
    @Bean NewTopic operacionesTopic(){return TopicBuilder.name(Outbox.TOPIC).partitions(3).replicas(1).build();}
    @Bean NewTopic operacionesDlt(){return TopicBuilder.name(Outbox.TOPIC+".DLT").partitions(3).replicas(1).build();}
    @Bean CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String,String> template){
        var recoverer=new DeadLetterPublishingRecoverer(template,(r,e)->new TopicPartition(r.topic()+".DLT",r.partition()));
        recoverer.setFailIfSendResultIsError(true);
        return new DefaultErrorHandler(recoverer,new FixedBackOff(1000L,3));
    }
}
