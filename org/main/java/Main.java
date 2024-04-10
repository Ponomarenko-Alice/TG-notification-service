import models.ChannelPost;
import models.Link;
import service.ChannelPostService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        ChannelPostService channelPostService = new ChannelPostService();
        ChannelPost post = new ChannelPost( (int) (Math.random()*1000), false);
        List<Link> list = post.getLinks() == null ? new ArrayList<>() : post.getLinks();
        Link newLink = new Link("https://some/link/raf", post);
        list.add(newLink);
        post.setLinks(list);
        post.setHasLink(true);
        channelPostService.save(post);

    }
}
