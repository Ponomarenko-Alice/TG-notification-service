package service;

import dao.ChannelPostDAO;
import jakarta.persistence.OneToMany;
import models.ChannelPost;
import models.Link;

import java.util.List;
import java.util.Set;

public class ChannelPostService {

    private ChannelPostDAO channelPostDAO = new ChannelPostDAO();
    @OneToMany(mappedBy="channel_post")
    private Set<Link> linkSet;

    public ChannelPostService() {
    }

    public ChannelPost find(int id) {
        return channelPostDAO.findById(id);
    }

    public void save(ChannelPost channelPost) {
        channelPostDAO.save(channelPost);
    }

    public void delete(ChannelPost channelPost) {
        channelPostDAO.delete(channelPost);
    }

    public void update(ChannelPost channelPost) {
        channelPostDAO.update(channelPost);
    }

    public List<ChannelPost> findAll() {
        return channelPostDAO.findAll();
    }

    public Link findMessageThreadById(int id) {
        return channelPostDAO.findMessageThreadById(id);
    }


}