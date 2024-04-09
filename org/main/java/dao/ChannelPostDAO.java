package dao;

import models.ChannelPost;
import models.Link;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateSessionFactoryUtil;

import java.util.List;

public class ChannelPostDAO {

    public ChannelPost findById(int id) {
        return HibernateSessionFactoryUtil
                .getSessionFactory()
                .openSession()
                .get(ChannelPost.class, id);
    }

    public void save(ChannelPost channelPost) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.save(channelPost);
        transaction.commit();
        session.close();
    }
    public void update(ChannelPost channelPost) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.update(channelPost);
        tx1.commit();
        session.close();
    }

    public void delete(ChannelPost channelPost) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.delete(channelPost);
        tx1.commit();
        session.close();
    }

    public Link findMessageThreadById(int id) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(Link.class, id);
    }
    public List<ChannelPost> findAll() {
        return HibernateSessionFactoryUtil.getSessionFactory()
                .openSession().createQuery("From models.ChannelPost", ChannelPost.class).list();
    }
}

