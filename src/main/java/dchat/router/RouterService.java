package dchat.router;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class RouterService {

    private static Map<String, AtomicReference<Channel>> addressChannelMap = new ConcurrentHashMap<>();
    private static Map<String, String> addressKafkaTopicMap = new ConcurrentHashMap<>();


}
