package be.jkin.q2service.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Subscriber {

    private static final Logger LOGGER = LoggerFactory.getLogger(Subscriber.class);

    @RabbitListener(queues = "kudos-queue")
    public void onMessageFromRabbitMQ(final String messageFromRabbitMQ)
    {
        LOGGER.info("{}", messageFromRabbitMQ);
    }
}
