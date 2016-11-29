package ca.aytech.example;

import ca.aytech.examples.hibernate.Memo;
import ca.aytech.examples.hibernate.MemoDAO;
import ca.aytech.examples.queue.QueueConfig;
import ca.aytech.examples.queue.QueueHelper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.dropwizard.hibernate.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by amin on 11/13/15.
 */
@Path("/memos")
@Produces(MediaType.APPLICATION_JSON)
public class MemoResource {
    private static final Logger logger = LoggerFactory.getLogger(MemoResource.class);

    private final ConnectionFactory connectionFactory;
    private final QueueConfig queueConfig;
    private final MemoDAO memoDAO;

    public MemoResource(QueueConfig queueConfig, MemoDAO memoDAO) {
        this.queueConfig = queueConfig;
        this.memoDAO = memoDAO;
        this.connectionFactory = QueueHelper.getQueue(queueConfig);
    }

    @POST
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Memo addMemo(@FormParam(value="content") String content) throws IOException, TimeoutException {
        Memo memo = new Memo();
        memo.setContent(content);
        memo.setStatus(Memo.STATUS.Processing.name());
        memo = memoDAO.createOrUpdate(memo);
        addToQueue(memo.getId());
        return memo;
    }

    private void addToQueue(Long memoId) throws IOException, TimeoutException {
        logger.info("adding a message to queue.");

        Connection connection = null;
        Channel channel = null;
        try {
            connection= connectionFactory.newConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(queueConfig.getExchangeName(), "direct", true);
            channel.queueDeclare(queueConfig.getQueueName(), true, false, false, null);
            channel.queueBind(queueConfig.getQueueName(), queueConfig.getExchangeName(), queueConfig.getRoutingKey());

            //will send the memoID as the body of message to MQ
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(memoId);

            channel.basicPublish(queueConfig.getExchangeName(),
                    queueConfig.getRoutingKey(),
                    false, false, new AMQP.BasicProperties(),
                    buffer.array());
            logger.info("message added to queue.");
        }catch(Exception e){
            if (channel!=null){
                channel.close();
            }
            if (connection!=null){
                connection.close();
            }
            logger.error("Error happened.", e);
        }
    }

    @GET
    @UnitOfWork
    public List<Memo> listMemos(){
        return memoDAO.list();
    }

    @GET
    @Path("/count")
    @UnitOfWork
    public Long memoCount(){
        return memoDAO.total();
    }
}
