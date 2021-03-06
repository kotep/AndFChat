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


package com.andfchat.frontend.adapter;

import java.util.List;

import net.sourcerer.android.ActionItem;
import net.sourcerer.android.QuickActionBar;
import net.sourcerer.android.QuickActionClickListner;
import net.sourcerer.android.QuickActionPreOpenListner;
import roboguice.RoboGuice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andfchat.R;
import com.andfchat.core.connection.FlistHttpClient;
import com.andfchat.core.connection.handler.PrivateMessageHandler;
import com.andfchat.core.connection.handler.VariableHandler.Variable;
import com.andfchat.core.data.CharRelation;
import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.ChatroomManager;
import com.andfchat.core.data.FCharacter;
import com.andfchat.core.data.RelationManager;
import com.andfchat.core.data.SessionData;
import com.andfchat.core.util.FlistCharComparator;
import com.andfchat.frontend.util.NameSpannable;
import com.google.inject.Inject;

public class MemberListAdapter extends ArrayAdapter<FCharacter> {

    private final static FlistCharComparator COMPARATOR = new FlistCharComparator();

    @Inject
    private ChatroomManager chatroomManager;
    @Inject
    private SessionData sessionData;
    @Inject
    private RelationManager relationManager;

    private final List<FCharacter> chars;
    private final QuickActionBar quickActionBar;
    private FCharacter activeCharacter;

    public MemberListAdapter(final Context context, List<FCharacter> chars) {
        super(context, R.layout.list_item_user, chars);

        if (chars.size() > 1) {
            this.sort(COMPARATOR);
        }

        this.chars = chars;

        RoboGuice.getInjector(context).injectMembers(this);

        quickActionBar = new QuickActionBar(context);
        quickActionBar.setOrientation(QuickActionBar.LEFT);

        // Add PM user
        ActionItem pmUser = new ActionItem(context.getResources().getString(R.string.pm_user), context.getResources().getDrawable(R.drawable.add_chat));
        pmUser.setQuickActionClickListner(new QuickActionClickListner() {

            @Override
            public void onClick(ActionItem item, View view) {
                Chatroom chatroom;
                if (chatroomManager.hasOpenPrivateConversation(activeCharacter) == false) {
                    int maxTextLength = sessionData.getIntVariable(Variable.priv_max);
                    chatroom = PrivateMessageHandler.openPrivateChat(chatroomManager, activeCharacter, maxTextLength);
                } else {
                    chatroom = chatroomManager.getPrivateChatFor(activeCharacter);
                }

                activeCharacter = null;
                chatroomManager.setActiveChat(chatroom);
                notifyDataSetChanged();
            }
        });
        quickActionBar.addActionItem(pmUser);

        final String bookmarkText = context.getResources().getString(R.string.bookmark_user);
        final String unbookmarkText = context.getResources().getString(R.string.unbookmark_user);

        // Add Bookmark user
        final ActionItem bookmark = new ActionItem(bookmarkText, context.getResources().getDrawable(R.drawable.friends));
        bookmark.setQuickActionClickListner(new QuickActionClickListner() {

            @Override
            public void onClick(ActionItem item, View view) {
                if (item.isSelected()) {
                    FlistHttpClient.removeBookmark(sessionData.getAccount(), sessionData.getTicket(), activeCharacter.getName(), null);
                    relationManager.removeFromList(CharRelation.BOOKMARKED, activeCharacter);
                }
                else {
                    FlistHttpClient.addBookmark(sessionData.getAccount(), sessionData.getTicket(), activeCharacter.getName(), null);
                    relationManager.addOnList(CharRelation.BOOKMARKED, activeCharacter);
                }
            }
        });

        bookmark.setQuickActionPreOpenListner(new QuickActionPreOpenListner() {

            @Override
            public void onPreOpen(ActionItem item) {
                if (activeCharacter.isBookmarked()) {
                    item.setSelected(true);
                    item.setTitle(unbookmarkText);
                }
                else {
                    item.setSelected(false);
                    item.setTitle(bookmarkText);
                }
            }
        });

        quickActionBar.addActionItem(bookmark);

        // Add show details
        ActionItem showDetails = new ActionItem(context.getResources().getString(R.string.show_profile), context.getResources().getDrawable(R.drawable.info));
        showDetails.setQuickActionClickListner(new QuickActionClickListner() {

            @Override
            public void onClick(ActionItem item, View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.f-list.net/c/" + activeCharacter.getName()));
                getContext().startActivity(browserIntent);
            }
        });
        quickActionBar.addActionItem(showDetails);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final FCharacter character = this.getItem(position);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.list_item_user, null);
        // Set username
        TextView textView = (TextView)rowView.findViewById(R.id.itemText);
        textView.setText(new NameSpannable(character, null, getContext().getResources()));

        // Set icon
        ImageView itemIcon = (ImageView)rowView.findViewById(R.id.itemIcon);

        switch (character.getStatus()) {
            case ONLINE:
                itemIcon.setBackgroundResource(R.drawable.icon_blue);
                break;
            case BUSY:
                itemIcon.setBackgroundResource(R.drawable.icon_orange);
                break;
            case DND:
                itemIcon.setBackgroundResource(R.drawable.icon_red);
                break;
            case LOOKING:
                itemIcon.setBackgroundResource(R.drawable.icon_green);
                break;
            case AWAY:
                itemIcon.setBackgroundResource(R.drawable.icon_grey);
                break;
            default:
                itemIcon.setBackgroundResource(R.drawable.icon_blue);
        }

        View userLabel = rowView.findViewById(R.id.userlabel);

        userLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sessionData.getCharacterName().equals(character.getName())) {
                    activeCharacter = character;
                    quickActionBar.show(v);
                }
            }
        });

        return rowView;
    }

    @Override
    public void add(FCharacter flistChar) {
        if (flistChar == null) {
            return;
        }

        boolean added = false;
        for (int i = 0; i < chars.size(); i++) {
            if (COMPARATOR.compare(chars.get(i), flistChar) == 0) {
                chars.add(i, flistChar);
                added = true;
                break;
            }
        }

        if (!added) {
            chars.add(flistChar);
        }

        notifyDataSetChanged();
    }

}
