package com.serfer.notify.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serfer.notify.entity.AlreadySent;
import com.serfer.notify.tranfer.Meeting;
import com.serfer.notify.entity.TelegramChatId;
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
import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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


    @Value("${app.meeting.service.uri:https://localhost:44306/api/meetings/all}")
    String meetingServiceUri;

    @Value("${jwt.secret:EWRsdfwerEWRWrgfdPklewrIOP34234_432hJKHJK534##BmB_}")
    String accessSecret;

    public SchedulerService(EmailService emailSenderService, ChatIdRepository chatIdRepository, AlreadySentRepository alreadySentRepository, TelegramService bot) throws NoSuchAlgorithmException, KeyManagementException {
        this.emailSenderService = emailSenderService;
        this.chatIdRepository = chatIdRepository;
        this.alreadySentRepository = alreadySentRepository;
        this.bot = bot;

        mapper = new ObjectMapper();

        this.httpClient = getUnsafeOkHttpClient();

        formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
    }

    @PostConstruct
    private void init() {
        token = createAccessToken();
    }


    @Scheduled(fixedDelay = 150000, initialDelay = 5000)
    public void getNewEvent() throws Exception {

        log.info("Get information about events");

        Request request = new Request.Builder()
                .url(meetingServiceUri)
                .addHeader("Authorization", token)
                .get()
                .build();

        Response response = httpClient.newCall(request).execute();
        log.info("Response " + response);

        if (response.isSuccessful()) {
            final String json = response.body().string();

            List<Meeting> meetings = mapper.readValue(json, new TypeReference<List<Meeting>>() {
            });
            if (meetings.size() > 0) {

                meetings.forEach(meeting -> {

                    Optional<AlreadySent> byOwnerAndDateTime = alreadySentRepository.findByOwnerAndDateTime(meeting.owner.userEmail, meeting.startTime);
                    if (!byOwnerAndDateTime.isPresent()) {

                        log.info("There is a meeting to remind {}", meeting);
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

                                        bot.sendMsg(bot.createMessage(telegramChatId.getChatId(), message));
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
        String smile = "\uD83D\uDD61";
        return new StringBuilder()
                .append(smile)
                .append("You have a scheduled meeting with ")
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
        String accessToken = "Bearer " + Jwts.builder()//
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)//
                .setIssuedAt(currentTime)//
                .setExpiration(new Date(System.currentTimeMillis() + 360000 * 1000))//
                .setNotBefore(currentTime)
                .signWith(SignatureAlgorithm.HS256, accessSecret = Base64.getEncoder().encodeToString(accessSecret.getBytes()))//
                .compact();

        log.info("Generated access token [{}]", accessToken);
        return accessToken;
    }

    public static OkHttpClient getUnsafeOkHttpClient() {
        try {

            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true; // Не перевіряємо ім'я хоста
                }
            });

            return builder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}
