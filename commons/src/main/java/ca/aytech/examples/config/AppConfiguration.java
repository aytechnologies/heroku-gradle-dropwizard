package ca.aytech.examples.config;

import ca.aytech.examples.queue.QueueConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by amin on 11/13/15.
 */
public class AppConfiguration extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("queue")
    private QueueConfig queueConfig;

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        if (System.getenv("DATABASE_URL")!=null){
            DatabaseConfiguration databaseConfiguration = null;
            try {
                URI dbUri = new URI(System.getenv("DATABASE_URL"));
                final String user = dbUri.getUserInfo().split(":")[0];
                final String password = dbUri.getUserInfo().split(":")[1];
                final String url = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
                        + "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
                databaseConfiguration = new DatabaseConfiguration() {
                    DataSourceFactory dataSourceFactory;

                    @Override
                    public DataSourceFactory getDataSourceFactory(Configuration configuration) {
                        if (dataSourceFactory != null) {
                            return dataSourceFactory;
                        }
                        DataSourceFactory dsf = new DataSourceFactory();
                        dsf.setUser(user);
                        dsf.setPassword(password);
                        dsf.setUrl(url);
                        dsf.setDriverClass("org.postgresql.Driver");
                        dsf.setInitialSize(3);
                        dsf.setMaxSize(5);
                        dsf.setMinSize(1);
                        dataSourceFactory = dsf;
                        return dsf;
                    }
                };
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if(databaseConfiguration != null){
                return (DataSourceFactory) databaseConfiguration.getDataSourceFactory(null);
            }else{
                throw new RuntimeException("Can't get database!");
            }
        }else{
            return database;
        }
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    public QueueConfig getQueueConfig() {
        return queueConfig;
    }

    public void setQueueConfig(QueueConfig queueConfig) {
        this.queueConfig = queueConfig;
    }
}

