package ca.aytech.examples.queue;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by amin on 11/13/15.
 */
public class QueueHelper {
    private static ConnectionFactory queue = null;
    private static final Logger logger = LoggerFactory.getLogger(QueueHelper.class);

    public static ConnectionFactory getQueue(QueueConfig config) {
        if (queue != null) {
            return queue;
        }else{
            logger.info("Creating Connection Factory");
            logger.info("Queue: " + config.getQueueUrl());
            return createConnectionFactory(config.getQueueUrl());
        }
    }

    private static ConnectionFactory createConnectionFactory(String url){
        final URI ampqUrl;
        try {
            ampqUrl = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(ampqUrl.getUserInfo().split(":")[0]);
        factory.setPassword(ampqUrl.getUserInfo().split(":")[1]);
        factory.setHost(ampqUrl.getHost());
        factory.setPort(ampqUrl.getPort());
        if(ampqUrl.getPath().startsWith("/")) {
            factory.setVirtualHost(ampqUrl.getPath().substring(1));
        }
        return factory;
    }
}
