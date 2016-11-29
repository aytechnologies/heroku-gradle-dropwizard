package ca.aytech.examples.hibernate;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import java.util.List;
import java.util.Optional;

/**
 * Created by amin on 11/13/15.
 */
public class MemoDAO extends AbstractDAO<Memo> {

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory a session provider
     */
    public MemoDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Long total(){
        return (Long) criteria().setProjection(Projections.rowCount()).uniqueResult();
    }

    public Memo createOrUpdate(Memo memo) {
        return persist(memo);
    }

    public List<Memo> list() { return list(criteria().addOrder(Order.desc("id"))); }

    public Optional<Memo> find(Long memoId) { return Optional.ofNullable(get(memoId)); }
}
