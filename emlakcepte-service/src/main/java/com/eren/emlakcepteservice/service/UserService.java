package com.eren.emlakcepteservice.service;

import com.eren.emlakcepteservice.client.PaymentServiceClient;
import com.eren.emlakcepteservice.client.model.Payment;
import com.eren.emlakcepteservice.client.model.enums.PaymentStatus;
import com.eren.emlakcepteservice.converter.PublicationRightConverter;
import com.eren.emlakcepteservice.converter.UserConverter;
import com.eren.emlakcepteservice.entity.PublicationRight;
import com.eren.emlakcepteservice.entity.Search;
import com.eren.emlakcepteservice.entity.User;
import com.eren.emlakcepteservice.repository.PublicationRepository;
import com.eren.emlakcepteservice.repository.UserRepository;
import com.eren.emlakcepteservice.request.PublicationRightRequest;
import com.eren.emlakcepteservice.request.UserRequest;
import com.eren.emlakcepteservice.request.UserUpdateRequest;
import com.eren.emlakcepteservice.response.PublicationRightResponse;
import com.eren.emlakcepteservice.response.UserResponse;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private PublicationRightConverter publicationRightConverter;

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    private DirectExchange exchange;

    // Payment Queue Parameters Read From application.properties
    @Value("${rabbitmq.routingKey}")
    private String routingKey;
    @Value("${rabbitmq.queue}")
    private String queueName;

    private final Logger logger = Logger.getLogger(UserService.class.getName());

    // Create User
    public UserResponse createUser(UserRequest userRequest) {
        User savedUser = userRepository.save(userConverter.convert(userRequest));
        logger.log(Level.INFO, "[createUser] - user created: {0}", savedUser.getId());
        return userConverter.convert(savedUser);
    }

    // Get All Users
    public List<UserResponse> getAll() {
        return userConverter.convert(userRepository.findAll());
    }

    // Get UserResponse By Id
    public UserResponse getUserResponseById(Integer userId) {
        User foundUser = getById(userId);
        return userConverter.convert(foundUser);
    }

    // Get User By Id
    public User getById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with this id: " + userId));
    }
    // Get User By Email
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with this email: " + email));
    }

    // Get UserResponse By Email
    public UserResponse getUserResponseByEmail(String email) {
        User foundUser = getByEmail(email);
        return userConverter.convert(foundUser);
    }

    // Update and return updated UserResponse
    public UserResponse update(UserUpdateRequest userUpdateRequest, Integer userId) {
        User foundUser = getById(userId);
        User updatedUser = updateUser(foundUser, userUpdateRequest);
        return userConverter.convert(updatedUser);
    }

    // Update User
    public User updateUser(User user, UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest.getName() != null && userUpdateRequest.getName().length() > 0) {
            user.setName(userUpdateRequest.getName());
        }
        if (userUpdateRequest.getEmail() != null && userUpdateRequest.getEmail().length() > 0) {
            user.setEmail(userUpdateRequest.getEmail());
        }
        if (userUpdateRequest.getPassword() != null && userUpdateRequest.getPassword().length() > 0) {
            user.setPassword(userUpdateRequest.getPassword());
        }
        userRepository.save(user);
        return user;
    }

    // Get User's Search History
    public List<Search> getUserSearchHistory(Integer userId) {
        return getById(userId).getSearchList();
    }

    // Buy Publication Rights
    public void buyPublication(PublicationRightRequest publicationRightRequest) {
        User user = getById(publicationRightRequest.getUserId());
        // Payment
        Payment payment = paymentServiceClient.buyPublicationRights(new Payment(publicationRightRequest.getUserId(), PaymentStatus.UNSUCCESSFUL));
        if (PaymentStatus.UNSUCCESSFUL.equals(payment.getPaymentStatus())){
            logger.log(Level.WARNING, "[buyPublication] - payment unsuccessful: {0}", payment.getPaymentStatus());
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Unsuccessful payment");
        }
        rabbitTemplate.convertAndSend(exchange.getName(), routingKey, publicationRightRequest);
    }

    // Asynchronous publicationRight Buy Process
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void giveBoughtPublicationRights(PublicationRightRequest publicationRightRequest) {
        User user = getById(publicationRightRequest.getUserId());
        while (publicationRightRequest.getQuantity() > 0) {
            PublicationRight newPublicationRight = publicationRightConverter.convert(publicationRightRequest, user);
            publicationRepository.save(newPublicationRight);
            publicationRightRequest.setQuantity(publicationRightRequest.getQuantity() - 1);
        }
        userRepository.save(user);
    }

    // Get User's Publication Rights
    public List<PublicationRightResponse> getPublicationRights(Integer userId) {
        User user = getById(userId);
        List<PublicationRight> publicationRightList = user.getPublicationRightList();
        return publicationRightConverter.convert(publicationRightList);
    }
}
