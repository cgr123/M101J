package course;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class BlogPostDAO {
    public static final String PERMALINK_FIELD = "permalink";
    public static final String TITLE_FIELD = "title";
    public static final String AUTHOR_FIELD = "author";
    public static final String BODY_FIELD = "body";
    public static final String TAGS_FIELD = "tags";
    public static final String DATE_FIELD = "date";
    public static final String COMMENTS_FIELD = "comments";
    public static final String EMAIL_FIELD = "email";

    MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    // Return a single post corresponding to a permalink
    public Document findByPermalink(String permalink) {

        // todo  XXX
        Document findByPermalink = getFindByPermalinkDocument(permalink);
        return postsCollection.find(findByPermalink).first();
    }

    private Document getFindByPermalinkDocument(final String permalink) {
        return new Document(PERMALINK_FIELD, permalink);
    }

    // Return a list of posts in descending order. Limit determines
    // how many posts are returned.
    public List<Document> findByDateDescending(int limit) {

        // todo,  XXX
        // Return a list of Documents, each one a post from the posts collection
        return postsCollection.find().limit(limit).sort(Sorts.descending(DATE_FIELD)).into(new LinkedList<Document>());
    }

    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();
        permalink = permalink + (new Date()).getTime();

        // todo XXX
        // Remember that a valid post has the following keys:
        // author, body, permalink, tags, comments, date
        //
        // A few hints:
        // - Don't forget to create an empty list of comments
        // - for the value of the date key, today's datetime is fine.
        // - tags are already in list form that implements suitable interface.
        // - we created the permalink for you above.

        // Build the post object and insert it
        Document post = new Document(AUTHOR_FIELD, username).append(TITLE_FIELD, title)
                                                            .append(BODY_FIELD, body)
                                                            .append(PERMALINK_FIELD, permalink)
                                                            .append(TAGS_FIELD, tags)
                                                            .append(DATE_FIELD, new Date())
                                                            .append(COMMENTS_FIELD, new ArrayList<Document>());
        postsCollection.insertOne(post);
        return permalink;
    }

    // Append a comment to a blog post
    public void addPostComment(final String name, final String email, final String body,
                               final String permalink) {

        // todo  XXX
        // Hints:
        // - email is optional and may come in NULL. Check for that.
        // - best solution uses an update command to the database and a suitable
        //   operator to append the comment on to any existing list of comments

        Document postComment = new Document(AUTHOR_FIELD, name).append(BODY_FIELD, body);
        if (email != null && !email.isEmpty()) {
            postComment.append(EMAIL_FIELD, email);
        }

        Document pushCommentsOntoArray = new Document("$push", new Document(COMMENTS_FIELD, postComment));
        postsCollection.updateOne(getFindByPermalinkDocument(permalink), pushCommentsOntoArray);
    }
}
