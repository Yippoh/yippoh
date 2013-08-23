package jim.h.common.android.zxingjar.demo;
public class Contact {
	  //private variables
    int _id;
    String _device_id;
    String _email_id;
    int _count;
     
    // Empty constructor
    public Contact(){
         
    }
    // constructor
    public Contact(int id, String device_id,String email_id,int count){
        this._id = id;
        this._device_id = device_id;
        this._email_id = email_id;
        this._count = count;
    }
     
    // constructor
    public Contact(String device_id,String email_id,int count){
        this._device_id = device_id;
        this._email_id = email_id;
        this._count = count;
    }
    // getting ID
    public int getID(){
        return this._id;
    }
     
    // setting id
    public void setID(int id){
        this._id = id;
    }
     
    // getting name
    public String getDeviceID(){
        return this._device_id;
    }
     
    // setting name
    public void setDeviceID(String device_id){
        this._device_id = device_id;
    }
    
    
    public String getEmailID(){
        return this._email_id;
    }
     
    // setting name
    public void setEmailID(String email_id){
        this._email_id = email_id;
    }
    
    // getting count
    public int getCount(){
        return this._count;
    }
     
    // setting id
    public void setCount(int count){
        this._count = count;
    }
	
}
