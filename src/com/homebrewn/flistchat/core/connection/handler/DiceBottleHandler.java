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


package com.homebrewn.flistchat.core.connection.handler;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.homebrewn.flistchat.core.connection.FeedbackListner;
import com.homebrewn.flistchat.core.connection.ServerToken;
import com.homebrewn.flistchat.core.data.ChatEntry;
import com.homebrewn.flistchat.core.data.ChatEntry.ChatEntryType;
import com.homebrewn.flistchat.core.data.Chatroom;
import com.homebrewn.flistchat.core.data.FlistChar;

/**
 * Adds Dice and Bottle messages to channels.
 * @author AndFChat
 */
public class DiceBottleHandler extends TokenHandler {

    @Override
    public void incomingMessage(ServerToken token, String msg, List<FeedbackListner> feedbackListner) throws JSONException {
        if (token == ServerToken.RLL) {
            JSONObject json = new JSONObject(msg);
            String channelId = json.getString("channel");
            String message = json.getString("message");
            String character = json.getString("character");

            FlistChar owner = characterManager.findCharacter(character);

            Chatroom chatroom = chatroomManager.getChatroom(channelId);
            if (chatroom != null) {
                // Remove the first name, is already displayed by the ChatEntry.
                message = message.substring(message.indexOf("[/b]") + "[/b]".length());
                ChatEntry chatEntry = new ChatEntry(message, owner, new Date(), ChatEntryType.NOTATION_DICE);
                chatroom.addMessage(chatEntry);
            }
        }
    }

    @Override
    public ServerToken[] getAcceptableTokens() {
        return new ServerToken[] {ServerToken.RLL};
    }

}