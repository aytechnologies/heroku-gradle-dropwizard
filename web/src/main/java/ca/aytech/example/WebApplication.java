package ca.aytech.example;

import ca.aytech.examples.config.AppConfiguration;
import ca.aytech.examples.hibernate.Memo;
import ca.aytech.examples.hibernate.MemoDAO;
import ca.aytech.examples.queue.QueueHealthCheck;
import ca.aytech.examples.queue.QueueHelper;
import com.rabbitmq.client.ConnectionFactory;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.hibernate.SessionFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

/**
 * Created by amin on 11/13/15.
 */
public class WebApplication extends Application<AppConfiguration> {
    public static void main(String[] args) throws Exception {
        new WebApplication().run(args);
    }

    private final HibernateBundle<AppConfiguration> hibernate = new HibernateBundle<AppConfiguration>(Memo.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/ui", "/", "index.html"));
        bootstrap.addBundle(hibernate);
        bootstrap.addBundle(new MigrationsBundle<AppConfiguration>() {

            @Override
            public PooledDataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(AppConfiguration configuration, Environment environment) throws Exception {
        SessionFactory sessionFactory = hibernate.getSessionFactory();
        MemoDAO memoDAO = new MemoDAO(sessionFactory);

        ConnectionFactory connectionFactory = QueueHelper.getQueue(configuration.getQueueConfig());

        environment.healthChecks().register("RabbitMQHealthCheck", new QueueHealthCheck(connectionFactory));

        environment.jersey().register(new MemoResource(configuration.getQueueConfig(), memoDAO));

        // CORS Settings to allow cross origin requests
        FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filter.setInitParameter("allowCredentials", "true");
    }
}
