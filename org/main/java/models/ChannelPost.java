package models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "channel_post")
public class ChannelPost {
    @Id
    @Column(name = "channel_post_id")
    private int id;
    @Column(name = "has_link")
    private boolean hasLink;

    @OneToMany(mappedBy = "channelPost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Link> links;
    public ChannelPost() {
    }

    public ChannelPost(int id, boolean hasLink) {
        this.id = id;
        this.hasLink = hasLink;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public int getId() {
        return id;
    }

    public void setId(int message_id) {
        this.id = message_id;
    }

    public boolean isHasLink() {
        return hasLink;
    }

    public void setHasLink(boolean hasLink) {
        this.hasLink = hasLink;
    }
}
