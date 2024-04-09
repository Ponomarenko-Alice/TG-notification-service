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


    public Link() {
    }

    public Link(String linkText) {
        this.linkText = linkText;
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
