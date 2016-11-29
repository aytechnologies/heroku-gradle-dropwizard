package ca.aytech.example;

import ca.aytech.examples.hibernate.Memo;
import ca.aytech.examples.hibernate.MemoDAO;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Created by amin on 11/13/15.
 */
public class MemoWorker extends DefaultConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MemoWorker.class);

    private final MemoDAO memoDAO;
    private final Channel channel;
    private final SessionFactory sessionFactory;
    public MemoWorker(MemoDAO memoDAO, Channel channel, SessionFactory sessionFactory) {
        super(channel);
        this.memoDAO = memoDAO;
        this.sessionFactory = sessionFactory;
        this.channel = channel;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        long deliveryTag = envelope.getDeliveryTag();

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(body);
        buffer.flip(); //needs flip
        Long memoId = buffer.getLong();

        logger.info("retrieved message from MQ for memo id: " + memoId);

        //handle session manually
        Session session = sessionFactory.openSession();
        try {
            Transaction tx = session.beginTransaction();
            try {
                ManagedSessionContext.bind(session);

                Optional<Memo> maybeMemo = memoDAO.find(memoId);
                if(maybeMemo.isPresent()){
                    Memo memo = maybeMemo.get();
                    memo.setStatus(Memo.STATUS.Processed.name());
                    memoDAO.createOrUpdate(memo);
                }else{
                    logger.warn("Can't find memo with the memoId!");
                }
                session.flush();
                tx.commit();
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
                tx.rollback();
                throw e;
            }


            channel.basicAck(deliveryTag, false);

        } catch(RuntimeException e){
            channel.basicReject(deliveryTag, true);
        } finally {
            session.close();
        }
    }
}
