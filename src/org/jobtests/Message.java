package org.jobtests;

public class Message {
    private Long ts;
    private String name;
    private String text;

    public Message(String name, String text) {
        this.ts = System.currentTimeMillis();
        this.name = name;
        this.text = text;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "ts=" + ts +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
