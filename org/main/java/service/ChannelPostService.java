package service;

import dao.ChannelPostDAO;
import jakarta.persistence.OneToMany;
import models.ChannelPost;
import models.Link;

import java.util.List;
import java.util.Set;

public class ChannelPostService {

    private final ChannelPostDAO channelPostDAO = new ChannelPostDAO();
    @OneToMany(mappedBy="channel_post")
    private Set<Link> linkSet;

    public ChannelPostService() {
    }

    public Object find(int id) {
        return channelPostDAO.findById(id);
    }

    public void save(Object o) {
        channelPostDAO.merge(o);
    }


    public void delete(Object o) {
        channelPostDAO.remove(o);
    }

    public void update(Object o) {
        channelPostDAO.update(o);
    }



    public List<ChannelPost> findAllChannelPosts() {
        return channelPostDAO.findAll();
    }

    public Link findLinkById(int id) {
        return channelPostDAO.findLinkById(id);
    }


}