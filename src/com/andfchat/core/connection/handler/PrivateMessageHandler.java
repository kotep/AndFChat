/*******************************************************************************
 *     This file is part of AndFChat.
 *
 *     AndFChat is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AndFChat is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AndFChat.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/


package com.andfchat.core.connection.handler;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import roboguice.util.Ln;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Vibrator;

import com.andfchat.core.connection.FeedbackListner;
import com.andfchat.core.connection.ServerToken;
import com.andfchat.core.connection.handler.VariableHandler.Variable;
import com.andfchat.core.data.Channel;
import com.andfchat.core.data.ChatEntry;
import com.andfchat.core.data.ChatEntryType;
import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.Chatroom.ChatroomType;
import com.andfchat.core.data.ChatroomManager;
import com.andfchat.core.data.FCharacter;
import com.andfchat.frontend.application.AndFChatApplication;
import com.andfchat.frontend.events.ChatroomEventListner.ChatroomEventType;
import com.google.inject.Inject;

/**
 * Handles private messages send to user.
 * @author AndFChat
 */
public class PrivateMessageHandler extends TokenHandler {

    public final static String PRIVATE_MESSAGE_TOKEN = "PRIV:::";

    @Inject
    protected Vibrator vibrator;
    @Inject
    protected NotificationManager notificationManager;

    private final static int VIBRATING_TIME = 300;

    @Override
    public void incomingMessage(ServerToken token, String msg, List<FeedbackListner> feedbackListner) throws JSONException {
        JSONObject jsonObject = new JSONObject(msg);

        String character = jsonObject.getString("character");
        String message = jsonObject.getString("message");

        Chatroom chatroom = chatroomManager.getChatroom(PRIVATE_MESSAGE_TOKEN + character);
        if (chatroom == null) {
            int maxTextLength = sessionData.getIntVariable(Variable.priv_max);
            chatroom = openPrivateChat(chatroomManager, characterManager.findCharacter(character), maxTextLength);
            eventManager.fire(chatroom, ChatroomEventType.NEW);
        }

        // If vibration is allowed, do it on new messages!
        if (sessionData.getSessionSettings().vibrationFeedback()) {
            // Vibrate if the active channel is not the same as the "messaged" one or the app is not visible and the chatroom isn't already set to "hasNewMessage".
            if ((chatroomManager.getActiveChat().equals(chatroom) == false || sessionData.isVisible() == false) && chatroom.hasNewMessage() == false) {
                Ln.d("New Message Vibration on!");
                vibrator.vibrate(VIBRATING_TIME);
            }
        }

        if (sessionData.getSessionSettings().ledFeedback()) {
            if (sessionData.isVisible() == false) {
                Ln.d("Set led active!");
                Notification notif = new Notification();
                notif.ledARGB = 0xFFffffff;
                notif.flags = Notification.FLAG_SHOW_LIGHTS;
                notif.ledOnMS = 200;
                notif.ledOffMS = 200;
                notificationManager.notify(AndFChatApplication.LED_NOTIFICATION_ID, notif);
            }
        }

        ChatEntry entry = new ChatEntry(message, characterManager.findCharacter(character), ChatEntryType.MESSAGE);
        chatroomManager.addMessage(chatroom, entry);
    }

    @Override
    public ServerToken[] getAcceptableTokens() {
        return new ServerToken[]{ServerToken.PRI};
    }

    public static Chatroom openPrivateChat(ChatroomManager chatroomManager, FCharacter character, int maxTextLength) {
        String channelname = PrivateMessageHandler.PRIVATE_MESSAGE_TOKEN + character.getName();

        Chatroom chatroom = new Chatroom(new Channel(channelname, character.getName(), ChatroomType.PRIVATE_CHAT), character, maxTextLength);
        chatroomManager.addChatroom(chatroom);

        return chatroom;
    }
}
