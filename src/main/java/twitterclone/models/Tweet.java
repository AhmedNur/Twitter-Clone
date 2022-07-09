package twitterclone.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Getter @Setter
public class Tweet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long parentId;
    private String tag;
    private String displayName;
    @Column(length = 280)
    private String content;
    private Long dateCreated;
    private Integer likes;
    private Integer retweets;
    @ElementCollection
    private Collection<Long> replies;
    private Integer numOfReplies;

    public String toString(){
        return content;
    }
}
