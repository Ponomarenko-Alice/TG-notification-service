package models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "channel_post")
public class ChannelPost {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "has_link")
    private boolean hasLink;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Link> links = new ArrayList<>();
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
