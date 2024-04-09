import models.ChannelPost;
import models.Link;
import service.ChannelPostService;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        ChannelPostService channelPostService = new ChannelPostService();
        ChannelPost post = new ChannelPost( (int) (Math.random()*1000), false);
        post.getLinks().add(new Link("https://some/link1"));
        post.setHasLink(true);
        channelPostService.save(post);
    }
}
