package edu.stachsaizhartgers.broccoli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import edu.stachsaizhartgers.broccoli.config.AppConfig;
import edu.stachsaizhartgers.broccoli.consumer.ConsoleLogConsumer;
import edu.stachsaizhartgers.broccoli.consumer.FileConsumer;
import edu.stachsaizhartgers.broccoli.consumer.MongoConsumer;
import io.reactivex.flowables.ConnectableFlowable;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by Christoph Stach on 4/27/17.
 * <p>
 * Main Class
 */
public class App {
  /**
   * Main (will be invoked when the program launches)
   *
   * @param args Arguments that are passed over the console
   */
  public static void main(String[] args) {
    try {
      String config = args[0];
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      AppConfig appConfig = mapper.readValue(new File(config), AppConfig.class);
      TwitterClient twitterClient = new TwitterClient(appConfig.getApi().getTwitter());
      ConnectableFlowable<String> stream = twitterClient.listen();

      for (String consumer : appConfig.getConsumer()) {
        if (consumer.equals("ConsoleLogSubscriber")) {
          stream.subscribe(new ConsoleLogConsumer(new ObjectMapper()), App::error);
        } else if (consumer.equals("FileConsumer")) {
          stream.subscribe(new FileConsumer(appConfig.getDatabase().getFile()), App::error);
        } else if (consumer.equals("MongoConsumer")) {
          stream.subscribe(new MongoConsumer(appConfig.getDatabase().getMongo()), App::error);
        }
      }

      System.out.println();
      stream.connect();
    } catch (NullPointerException e) {
      System.out.println(e);
      System.out.println(Arrays.toString(e.getStackTrace()));
    } catch (Throwable throwable) {
      System.out.println(throwable);
    }
  }

  /**
   * Is called when a stream error occurs
   *
   * @param e The exception
   */
  private static void error(Throwable e) {
    System.out.println(e + ": " + e.getMessage());
    Stream.of(e.getStackTrace()).forEach((stackTraceElement) -> System.out.println(stackTraceElement));
  }
}
