/*
 * 	 Copyright 2014 Sergio Carrozzo
 * 
 *   This file is part of Facial Expression Recognizer.
 * 
 *   Facial Expression Recognizer is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Facial Expression Recognizer is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Facial Expression Recognizer.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.altervista.scarrozzo.facialexpressionrecognizer.Model;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ExpressionContentProvider extends ContentProvider{

	public static final Uri CONTENT_URI = Uri.parse("content://org.altervista.scarrozzo.facialexpressionrecognizer/elements");
	
	// Create the constants used to differentiate between // the different URI requests.
	private static final int ALLROWS = 1;
	private static final int SINGLE_ROW = 2;
	
	private static final UriMatcher uriMatcher;
	
	// Populate the UriMatcher object, where a URI ending
	// in elements will correspond to a request for all
	// items, and elements/[rowID] represents a single row. 
	static {
			uriMatcher = new UriMatcher(UriMatcher.NO_MATCH); 
			uriMatcher.addURI("org.altervista.scarrozzo.facialexpressionrecognizer",
							  "elements", ALLROWS); 
			uriMatcher.addURI("org.altervista.scarrozzo.facialexpressionrecognizer",
							  "elements/#", SINGLE_ROW); 
			}
	
	// The index (key) column name for use in where clauses. 
	public static final String KEY_ID = "_id";
	
	// The name and column index of each column in your database.
	// These should be descriptive.
	public static final String KEY_COLUMN_1_NAME = "TITLE"; 
	public static final String KEY_COLUMN_2_NAME = "Description";
	public static final String KEY_COLUMN_3_NAME = "Neutral";
	public static final String KEY_COLUMN_4_NAME = "Happy";
	public static final String KEY_COLUMN_5_NAME = "Surprise";
	public static final String KEY_COLUMN_6_NAME = "Anger";
	public static final String KEY_COLUMN_7_NAME = "Fear";
	public static final String KEY_COLUMN_8_NAME = "Sad";
	public static final String KEY_COLUMN_9_NAME = "Disgust";
	public static final String KEY_COLUMN_10_NAME = "Expression";
	// BLOB Bitamp

	// TODO: Create public field for each column in your table.
	
	// SQLite Open Helper variable
	private ExpressionDBOpenHelper myOpenHelper;
	
	@Override
	public boolean onCreate() {
		// Construct the underlying database.
		// Defer opening the database until you need to perform // a query or transaction.
		myOpenHelper = new ExpressionDBOpenHelper(getContext(),
				ExpressionDBOpenHelper.DATABASE_NAME, null, ExpressionDBOpenHelper.DATABASE_VERSION);
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		// Open the database.
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();
		// Replace these with valid SQL statements if necessary. 
		String groupBy = null;
		String having = null;
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder(); 
		queryBuilder.setTables(ExpressionDBOpenHelper.DATABASE_TABLE);
		
		// If this is a row query, limit the result set to the // passed in row.
		switch (uriMatcher.match(uri)) {
			case SINGLE_ROW :
			String rowID = uri.getPathSegments().get(1); 
			queryBuilder.appendWhere(KEY_ID + "=" + rowID);
			default: break; 
		}
		
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
		
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// Return a string that identifies the MIME type 
		// for a Content Provider URI
		switch (uriMatcher.match(uri)) {
		case ALLROWS:
			return "vnd.android.cursor.dir/vnd.facialexpression.elemental";
		case SINGLE_ROW:
			return "vnd.android.cursor.item/vnd.facialexpression.elemental";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Open a read / write database to support the transaction. 
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();
		
		// To add empty rows to your database by passing in an empty // Content Values object, you must use the null column hack // parameter to specify the name of the column that can be // set to null.
		String nullColumnHack = null;
		
		// Insert the values into the table
		long id = db.insert(ExpressionDBOpenHelper.DATABASE_TABLE,
		nullColumnHack, values);
		
		if (id > -1) {
		// Construct and return the URI of the newly inserted row. 
			Uri insertedId = ContentUris.withAppendedId(CONTENT_URI, id);
		// Notify any observers of the change in the data set. 
			getContext().getContentResolver().notifyChange(insertedId, null);
			return insertedId; 
		}
		else
			return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Open a read / write database to support the transaction. 
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();
		
		// If this is a row URI, limit the deletion to the specified row. 
		switch (uriMatcher.match(uri)) {
			case SINGLE_ROW :
			String rowID = uri.getPathSegments().get(1); 
			selection = KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			default: break; 
		}
		
		// To return the number of deleted items, you must specify a where // clause. To delete all rows and return a value, pass in "1".
		if (selection == null)
		selection = "1";
		
		// Execute the deletion.
		int deleteCount = db.delete(ExpressionDBOpenHelper.DATABASE_TABLE, selection, selectionArgs);
		
		// Notify any observers of the change in the data set. 
		getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// Open a read / write database to support the transaction. 
		SQLiteDatabase db = myOpenHelper.getWritableDatabase();
		
		// If this is a row URI, limit the deletion to the specified row. 
		switch (uriMatcher.match(uri)) {
			case SINGLE_ROW :
			String rowID = uri.getPathSegments().get(1); 
			selection = KEY_ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			default: break; 
		}
		
		// Perform the update.
		int updateCount = db.update(ExpressionDBOpenHelper.DATABASE_TABLE,
		values, selection, selectionArgs);
		
		// Notify any observers of the change in the data set. 
		getContext().getContentResolver().notifyChange(uri, null);
		
		return updateCount;
	}
	
	
	private static class ExpressionDBOpenHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "facialExpressions.db"; 
		private static final String DATABASE_TABLE = "ExpressionImage"; 
		private static final int DATABASE_VERSION = 1;
		
		// SQL Statement to create a new database.
		private static final String DATABASE_CREATE = "create table " +
		DATABASE_TABLE + " (" 
				+ KEY_ID +" integer primary key autoincrement, " 
				+ KEY_COLUMN_1_NAME + " text not null, "
				+ KEY_COLUMN_2_NAME + " text not null, "
				+ KEY_COLUMN_3_NAME + " text not null, "
				+ KEY_COLUMN_4_NAME + " text not null, "
				+ KEY_COLUMN_5_NAME + " text not null, "
				+ KEY_COLUMN_6_NAME + " text not null, "
				+ KEY_COLUMN_7_NAME + " text not null, "
				+ KEY_COLUMN_8_NAME + " text not null, "
				+ KEY_COLUMN_9_NAME + " text not null, "
				+ KEY_COLUMN_10_NAME + " blob not null);";
		
		public ExpressionDBOpenHelper(Context context, String name, CursorFactory factory, int version) 
		{
			super(context, name, factory, version); 
		}
		
		// Called when no database exists in disk and the helper class needs // to create a new one.
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE); 
		}
	
		// Called when there is a database version mismatch meaning that // the version of the database on disk needs to be upgraded to // the current version.
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Log the version upgrade.
			Log.w("TaskDBAdapter", "Upgrading from version " +
			oldVersion + " to " +
			newVersion + ", which will destroy all old data");
			
			// Upgrade the existing database to conform to the new // version. Multiple previous versions can be handled by // comparing oldVersion and newVersion values.
			// The simplest case is to drop the old table and create a new one. 
			db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
			// Create a new one.
			onCreate(db);
		}
	}
}
