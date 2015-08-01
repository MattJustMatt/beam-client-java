package pro.beam.api.example;

import pro.beam.api.BeamAPI;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.resource.chat.BeamChat;
import pro.beam.api.resource.chat.BeamChatBridge;
import pro.beam.api.resource.chat.events.EventHandler;
import pro.beam.api.resource.chat.events.IncomingMessageEvent;
import pro.beam.api.resource.chat.methods.AuthenticateMessage;
import pro.beam.api.services.impl.ChatService;
import pro.beam.api.services.impl.UsersService;

import java.util.concurrent.ExecutionException;

public class Application {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        BeamAPI beam = new BeamAPI();

        BeamUser user = beam.use(UsersService.class).login("<user>", "<password>").get();
        BeamChat c = beam.use(ChatService.class).findOne(user.channel.id).get();

        BeamChatBridge bridge = c.bridge(beam).connect().get();

        bridge.send(AuthenticateMessage.from(user.channel, user, c.authkey));
        bridge.on(IncomingMessageEvent.class, new EventHandler<IncomingMessageEvent>() {
            @Override public void onEvent(IncomingMessageEvent e) {
                System.out.println(e.data.asString());
            }
        });

        // Simulate a recoverable disconnect from the client after 1sec.
        Thread.sleep(1000);
        bridge.connectable.close();
    }
}
