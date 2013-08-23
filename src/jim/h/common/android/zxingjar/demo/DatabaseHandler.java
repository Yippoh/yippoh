package jim.h.common.android.zxingjar.demo;

import java.util.ArrayList;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHandler extends SQLiteOpenHelper {

	 // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "contactsManager";
 
    // Contacts table name
    private static final String TABLE_CONTACTS = "contacts";
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DeviceID = "device_id";
    private static final String KEY_EmailID = "email_id";
    private static final String KEY_Count = "count";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DeviceID + " TEXT,"
                + KEY_EmailID + " TEXT(50),"
                + KEY_Count + " INTEGER " + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
 
        // Create tables again
        onCreate(db);
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    // Adding new contact
    void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_DeviceID, contact.getDeviceID()); // Contact Name
        values.put(KEY_EmailID, contact.getEmailID());
        values.put(KEY_Count, contact.getCount()); // Contact Phone
 
        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }
 
    
    // Getting single contact
 public Contact getContact(String device_id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
        		KEY_DeviceID,KEY_EmailID, KEY_Count }, KEY_DeviceID + "=?",
                new String[] { device_id }, null, null, null, null);
        Contact contact = null;
        if (cursor != null)
        {
            cursor.moveToFirst();
            contact=new Contact(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2),Integer.parseInt(cursor.getString(3)));
        }
        else
        {
        	contact = new Contact(0,null,null, 0);
        }
        // return contact
        return contact;
    }
  
 
    // Getting All Contacts
    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setID(Integer.parseInt(cursor.getString(0)));
                contact.setDeviceID(cursor.getString(1));
                contact.setEmailID(cursor.getString(2));
                contact.setCount(Integer.parseInt(cursor.getString(3)));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
 
        // return contact list
        return contactList;
    }
 
    // Updating single contact
    public void  updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_DeviceID, contact.getDeviceID());
        values.put(KEY_EmailID, contact.getEmailID());
        values.put(KEY_Count, contact.getCount());
 
        // updating row
        db.update(TABLE_CONTACTS, values, KEY_DeviceID + " = ?",
                new String[] { String.valueOf(contact.getDeviceID()) });
        db.close();
    }
 
    // Deleting single contact
    public void deleteContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_DeviceID + " = ?",
                new String[] { String.valueOf(contact.getDeviceID()) });
        db.close();
    }
 
 
    public int CheckExists(String DeviceId)
    {
    	String sql = "SELECT * FROM contacts WHERE device_id = '" + DeviceId + "'";
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery(sql, null);
    	       
//    	if (cursor.moveToFirst()) {
//    	    // record exists
//    	} else {
//    	    // record not found
//    	}
    	
   		 return cursor.getCount();
    }
    
    
    public int CheckEmailExists(String DeviceId)
    {
    	String sql = "SELECT device_id FROM contacts WHERE device_id = '" + DeviceId + "' AND email_id IS NOT NULL";
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery(sql, null);
//    	 int count=0;
//    	if (cursor.moveToFirst()) {
// 	    // record exists
//    		count =0;
//  	} else {
//   	    // record not found
//  		 count =1;
//    	}
    	
    	 return cursor.getCount();
    }
    
    // Getting contacts Count
//    public int getContactsCount() {
//        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(countQuery, null);
//        cursor.close();
// 
//        // return count
//        return cursor.getCount();
//    }
}
