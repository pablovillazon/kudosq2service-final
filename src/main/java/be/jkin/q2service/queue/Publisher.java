package be.jkin.q2service.queue;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Publisher {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String queue;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    public String SendMessageToQueue(String message)
    {
        //amqpTemplate.convertAndSend(exchange, routingKey, message);
        amqpTemplate.convertAndSend(exchange, "q2-kudos-key-k2u", message);
        return "Message sent to Queue!";
    }
}
