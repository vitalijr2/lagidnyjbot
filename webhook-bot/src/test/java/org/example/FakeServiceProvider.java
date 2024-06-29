package org.example;

import io.github.vitalijr2.telegram.webhookbot.TelegramWebhookBot;

public interface FakeServiceProvider extends TelegramWebhookBot {

  String hello();

}
