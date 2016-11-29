package ca.aytech.examples.queue;

import com.codahale.metrics.health.HealthCheck;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by amin on 11/13/15.
 */
public class QueueHealthCheck extends HealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(QueueHealthCheck.class);
    private final ConnectionFactory connectionFactory;

    public QueueHealthCheck(ConnectionFactory connectionFactory) {
        super();
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected Result check() throws Exception {
        Connection conn = null;
        try {
            conn = connectionFactory.newConnection();
            if (conn.isOpen()) {
                return Result.healthy();
            } else {
                return Result.unhealthy("Connection is closed.");
            }
        } catch (IOException e) {
            return Result.unhealthy("Cannot open connection. " + e.getMessage());
        } finally {
            if (conn != null)
                try {
                    logger.info("Closing connection.");
                    conn.close();
                } catch (IOException e) {
                    conn = null;
                    logger.info("Error closing connection.");
                }
        }
    }
}
