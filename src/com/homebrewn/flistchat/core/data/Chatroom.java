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


package com.homebrewn.flistchat.core.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.text.Spannable;
import android.util.Log;

import com.homebrewn.flistchat.core.data.ChatEntry.ChatEntryType;

public class Chatroom {

    public enum ChatroomType {
        PUBLIC_CHANNEL(true, true, 80),
        PRIVATE_CHANNEL(true, true, 60),
        PRIVATE_CHAT(true, false, 40),
        SYSTEM(false, false, 100);

        public final boolean closeable;
        public final boolean showUserList;
        public final int maxEntries;

        ChatroomType(boolean closeable, boolean showUserList, int maxEntries) {
            this.closeable = closeable;
            this.showUserList = showUserList;
            this.maxEntries = maxEntries;
        }
    }

    private final Channel channel;
    private final ChatroomType chatroomType;

    private List<ChatEntry> chatMessages;
    private final List<FlistChar> characters = new ArrayList<FlistChar>();

    private List<FlistChar> joined = new ArrayList<FlistChar>();
    private List<FlistChar> left = new ArrayList<FlistChar>();

    private Spannable description;

    private boolean hasNewMessage = false;

    public Chatroom(Channel channel, ChatroomType type) {
        this.channel = channel;
        chatroomType = type;

        this.chatMessages = new ArrayList<ChatEntry>(chatroomType.maxEntries);
    }

    public Chatroom(Channel channel, FlistChar character) {
        chatroomType = ChatroomType.PRIVATE_CHAT;

        this.channel = channel;
        this.characters.add(character);
        this.chatMessages = new ArrayList<ChatEntry>(chatroomType.maxEntries);
    }

    public void setDescription(Spannable description) {
        this.description = description;
    }

    public Spannable getDescription() {
        return description;
    }

    public String getName() {
        return channel.getChannelName();
    }

    public String getId() {
        return channel.getChannelId();
    }

    public int getMaxiumEntries() {
        return chatroomType.maxEntries;
    }

    public boolean isPrivateChat() {
        return chatroomType == ChatroomType.PRIVATE_CHAT;
    }

    public boolean closeable() {
        return chatroomType.closeable;
    }

    public boolean showUserList() {
        return chatroomType.showUserList;
    }

    public boolean hasNewMessage() {
        return hasNewMessage;
    }

    public void setHasNewMessage(boolean value) {
        hasNewMessage = value;
    }

    public List<ChatEntry> getLastMessages(int amount) {
        List<ChatEntry> lastMessages = new ArrayList<ChatEntry>(amount);

        int startPosition = 0;
        if (chatMessages.size() > amount) {
            startPosition = chatMessages.size() - amount;
        }

        for (int i = startPosition; i < chatMessages.size(); i++) {
            lastMessages.add(chatMessages.get(i));
        }

        return lastMessages;
    }

    public boolean chatChangedSince(Date date) {
        return chatMessages.get(chatMessages.size() - 1).getDate().after(date);
    }

    public void addMessage(ChatEntry entry) {
        if (chatMessages.size() < chatroomType.maxEntries) {
            chatMessages.add(entry);
        } else {
            chatMessages.remove(0);
            chatMessages.add(entry);
        }
    }

    public void addCharacter(FlistChar flistChar) {
        if (!characters.contains(flistChar)) {
            characters.add(flistChar);
            joined.add(flistChar);
            left.remove(flistChar);
        }
    }

    public void removeCharacter(FlistChar flistChar) {
        if (characters.remove(flistChar) == true) {
            joined.remove(flistChar);
            left.add(flistChar);
        }
    }

    public List<FlistChar> getCharacters() {
        joined.clear();
        left.clear();
        return characters;
    }

    public void addMessage(String message, FlistChar character, Date date) {
        Log.d("ChatLog", "NEW MESSAGE: " + message + " FROM " + character.getName());
        this.addMessage(new ChatEntry(message, character, date, ChatEntryType.MESSAGE));
    }

    public List<ChatEntry> getChatEntries() {
        return chatMessages;
    }

    public List<ChatEntry> getChatEntriesSince(long time) {
        List<ChatEntry> messages = new ArrayList<ChatEntry>();

        for (int i = chatMessages.size() - 1; i >= 0; i--) {
            if (chatMessages.get(i).getDate().getTime() > time) {
                messages.add(chatMessages.get(i));
            } else {
                break;
            }
        }

        return messages;
    }

    public List<FlistChar> getLeftChars() {
        List<FlistChar> leftChars = left;
        left = new ArrayList<FlistChar>();
        return leftChars;
    }

    public List<FlistChar> getJoinedChars() {
        List<FlistChar> joinedChars = joined;
        joined = new ArrayList<FlistChar>();
        return joinedChars;
    }

    public FlistChar getRecipient() {
        return characters.get(0);
    }

    public boolean isSystemChat() {
        return this.chatroomType == ChatroomType.SYSTEM;
    }

    public List<ChatEntry> getChatHistory() {
        return chatMessages;
    }

    public void setChatHistory(List<ChatEntry> chatMessages) {
        this.chatMessages = chatMessages;
    }
}