package com.serfer.notify.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serfer.notify.model.AlreadySent;
import com.serfer.notify.model.Meeting;
import com.serfer.notify.model.TelegramChatId;
import com.serfer.notify.repository.AlreadySentRepository;
import com.serfer.notify.repository.ChatIdRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
@Component
public class SchedulerService {

    final ChatIdRepository chatIdRepository;

    final AlreadySentRepository alreadySentRepository;

    final TelegramService bot;

    final EmailService emailSenderService;

    final ObjectMapper mapper;

    final OkHttpClient httpClient;

    private String token;

    DateTimeFormatter formatter;

    private static final String DATE_FORMATTER = "yyyy-MM-dd HH:mm"; //2020-06-03T16:10:43.176


    @Value("${app.meeting.service.uri:http://localhost:63798/api/meetings/all}")
    String meetingServiceUri;

    @Value("${jwt.secret:EWRsdfwerEWRWrgfdPklewrIOP34234_432hJKHJK534##BmB_}")
    String accessSecret;

    public SchedulerService(EmailService emailSenderService, ChatIdRepository chatIdRepository, AlreadySentRepository alreadySentRepository, TelegramService bot, OkHttpClient okHttpClient) throws NoSuchAlgorithmException, KeyManagementException {
        this.emailSenderService = emailSenderService;
        this.chatIdRepository = chatIdRepository;
        this.alreadySentRepository = alreadySentRepository;
        this.bot = bot;

        mapper = new ObjectMapper();

        this.httpClient = okHttpClient;
        formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
    }

    @PostConstruct
    private void init() {
        token = createAccessToken();
        System.out.println("Token " + token);
    }


    @Scheduled(fixedDelay = 150000, initialDelay = 5000)

    public void getNewEvent() throws Exception {

        log.debug("Get information about events");

        Request request = new Request.Builder()
                .url(meetingServiceUri)
                .addHeader("Authorization", token)
                .get()
                .build();

        Response response = httpClient.newCall(request).execute();
        log.debug("Response " + response);

        if (response.isSuccessful()) {
            final String json = response.body().string();

            List<Meeting> meetings = mapper.readValue(json, new TypeReference<List<Meeting>>() {
            });
            if (meetings.size() > 0) {

                meetings.forEach(meeting -> {

                    Optional<AlreadySent> byOwnerAndDateTime = alreadySentRepository.findByOwnerAndDateTime(meeting.owner.userEmail, meeting.startTime);
                    if (!byOwnerAndDateTime.isPresent()) {

                        log.info("There is a meeting to remind{}", meeting);
                        alreadySentRepository.save(AlreadySent.builder().owner(meeting.owner.userEmail).dateTime(meeting.startTime).build());

                        if (meeting.startTime.isBefore(LocalDateTime.now().plusMinutes(60))) {
                            log.info("The event will take place in less than an hour. Number of participants {}", meeting.participants.size());
                            meeting.participants.forEach(participant -> {

                                try {

                                    String message = getNotifyMassage(meeting);

                                    //send notify in email
                                    emailSenderService.sendEmail(message, meeting.title, participant.userEmail);

                                    //send notify in telegram
                                    Optional<TelegramChatId> byEmail = chatIdRepository.findByEmail(participant.userEmail);
                                    if (byEmail.isPresent()) {
                                        log.info("An additional reminder will be sent to the user {}", participant.userEmail);
                                        TelegramChatId telegramChatId = byEmail.get();

                                        bot.sendMsg(bot.createMessage(telegramChatId.getChapId(), message));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } else {
                        log.info("Participants have already been warned about this event");
                    }
                });

            } else {
                log.info("Інформації для відправки не виявнено");
            }
        }

    }

    @NotNull
    private String getNotifyMassage(Meeting meeting) {
        String meetingTime = meeting.startTime.format(formatter);
        String smile = "\\xF0\\x9F\\x95\\xA1";
        return new StringBuilder()
                .append(smile)
                .append(" You have a scheduled meeting with ")
                .append(meeting.owner.userFirstName).append(" ")
                .append(meeting.owner.userLastName).append(" at  ")
                .append(meetingTime).append(". ")
                .append("The topic of the meeting ")
                .append(meeting.title)
                .append(". Detail(")
                .append(meeting.description).append(")").toString();
    }

    public String createAccessToken() {

        Claims claims = Jwts.claims().setSubject("message-service");
        claims.put("role", "admin");

        Date currentTime = new Date();
        return "Bearer " + Jwts.builder()//
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)//
                .setIssuedAt(currentTime)//
                .setExpiration(new Date(System.currentTimeMillis() + 360000 * 1000))//
                .setNotBefore(currentTime)
                .signWith(SignatureAlgorithm.HS256, accessSecret = Base64.getEncoder().encodeToString(accessSecret.getBytes()))//
                .compact();
    }




}
