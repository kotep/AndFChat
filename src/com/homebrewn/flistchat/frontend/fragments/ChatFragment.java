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


package com.homebrewn.flistchat.frontend.fragments;

import java.util.ArrayList;
import java.util.List;

import roboguice.event.Observes;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.inject.Inject;
import com.homebrewn.flistchat.R;
import com.homebrewn.flistchat.core.connection.FlistWebSocketConnection;
import com.homebrewn.flistchat.core.data.ChatEntry;
import com.homebrewn.flistchat.core.data.Chatroom;
import com.homebrewn.flistchat.core.data.ChatroomManager;
import com.homebrewn.flistchat.core.util.Console;
import com.homebrewn.flistchat.frontend.adapter.ChatEntryListAdapter;

public class ChatFragment extends RoboFragment {

    @Inject
    protected ChatroomManager chatroomManager;
    @Inject
    protected FlistWebSocketConnection connection;
    @Inject
    protected Console commands;

    @InjectView(R.id.chat)
    private ListView chatListView;
    @InjectView(R.id.chatMessage)
    private EditText inputText;
    @InjectView(R.id.sendButton)
    private Button sendButton;

    private long lastMessage = System.currentTimeMillis();

    private ChatEntryListAdapter chatListData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_chat, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        { // Chat window setup
            chatListData = new ChatEntryListAdapter(getActivity(), new ArrayList<ChatEntry>());
            chatListView.setAdapter(chatListData);
            // Autoscroll to bottom
            chatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            // Stack chat from bottom to top
            chatListView.setStackFromBottom(true);
        }

        { // Setup send button
            sendButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });
        }
    }


    private void sendMessage() {
        // Ignore empty messages
        if (inputText.getText().toString().length() == 0 ) {
            return;
        }
        // Text command like /help / open /close shouldn't be send to the server.
        if (commands.checkForCommands(inputText.getText().toString())) {
            // Reset input
            inputText.setText("");
            return;
        }
        else if ((System.currentTimeMillis() - lastMessage > 2000)) {

            Chatroom activeChat = chatroomManager.getActiveChat();

            if (activeChat.isPrivateChat()) {
                connection.sendPrivatMessage(activeChat.getRecipient().getName(), inputText.getText().toString());
            } else {
                connection.sendMessageToChannel(activeChat, inputText.getText().toString());
            }

            lastMessage = System.currentTimeMillis();
        }
        else {
            //TODO: Show error message (input to fast).
        }

        // Reset input
        inputText.setText("");
    }

    protected void setActiveChat(@Observes Chatroom chatroom) {
        Ln.v("Active chat set event is called!");
        List<ChatEntry> messages = new ArrayList<ChatEntry>();

        if (chatroom != null) {
            messages = chatroom.getLastMessages(chatroom.getMaxiumEntries());
        }
        // Input text is disabled on System chats
        inputText.setEnabled(!chatroom.isSystemChat());
        // Set messages
        chatListData.clear();
        chatListData.addAll(messages);
    }

    public void refreshChat() {
        if (chatroomManager.getActiveChat() != null) {
            List<ChatEntry> entries = chatroomManager.getActiveChat().getChatEntriesSince(chatListData.getLastMessageTime());

            for (final ChatEntry entry : entries) {
                chatListData.add(entry);
            }
        }
    }
}
