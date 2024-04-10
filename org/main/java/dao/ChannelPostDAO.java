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

    public void merge(Object o) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.merge(o);
        transaction.commit();
        session.close();
    }
    public void update(Object o) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.update(o);
        tx1.commit();
        session.close();
    }

    public void remove(Object o) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.remove(o);
        tx1.commit();
        session.close();
    }

    public Link findLinkById(int id) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(Link.class, id);
    }
    public List<ChannelPost> findAll() {
        return HibernateSessionFactoryUtil.getSessionFactory()
                .openSession().createQuery("From models.ChannelPost", ChannelPost.class).list();
    }
}

