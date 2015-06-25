package com.ankurmittal.learning.storage;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;


/**
 * A dummy item representing a piece of content.
 */
public class ChatItem {
	public String id;
	public String content;
	public ArrayList<TextMessage> mMessages;
	public String email;
	private ParseUser senderUser;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setEmail() {
		ParseQuery<ParseUser> query = new ParseQuery<ParseUser>(ParseUser.class);
		query.whereEqualTo("objectId" , id);
		query.findInBackground(new FindCallback<ParseUser>() {
			
			@Override
			public void done(List<ParseUser> users, ParseException e) {
				if(e == null) {
					Log.i("chatItem", "found email");
					senderUser = users.get(0);
					setEmail(senderUser.getEmail());
				}
				
			}
		});
	}

	public ChatItem(String id, String content, ArrayList<TextMessage> messages) {
		this.id = id;
		this.content = content;
		this.mMessages = messages;
	}
	
	public ChatItem(String id, String content) {
		this.id = id;
		this.content = content;
		mMessages = new ArrayList<TextMessage>();
	}
	
	public void addMessage(TextMessage message) {
		mMessages.add(message);
	}
	public TextMessage getMessage(int i) {
		if(mMessages.size() > 0) {
			return mMessages.get(i);
		} else {
			return null;
		}
		
	}

	@Override
	public String toString() {
		return content;
	}
	
	
	public ArrayList<TextMessage> getItemMessages() {
		return mMessages;
	} 
	public String getId() {
		return id;
	}
	public void clearMessages() {
		mMessages.clear();
	}
}