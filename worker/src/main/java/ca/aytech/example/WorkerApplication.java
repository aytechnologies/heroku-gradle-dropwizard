package ca.aytech.example;

import ca.aytech.examples.config.AppConfiguration;
import ca.aytech.examples.hibernate.Memo;
import ca.aytech.examples.hibernate.MemoDAO;
import ca.aytech.examples.queue.QueueConfig;
import ca.aytech.examples.queue.QueueHelper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;

/**
 * Created by amin on 11/13/15.
 */
public class WorkerApplication extends Application<AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new WorkerApplication().run(args);
    }

    private final HibernateBundle<AppConfiguration> hibernate = new HibernateBundle<AppConfiguration>(Memo.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(AppConfiguration configuration, Environment environment) throws Exception {
        SessionFactory sessionFactory = hibernate.getSessionFactory();
        MemoDAO memoDAO = new MemoDAO(sessionFactory);

        QueueConfig queueConfig = configuration.getQueueConfig();
        ConnectionFactory connectionFactory = QueueHelper.getQueue(queueConfig);

        Channel channel = connectionFactory.newConnection().createChannel();

        channel.exchangeDeclare(queueConfig.getExchangeName(), "direct", true);
        channel.queueDeclare(queueConfig.getQueueName(), true, false, false, null);
        channel.queueBind(queueConfig.getQueueName(), queueConfig.getExchangeName(), queueConfig.getRoutingKey());

        channel.basicConsume(queueConfig.getQueueName(), false, "myConsumerTag", new MemoWorker(memoDAO, channel, sessionFactory));

    }
}
