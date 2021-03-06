package com.ankurmittal.learning.storage;

import java.util.ArrayList;
import java.util.HashMap;

import com.ankurmittal.learning.util.CustomTarget;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ChatItemDataSource {
	private SQLiteDatabase mDatabase; // The actual DB!
	private ChatItemHelper mChatItemHelper; // Helper class for creating
	private FriendsDataSource frndsDataSource;
	// and
	// opening the DB
	private Context mContext;

	public ChatItemDataSource(Context context) {
		mContext = context;
		mChatItemHelper = new ChatItemHelper(mContext);
	}

	/*
	 * Open the db. Will create if it doesn't exist
	 */
	public void open() throws SQLException {
		mDatabase = mChatItemHelper.getWritableDatabase();

		// Log.d("TEXT Databse check", "database opened");
	}

	/*
	 * We always need to close our db connections
	 */
	public void close() {
		if (mDatabase != null) {
			mDatabase.close();
		}

		// Log.d("TEXT Databse check", "database closed");
	}

	// INSERT
	public void insert(ChatItem chatItem) {

		if (!mDatabase.isOpen()) {
			open();
		}

		Cursor cursor = isChatItemNew(chatItem);
		if (cursor.getCount() == 0) {
			// we got a new chatItem since cursor cud not find it
			Log.d("DATA SOURCE", "INSERTING NEW ChatItem" + chatItem);
			String imgUrl;
			String lastMessage;
			frndsDataSource = new FriendsDataSource(mContext);
			if (frndsDataSource.getImageUrlFromId(mContext, chatItem.getId()) != null) {
				imgUrl = frndsDataSource.getImageUrlFromId(mContext,
						chatItem.getId());
			} else {
				imgUrl = "null";
			}

			// sendNotification(friend);

			mDatabase.beginTransaction();
			try {
				ContentValues values = new ContentValues();
				values.put(ChatItemHelper.COLUMN_SENDER_ID, chatItem.getId());
				values.put(ChatItemHelper.COLUMN_SENDER_NAME,
						chatItem.getContent());
				// values.put(ChatItemHelper.COLUMN_SENDER_EMAIL,
				// null);
				values.put(ChatItemHelper.COLUMN_SENDER_IMG, imgUrl);
				// values.put(ChatItemHelper.COLUMN_SENDER_CONTACT,
				// chatItem.getMessageId());
				if (chatItem.getLastMessage() != null) {
					values.put(ChatItemHelper.COLUMN_LAST_MESSAGE, chatItem
							.getLastMessage().getMessage());

					Log.d("inseting", chatItem.getContent());

					values.put(ChatItemHelper.COLUMN_CREATED_AT, chatItem
							.getLastMessage().getCreatedAtString());
				}

				// values.put(ChatItemHelper.COLUMN_IS_SENT,
				// chatItem.isSent() + "");

				// friend.setViewed(false);
				mDatabase.insert(ChatItemHelper.TABLE_CHAT_ITEMS, null, values);
				mDatabase.setTransactionSuccessful();
				Log.d("INSERTED", "Chat Item ADDED");
			} finally {
				mDatabase.endTransaction();
			}
		} else {

			Log.d(" NOT INSERTED", "ROW NOT ADDED");
		}

	}

	public Cursor isChatItemNew(ChatItem chatItem) {

		String whereClause = ChatItemHelper.COLUMN_SENDER_ID + " = ?";

		if (mDatabase.isOpen()) {
			open();
		}
		Cursor cursor = mDatabase.query(ChatItemHelper.TABLE_CHAT_ITEMS, // table
				new String[] { ChatItemHelper.COLUMN_SENDER_ID }, // column
																	// names
				whereClause, // where clause
				new String[] { chatItem.getId() }, // where params
				null, // groupby
				null, // having
				null // orderby
				);
		return cursor;

	}

	public void deleteAll() {
		mDatabase.delete(ChatItemHelper.TABLE_CHAT_ITEMS, // table
				null, // where clause
				null // where params
				);
	}

	public void deleteChatItem(String id) {
		String whereClause = ChatItemHelper.COLUMN_SENDER_ID + " = ?";
		mDatabase.delete(ChatItemHelper.TABLE_CHAT_ITEMS, // table
				whereClause, // where clause
				new String[] { id } // where params
				);

	}

	public Cursor selectAll() {

		if (!mDatabase.isOpen()) {
			Log.i("chat item data source", "opening before select all method");
			open();
		}
		Cursor cursor = mDatabase.query(ChatItemHelper.TABLE_CHAT_ITEMS, // table
				new String[] { ChatItemHelper.COLUMN_SENDER_ID,
						ChatItemHelper.COLUMN_ID,
						ChatItemHelper.COLUMN_SENDER_NAME,
						ChatItemHelper.COLUMN_LAST_MESSAGE,
						ChatItemHelper.COLUMN_SENDER_CONTACT,
						ChatItemHelper.COLUMN_SENDER_IMG,
						ChatItemHelper.COLUMN_IS_SENT,
						ChatItemHelper.COLUMN_CREATED_AT }, // column names
				null, // where clause
				null, // where params
				null, // groupby
				null, // having
				null // orderby
				);

		return cursor;
	}

	public ArrayList<ChatItem> getAllChatItems() {

		Cursor cursor = selectAll();
		ArrayList<ChatItem> mChatItems = new ArrayList<ChatItem>();

		if (cursor.getCount() == 0) {
			return null;
		} else {
			cursor.moveToFirst();
			int row = 0;
			while (!cursor.isAfterLast()) {

				TextMessageDataSource mMessageDataSource = new TextMessageDataSource(
						mContext);
				try {
					mMessageDataSource.open();
				} catch (Exception e) {
					Log.e("Chat Item Data Source Line 184", "" + e.getMessage());
				}
				
				ChatItem chatItem = new ChatItem();
				
				int i = cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_IMG);
				chatItem.setImgUrl(cursor.getString(i));
				Log.e("MChatItem data source", "url-" + cursor.getString(i));
				// do stuff
				mChatItems.add(chatItem);

				i = cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_ID);
				mChatItems.get(row).setId(cursor.getString(i));
				
				if(chatItem.mMessages == null) {
					chatItem.mMessages = new ArrayList<TextMessage>();
				}
				chatItem.mMessages.clear();
				chatItem.mMessages = mMessageDataSource.getMessagesFrom(cursor.getString(i));

				i = cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_NAME);
				mChatItems.get(row).setContent(cursor.getString(i));

				// i =
				// cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_EMAIL);
				// mChatItems.get(row).setReceiverId(cursor.getString(i));

				// i = cursor
				// .getColumnIndex(ChatItemHelper.COLUMN_SENDER_CONTACT);
				// mChatItems.get(row).setReceiverName(cursor.getString(i));

				i = cursor.getColumnIndex(ChatItemHelper.COLUMN_LAST_MESSAGE);
				if (cursor.getString(i) != null) {
					Log.i("quick check", cursor.getString(i));
					mChatItems.get(row).setLastMessage(cursor.getString(i));
				}

				i = cursor.getColumnIndex(ChatItemHelper.COLUMN_CREATED_AT);
				if (cursor.getString(i) != null) {
					mChatItems.get(row).setLastMessageCreatedAt(
							cursor.getString(i));
				}

				// i = cursor.getColumnIndex(ChatItemHelper.COLUMN_IS_SENT);
				// boolean sent = false;
				// if (cursor.getString(i).equals("true")) {
				// sent = true;
				// }
				// mChatItems.get(row).setSent(sent);

				Log.d("ChatItem -- check", "retrieving :"
						+ mChatItems.get(row).getContent());

				cursor.moveToNext();
				row++;

			}
			// for (int i=0;i< mTextMessages.size(); i++) {
			// //Log.d("Database check", "added" +
			// mTextMessages.get(i).getSenderName());
			// }

			return mChatItems;
		}

	}

	public void upadteLastMessage(String id, TextMessage lastMessage) {
		if (!mDatabase.isOpen()) {
			open();
		}
		String whereClause = ChatItemHelper.COLUMN_SENDER_ID + " = ?";

		ContentValues values = new ContentValues();
		values.put(ChatItemHelper.COLUMN_LAST_MESSAGE, lastMessage.getMessage());
		mDatabase.update(TextMessageHelper.TABLE_MESSAGES, // table
				values, // values
				whereClause, // where clause
				new String[] { id } // where params
				);
		mDatabase.close();
	}
	
	public int updateImageUrlFromId(Context context,
			final ArrayList<HashMap<String, String>> friends) {

		int ans = 0;
		mChatItemHelper = new ChatItemHelper(context);
		mDatabase = mChatItemHelper.getWritableDatabase();
		if (!mDatabase.isOpen()) {
			open();
		}

		Log.e("ChatItem data source", "updating picss ");

		for (int i = 0; i < friends.size(); i++) {

			final HashMap<String, String> frnd = friends.get(i);

			String whereClause = ChatItemHelper.COLUMN_SENDER_ID + " = ?";
			ContentValues values = new ContentValues();
			values.put(ChatItemHelper.COLUMN_SENDER_IMG,
					frnd.get("img_url"));
			int res = mDatabase.update(ChatItemHelper.TABLE_CHAT_ITEMS, // table
					values, // column names
					whereClause, // where clause
					new String[] { frnd.get("id") });
			if (res > 0) {
				Log.e("frnds data ", "deleting and updating file");
				String middlePath = frnd.get("img_url").substring(93, 116);
				CustomTarget target = new CustomTarget(mContext);
				target.setTargetHash(middlePath);
				// TODO Auto-generated method stub
				Picasso.with(context).load(frnd.get("img_url"))
						.memoryPolicy(MemoryPolicy.NO_CACHE).into(target);

			}
			Log.e("cht item data source", "updated rows- " + res);
			// ans = ans + res;
		}

		return ans;

	}

}
