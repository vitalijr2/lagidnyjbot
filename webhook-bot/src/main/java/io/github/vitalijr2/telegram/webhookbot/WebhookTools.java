package io.github.vitalijr2.telegram.webhookbot;

import java.io.InputStream;
import java.util.Properties;
import java.util.ServiceLoader;
import org.slf4j.LoggerFactory;

class WebhookTools {

  private WebhookTools() {
  }

  static TelegramWebhookBot getTelegramWebhookBot(Class<? extends TelegramWebhookBot> clazz) {
    return ServiceLoader.load(clazz).findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find a TelegramWebhookBot implementation"));
  }

}
