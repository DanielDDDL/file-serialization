package br.com.danieldddl.files;

import br.com.danieldddl.model.Message;

import java.util.List;

public interface MessagePersist {

    void append (Message message);
    List<Message> retrieveLastMessages (Integer numberOfMessages);
}
