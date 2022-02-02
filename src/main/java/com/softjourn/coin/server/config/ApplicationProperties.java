package com.softjourn.coin.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("application")
public class ApplicationProperties {

  private Auth auth;
  private Fabric fabric;
  private Organisation organisation;
  private Treasury treasury;
  private Image image;

  @Getter
  @Setter
  public static class Auth {

    private Client client;
    private Server server;
    private String publicKeyFile;

    @Getter
    @Setter
    public static class Client {

      private String id;
      private String secret;
    }

    @Getter
    @Setter
    public static class Server {

      private String url;
    }
  }

  @Getter
  @Setter
  public static class Fabric {

    private String clientUrl;
  }

  @Getter
  @Setter
  public static class Organisation {

    private String name;
  }

  @Getter
  @Setter
  public static class Treasury {

    private String account;
  }

  @Getter
  @Setter
  public static class Image {

    private Storage storage;
    private Account account;

    @Getter
    @Setter
    public static class Storage {

      private String path;
    }

    @Getter
    @Setter
    public static class Account {

      private String defaultUrl;
    }
  }
}
