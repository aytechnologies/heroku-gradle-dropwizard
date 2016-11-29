package ca.aytech.examples.hibernate;

import javax.persistence.*;

/**
 * Created by amin on 11/13/15.
 */
@Entity
@Table(name="memos")
public class Memo {
    @Id
    @SequenceGenerator(name = "memoSeq", sequenceName="memo_id_seq", allocationSize=1)
    @GeneratedValue(generator="memoSeq", strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "status", nullable = false)
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public enum STATUS{
        Processing, Processed
    }
}
