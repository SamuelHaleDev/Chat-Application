import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChatServerEndpointTest {

    private ChatServerEndpoint chatServerEndpoint;

    @Mock
    private Session session;

    @Mock
    private ChatRoom chatRoom;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        chatServerEndpoint = new ChatServerEndpoint();
        when(session.getId()).thenReturn("session1");
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
    }

    @Test
    public void testOnOpen() throws IOException {
        chatServerEndpoint.onOpen(session);

        verify(session.getBasicRemote(), times(1)).sendText("Connection established!");
    }

    @Test
    public void testOnMessage_CreateChatRoomSuccess() throws IOException {
        String message = "CREATE shouldexistnow";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));

        chatServerEndpoint.onMessage(message, session);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getBasicRemote(), times(1)).sendText(argumentCaptor.capture());
        assertEquals("CREATE shouldexistnow successful", argumentCaptor.getValue());
    }

    @Test
    public void testOnMessage_CreateChatRoomFailure() throws IOException {
        String message = "CREATE room1";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
        ChatServerEndpoint.getChatRooms().put("room1", chatRoom);

        chatServerEndpoint.onMessage(message, session);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getBasicRemote(), times(1)).sendText(argumentCaptor.capture());
        assertEquals("CREATE room1 failed", argumentCaptor.getValue());
    }

    @Test
    public void testOnMessage_SubscribeChatRoomSuccess() throws IOException {
        String message = "SUBSCRIBE room1";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
        when(chatRoom.getSubscribers()).thenReturn(new HashSet<>());
        ChatServerEndpoint.getChatRooms().put("room1", chatRoom);

        chatServerEndpoint.onMessage(message, session);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getBasicRemote(), times(1)).sendText(argumentCaptor.capture());
        assertEquals("SUBSCRIBE room1 successful", argumentCaptor.getValue());
    }

    @Test
    public void testOnMessage_SubscribeChatRoomFailure() throws IOException {
        String message = "SUBSCRIBE room2";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));

        chatServerEndpoint.onMessage(message, session);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getBasicRemote(), times(1)).sendText(argumentCaptor.capture());
        assertEquals("SUBSCRIBE room2 failed", argumentCaptor.getValue());
    }

    @Test
    public void testOnMessage_UnsubscribeChatRoomSuccess() throws IOException {
        String message = "UNSUBSCRIBE room1";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
        when(chatRoom.getSubscribers()).thenReturn(new HashSet<>());
        ChatServerEndpoint.getChatRooms().put("room1", chatRoom);

        chatServerEndpoint.onMessage(message, session);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getBasicRemote(), times(1)).sendText(argumentCaptor.capture());
        assertEquals("UNSUBSCRIBE room1 successful", argumentCaptor.getValue());
    }

    @Test
    public void testOnMessage_UnsubscribeChatRoomFailure() throws IOException {
        String message = "UNSUBSCRIBE doesnotexist";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));

        chatServerEndpoint.onMessage(message, session);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getBasicRemote(), times(1)).sendText(argumentCaptor.capture());
        assertEquals("UNSUBSCRIBE doesnotexist failed", argumentCaptor.getValue());
    }

    @Test
    public void testOnMessage_GetChatRooms() throws IOException {
        String message = "GET_CHATROOMS";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
        Map<String, ChatRoom> chatRooms = new HashMap<>();
        ChatRoom room1 = new ChatRoom("room1");
        ChatRoom room2 = new ChatRoom("room2");
        chatRooms.put("room1", room1);
        chatRooms.put("room2", room2);
        chatServerEndpoint.setChatRooms(chatRooms);

        chatServerEndpoint.onMessage(message, session);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getBasicRemote(), times(1)).sendText(argumentCaptor.capture());
        assertEquals("GET_CHATROOMS room1,room2", argumentCaptor.getValue());
    }

    @Test
    public void testOnMessage_GetChatHistory() throws IOException {
        String message = "GET_HISTORY room1:username";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
        when(chatRoom.getMessageHistory("username")).thenReturn("chat history");
        ChatServerEndpoint.getChatRooms().put("room1", chatRoom);

        chatServerEndpoint.onMessage(message, session);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getBasicRemote(), times(1)).sendText(argumentCaptor.capture());
        assertEquals("GET_HISTORY chat history", argumentCaptor.getValue());
    }

    @Test
    public void testOnMessage_ProcessChatMessage() throws IOException {
        String message = "MESSAGE room1:sender:Hello";
        when(session.getBasicRemote()).thenReturn(mock(RemoteEndpoint.Basic.class));
        ChatServerEndpoint.getChatRooms().put("room1", chatRoom);

        chatServerEndpoint.onMessage(message, session);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getBasicRemote(), times(1)).sendText(argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue().startsWith("MESSAGE"));
    }

    @Test
    public void testOnClose() {
        chatServerEndpoint.onClose(session);

        assertFalse(ChatServerEndpoint.getSessions().contains(session));
    }
}
