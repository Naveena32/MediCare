package com.medicare.service;

import com.medicare.dto.ContactMessageDto;
import com.medicare.entity.ContactMessage;
import com.medicare.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Transactional
    public ContactMessage saveMessage(ContactMessageDto dto) {
        ContactMessage message = new ContactMessage();
        message.setName(dto.getName().trim());
        message.setEmail(dto.getEmail().trim().toLowerCase());
        message.setSubject(dto.getSubject() != null ? dto.getSubject().trim() : "");
        message.setMessage(dto.getMessage().trim());

        return contactMessageRepository.save(message);
    }

    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }
}
