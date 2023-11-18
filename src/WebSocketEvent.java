import java.util.EventObject;

public class WebSocketEvent extends EventObject {
    private final String message;

    public WebSocketEvent(Object source, String message){
        super(source);
        this.message = message;
    }

    public String getMessage(){
        return message;
    }

    interface WebSocketEventListener{
        void onWebSocketEvent(WebSocketEvent event);
    }
}
