package ca.aytech.examples.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rabbitmq.client.ConnectionFactory;
import io.dropwizard.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by amin on 11/13/15.
 */
public class QueueConfig extends Configuration{
    private static final Logger logger = LoggerFactory.getLogger(QueueConfig.class);

    private ConnectionFactory queue;

    private String queueUrl;

    @JsonProperty
    private String exchangeName;

    @JsonProperty
    private String routingKey;

    @JsonProperty
    private String queueName;

    @JsonProperty("queueUrl")
    public String getQueueUrl() {
        if (System.getenv("CLOUDAMQP_URL")!=null) {
            return System.getenv("CLOUDAMQP_URL");
        }else {
            return queueUrl;
        }
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    @JsonProperty("queueUrl")
    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
