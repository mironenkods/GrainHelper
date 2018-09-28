package com.edfman.grainhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DatabaseHandler extends SQLiteOpenHelper {



    private static final String server_Ip = "http://178.92.47.54:8085";

    private static final int DATABASE_VERSION       = 1;
    private static final String DATABASE_NAME       = "grain_helper";
    private static final String TABLE_CARS          = "cars";
    private static final String TABLE_DRIVERS       = "drivers";
    private static final String TABLE_FIELDS        = "fields";
    private static final String TABLE_WAREHOUSES    = "warehouses";
    private static final String TABLE_CROPS         = "crops";
    private static final String TABLE_DOCS          = "docs";
    private static final String TABLE_MESS          = "messages";
    private static final String FIELD_ID            = "id";
    private static final String FIELD_1C_ID         = "id_1c";
    private static final String FIELD_TITLE         = "title";
    private static final String FIELD_DRIVER_ID     = "driver_id";
    private static final String FIELD_DATE          = "date";
    private static final String FIELD_CAR_ID        = "car_id";
    private static final String FIELD_CAR_TITLE     = "car_title";
    private static final String FIELD_CROP_ID       = "crop_id";
    private static final String FIELD_FIELD_ID      = "field_id";
    private static final String FIELD_WAREHOUSE_ID  = "warehouse_id";
    private static final String FIELD_CREATED_BY    = "created_by";
    private static final String FIELD_DOC_ID        = "doc_id";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "create table if not exists " + TABLE_CARS   + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT, " + FIELD_DRIVER_ID + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_DRIVERS       + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_FIELDS        + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_WAREHOUSES    + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_CROPS         + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_MESS           + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                                            + FIELD_DATE        + " LONG, "
                                                                            + FIELD_CAR_ID      + " TEXT, "
                                                                            + FIELD_DOC_ID     + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_DOCS          + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FIELD_TITLE + " TEXT,"
                + FIELD_DATE + " LONG, "
                + FIELD_CAR_ID + " TEXT, "
                + FIELD_DRIVER_ID + " TEXT, "
                + FIELD_CROP_ID + " TEXT, "
                + FIELD_FIELD_ID + " TEXT, "
                + FIELD_WAREHOUSE_ID + " TEXT, "
                + FIELD_CREATED_BY + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addDocument(Document document, String deviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_DATE, document.getDate());
        values.put(FIELD_CAR_ID, document.getCar_id());
        values.put(FIELD_DRIVER_ID, document.getDriver_id());
        values.put(FIELD_CROP_ID, document.getCrop_id());
        values.put(FIELD_FIELD_ID, document.getField_id());
        values.put(FIELD_WAREHOUSE_ID, document.getWarehouse_id());
        values.put(FIELD_CREATED_BY, document.getCreated_by());

        long doc_id = db.insert(TABLE_DOCS, null, values);
        if (deviceId != "") {
            if (doc_id != -1) {
                values = new ContentValues();
                values.put(FIELD_DATE, document.getDate());
                values.put(FIELD_DOC_ID, doc_id);

                db.insert(TABLE_MESS, null, values);
            }

            //try send messages
            this.sendAllMessages(deviceId);
        }

        db.close();
    }

    public void addRef(TypicalRef ref, String table_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_1C_ID, ref.getId_1c());
        values.put(FIELD_TITLE, ref.getTitle());
        if(ref.getType() == RefType.car) values.put(FIELD_DRIVER_ID, ref.getDriver_id());

        db.insert(table_name, null, values);
        db.close();
    }

    public Document getDoc(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DOCS, new String[] {
                        FIELD_ID,
                        FIELD_TITLE,
                        FIELD_DRIVER_ID,
                        FIELD_DATE,
                        FIELD_CAR_ID,
                        FIELD_CROP_ID,
                        FIELD_FIELD_ID,
                        FIELD_WAREHOUSE_ID,
                        FIELD_CREATED_BY}, FIELD_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null){
            cursor.moveToFirst();
        }
        //int id, String title, int driver_id, long date, int car_id, int crop_id, int field_id, int warehouse_id, String created_by
        Document document = new Document(
                cursor.getInt(0),
                cursor.getString(2),
                cursor.getLong(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getString(8));

        db.close();
        return document;
    }

    public Document getStringDoc(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select\n" +
                "docs.id,\n" +
                "docs.driver_id as driver,\n" +
                "docs.date,\n" +
                "cars.title as car,\n" +
                "crops.title as crop,\n" +
                "fields.title as field,\n" +
                "warehouses.title as warehouse,\n" +
                "docs.created_by\n" +
                "from docs as docs left join cars as cars on docs.car_id = cars.id_1c " +
                "left join fields as fields on docs.field_id = fields.id_1c left join crops as crops on docs.crop_id = crops.id_1c " +
                "left join warehouses as  warehouses on docs.warehouse_id = warehouses.id_1c\n" +
                "where docs.id>= ?", new String[] {String.valueOf(id)}, null);

        if (cursor != null){
            cursor.moveToFirst();
        }
        // int id, String driver_id, long date, String car_id, String crop_id, String field_id, String warehouse_id, String created_by) {
        Document document = new Document(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getLong(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7));

        db.close();
        return document;
    }

    public List<Document> getAllDocs() {
        List<Document> docsList = new ArrayList<Document>();
        String selectQuery = "SELECT  * FROM " + TABLE_DOCS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Document doc = new Document(
                        cursor.getInt(0),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8));
                docsList.add(doc);
            } while (cursor.moveToNext());
        }

        db.close();
        return docsList;
    }

    public List<TypicalRef> getRefElements(String table_name) {
        List<TypicalRef> refList = new ArrayList<TypicalRef>();
        String selectQuery = "SELECT  * FROM " + table_name;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        RefType curr_type = RefType.car;
        if(table_name==TABLE_CARS) curr_type = RefType.car;
        if(table_name==TABLE_CROPS) curr_type = RefType.crop;
        if(table_name==TABLE_DRIVERS) curr_type = RefType.driver;
        if(table_name==TABLE_FIELDS) curr_type = RefType.field;
        if(table_name==TABLE_WAREHOUSES) curr_type = RefType.warehouse;

        if (cursor.moveToFirst()) {
            do {
                TypicalRef ref = new TypicalRef(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        curr_type);
                refList.add(ref);
            } while (cursor.moveToNext());
        }

        db.close();
        return refList;
    }

    public ArrayList<TypicalRef> getRefElementsArrayList(String table_name) {
        ArrayList<TypicalRef> refList = new ArrayList<TypicalRef>();
        String selectQuery = "SELECT  * FROM " + table_name;

        RefType currType = null;
        if (table_name == TABLE_WAREHOUSES) currType = RefType.warehouse;
        else if (table_name == TABLE_FIELDS) currType = RefType.field;
        else if (table_name == TABLE_DRIVERS) currType = RefType.driver;
        else if (table_name == TABLE_CROPS) currType = RefType.crop;
        else if (table_name == TABLE_CARS) currType = RefType.car;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            if (table_name != TABLE_CARS){
                refList.add(new TypicalRef( cursor.getString(cursor.getColumnIndex("id_1c")),
                        cursor.getString(cursor.getColumnIndex("title")),
                        "",
                        currType));
            } else {
                refList.add(new TypicalRef(cursor.getString(cursor.getColumnIndex("id_1c")),
                        cursor.getString(cursor.getColumnIndex("title")),
                        cursor.getString(cursor.getColumnIndex("driver_id")),
                        currType));
            }
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return refList;

    }

    public Map<String, String> getRefElementsMap(String table_name) {
        List<TypicalRef> refList = new ArrayList<TypicalRef>();
        String selectQuery = "SELECT  * FROM " + table_name;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        Map<String, String> names = new HashMap<>();
        while(!cursor.isAfterLast()) {
            names.put(cursor.getString(cursor.getColumnIndex("title")),cursor.getString(cursor.getColumnIndex("id_1c")));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return names;

    }

    public TypicalRef getRefById(String table_name, String id) {
        List<TypicalRef> refList = new ArrayList<TypicalRef>();
        String selectQuery;
        if(table_name==TABLE_DRIVERS){
            selectQuery = "SELECT  * FROM " + table_name + " WHERE " + table_name + ".title LIKE ?";
        } else {
            selectQuery = "SELECT  * FROM " + table_name + " WHERE " + table_name + ".id_1c=?";
        }
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        if(table_name==TABLE_DRIVERS){
            cursor = db.rawQuery(selectQuery, new String[]{"%" + id + "%"});
        } else {
            cursor = db.rawQuery(selectQuery, new String[]{id});
        }

        RefType curr_type = RefType.car;
        if(table_name==TABLE_CARS) curr_type = RefType.car;
        if(table_name==TABLE_CROPS) curr_type = RefType.crop;
        if(table_name==TABLE_DRIVERS) curr_type = RefType.driver;
        if(table_name==TABLE_FIELDS) curr_type = RefType.field;
        if(table_name==TABLE_WAREHOUSES) curr_type = RefType.warehouse;
        TypicalRef ref = null;
        if (cursor.moveToFirst()) {

            if(table_name!=TABLE_DRIVERS){
                ref = new TypicalRef(
                        cursor.getString(1),
                        cursor.getString(2),
                        "",
                        curr_type);
            } else {
                ref = new TypicalRef(
                        "",
                        cursor.getString(2),
                        "",
                        curr_type);
            }


        }
        cursor.close();
        db.close();
        return ref;
    }

    public List<Document> getDocsByDate(String start_date, String finish_date) {
        List<Document> docsList = new ArrayList<Document>();
        String selectQuery = "SELECT  * FROM " + TABLE_DOCS
                + " WHERE " + FIELD_DATE + " >= ? AND "
                + FIELD_DATE + " <= ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{start_date, finish_date});

        if (cursor.moveToFirst()) {
            do {
                Document doc = new Document(
                        cursor.getInt(0),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8));
                docsList.add(doc);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return docsList;
    }

    public Cursor getDocsByDate_Cursor(String start_date, String finish_date) {
        List<Document> docsList = new ArrayList<Document>();
        String selectQuery = "select\n" +
                "docs.id AS _id,\n" +
                "ifnull(docs.driver_id, '')  AS driver,\n" +
                "ifnull(cars.title, '') AS car\n" +
                "from\n" +
                "docs left join cars ON\n" +
                "cars.id_1c = docs.car_id\n" +
                "where docs.date>= ? and docs.date<= ?";
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(selectQuery, new String[] {start_date, finish_date});
    }

    public Cursor getMessages_Cursor() {
        List<Document> docsList = new ArrayList<Document>();
        String selectQuery = "select messages.id AS _id, messages.date AS date, cars.title AS car from " + TABLE_MESS + " AS messages LEFT JOIN cars AS cars ON messages.car_id = cars.id_1c";
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(selectQuery, null);
    }

    public void clearAllRefs(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CARS, null, null);
        db.delete(TABLE_CROPS, null, null);
        db.delete(TABLE_DRIVERS, null, null);
        db.delete(TABLE_FIELDS, null, null);
        db.delete(TABLE_WAREHOUSES, null, null);
        db.close();
    }

    public void clearDocs(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOCS, null, null);
        db.close();
    }

    public void clearMessages(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESS, null, null);
        db.close();
    }

    public void sendAllMessages(String deviceId){
        SQLiteDatabase db = this.getReadableDatabase();
        DocsResult docsResult = new DocsResult();

        Cursor cursor = db.rawQuery("select\n" +
                "docs.id,\n" +
                "docs.driver_id as driver,\n" +
                "docs.date,\n" +
                "docs.car_id as car,\n" +
                "docs.crop_id as crop,\n" +
                "docs.field_id as field,\n" +
                "docs.warehouse_id as warehouse,\n" +
                "docs.created_by\n" +
                "from messages as messages inner join docs as docs on messages.doc_id = docs.id", null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
        // int id, String driver_id, long date, String car_id, String crop_id, String field_id, String warehouse_id, String created_by) {
        Document document = new Document(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getLong(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7));
            docsResult.invoices.add(document);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        if (docsResult.invoices != null && !docsResult.invoices.isEmpty()) {
            Gson gson = new Gson();
            String body = gson.toJson(docsResult);

            HTTPWorking.request_answer request = new HTTPWorking().callWebServicePost(server_Ip + "/agro/hs/Grain/main/post?&emei=" + deviceId + "", body);
            if (request!= null && request.code == 200) {
                clearMessages();
                MainActivity.lastSync_docs = System.currentTimeMillis()/1000  + 2 * 60 * 60;
            }
        }

    }

    protected void renewRefs(String lName, String lpass){

        SQLiteDatabase db = this.getWritableDatabase();

        HTTPWorking.request_answer request = new HTTPWorking().callWebService(server_Ip + "/agro/hs/Grain/ref/GetRef?login=" + lName + "&password=" + lpass + "");
        if (request.code == 200) {
            //clear all ref tables
            clearAllRefs();
            Gson gson = new Gson();
            Result result = gson.fromJson(request.Body, Result.class);
            for (TypicalRef ref : result.result_list) {
                if (ref.type == RefType.car) addRef(ref, TABLE_CARS);
                if (ref.type == RefType.crop) addRef(ref, TABLE_CROPS);
                if (ref.type == RefType.driver) addRef(ref, TABLE_DRIVERS);
                if (ref.type == RefType.field) addRef(ref, TABLE_FIELDS);
                if (ref.type == RefType.warehouse) addRef(ref, TABLE_WAREHOUSES);
            }
            MainActivity.lastSync_refs = System.currentTimeMillis()/1000 + 2 * 60 * 60;
        }
    }

}

