package models;

import jakarta.persistence.*;

@Entity
@Table(name = "links")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "link_text")
    private String linkText;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_post_id")
    private ChannelPost channelPost;


    public Link() {
    }

    public Link(String linkText, ChannelPost channelPost) {
        this.linkText = linkText;
        this.channelPost = channelPost;
    }

    public ChannelPost getChannelPost() {
        return channelPost;
    }

    public void setChannelPost(ChannelPost channelPost) {
        this.channelPost = channelPost;
    }

    public int getId() {
        return id;
    }

    public void setId(int linkId) {
        this.id = linkId;
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String link) {
        this.linkText = link;
    }


}
