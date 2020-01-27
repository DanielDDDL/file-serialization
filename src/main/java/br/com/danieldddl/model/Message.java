package br.com.danieldddl.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {

    private Person sender;
    private String content;
    private LocalDateTime date;

    public Message () {
        //used by jackson
    }

    public Message(Person sender, String content, LocalDateTime date) {
        this.sender = sender;
        this.content = content;
        this.date = date;
    }

    public Person getSender() {
        return sender;
    }

    public void setSender(Person sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender=" + sender +
                ", content='" + content + '\'' +
                ", date=" + date +
                '}';
    }
}
