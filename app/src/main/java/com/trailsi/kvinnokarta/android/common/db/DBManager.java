package com.trailsi.kvinnokarta.android.common.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.trailsi.kvinnokarta.android.common.cache.CachedData;
import com.trailsi.kvinnokarta.android.common.utils.StringHelper;
import com.trailsi.kvinnokarta.android.model.Audio;
import com.trailsi.kvinnokarta.android.model.AudioAds;
import com.trailsi.kvinnokarta.android.model.AudioType;
import com.trailsi.kvinnokarta.android.model.Document;
import com.trailsi.kvinnokarta.android.model.Language;
import com.trailsi.kvinnokarta.android.model.LocalFile;
import com.trailsi.kvinnokarta.android.model.Location;
import com.trailsi.kvinnokarta.android.model.PurchaseResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    public static final String TAG = "DBManager";

    private static DBManager instance;

    private SQLiteHelper helper;
    public SQLiteDatabase db;

    public static void init(Context ctx) {
        instance = new DBManager(ctx);
    }

    public static synchronized DBManager getInstance() {
        return instance;
    }

    private DBManager(Context ctx) {
        helper = new SQLiteHelper(ctx);
        db = helper.getWritableDatabase();
    }

    private long insertData(String tableName, ContentValues contentValues) {
        long id = -1;
        try {
            db.beginTransaction();
            id = db.insert(tableName, null, contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return id;
    }

    private boolean deleteData(String tableName, long id) {
        boolean ret = false;
        try {
            db.beginTransaction();
            db.delete(tableName, "id=?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return ret;
    }

    private boolean updateData(String tableName, ContentValues contentValues, long id) {
        boolean ret = false;
        try {
            db.beginTransaction();
            db.update(tableName, contentValues, "id=?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return ret;
    }

    public boolean clearTable(String tableName) {
        boolean res = false;
        try {
            db.beginTransaction();
            db.delete(tableName, null, null);
            db.setTransactionSuccessful();
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return res;
    }

    public boolean dropTable(String tableName) {
        boolean ret = false;
        try {
            db.beginTransaction();
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            db.setTransactionSuccessful();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return ret;
    }

    // Close DB
    public void close() {
        db.close();
    }

    public void clearLocations() {
        dropTable(Location.TABLE_NAME);
        Location.createTable(db);
    }

    public void clearAudio() {
        dropTable(Audio.TABLE_NAME);
        Audio.createTable(db);
        dropTable(AudioAds.TABLE_NAME);
        AudioAds.createTable(db);
    }

    public void clearDocument() {
        dropTable(Document.TABLE_NAME);
        Document.createTable(db);
    }

    public void clearLanguage() {
        dropTable(Language.TABLE_NAME);
        Language.createTable(db);
    }

    public void clearAudioType() {
        dropTable(AudioType.TABLE_NAME);
        AudioType.createTable(db);
    }

    public void clearAudioAds() {
        dropTable(AudioAds.TABLE_NAME);
        AudioAds.createTable(db);
    }

    public long addLocation(Location contact) {
        return insertData(Location.TABLE_NAME, contact.prepareContentValue());
    }

    public void getLocations(List<Location> locations) {
        try (Cursor c = db.query(Location.TABLE_NAME, Location.COLUMN, null, null, null, null, null)) {
            while (c != null && c.moveToNext()) {
                Location location = new Location(c);
                location.audios = getAudiosByLocationNumber(location.number);
                locations.add(location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long addAudio(Audio contact) {
        return insertData(Audio.TABLE_NAME, contact.prepareContentValue());
    }

    public Audio getAudio(int location_number) {
        String language = CachedData.getString(CachedData.kLanguage, "");
        String type = CachedData.getString(CachedData.kAudioType, "");
        Audio audio = null;
        String query = "location_number=" + location_number + " AND language='" + language + "' AND type='" + type + "'";
        try (Cursor c = db.query(Audio.TABLE_NAME, Audio.COLUMN, query, null, null, null, null)) {
            if (c != null && c.moveToNext()) {
                audio = new Audio(c);
                audio.ads = getAdsByAudio(audio.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return audio;
    }

    public List<Audio> getAudiosByLocationNumber(int location_number) {
        List<Audio> audios = new ArrayList<>();
        String query = "location_number=" + location_number;
        try (Cursor c = db.query(Audio.TABLE_NAME, Audio.COLUMN, query, null, null, null, null)) {
            while (c != null && c.moveToNext()) {
                audios.add(new Audio(c));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return audios;
    }

    public long addAudioAds(AudioAds contact) {
        return insertData(AudioAds.TABLE_NAME, contact.prepareContentValue());
    }

    public List<AudioAds> getAdsByAudio(int audio_id) {
        List<AudioAds> ads = new ArrayList<>();
        String query = "audio_id=" + audio_id;
        try (Cursor c = db.query(AudioAds.TABLE_NAME, AudioAds.COLUMN, query, null, null, null, null)) {
            while (c != null && c.moveToNext()) {
                ads.add(new AudioAds(c));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ads;
    }

    public long addDocument(Document contact) {
        return insertData(Document.TABLE_NAME, contact.prepareContentValue());
    }

    public void getDocuments(List<Document> documents) {
        try (Cursor c = db.query(Document.TABLE_NAME, Document.COLUMN, null, null, null, null, null)) {
            while (c != null && c.moveToNext()) {
                Document document = new Document(c);
                documents.add(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long addLanguage(Language contact) {
        return insertData(Language.TABLE_NAME, contact.prepareContentValue());
    }

    public void getLanguages(List<Language> languages) {
        try (Cursor c = db.query(Language.TABLE_NAME, Language.COLUMN, null, null, null, null, null)) {
            while (c != null && c.moveToNext()) {
                Language language = new Language(c);
                languages.add(language);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long addAudioType(AudioType contact) {
        return insertData(AudioType.TABLE_NAME, contact.prepareContentValue());
    }

    public void getAudioTypes(List<AudioType> types) {
        try (Cursor c = db.query(AudioType.TABLE_NAME, AudioType.COLUMN, null, null, null, null, null)) {
            while (c != null && c.moveToNext()) {
                AudioType type = new AudioType(c);
                types.add(type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AudioType getCurrentAudioType() {
        String selectedType = CachedData.getString(CachedData.kAudioType, "");
        AudioType audioType = null;
        try (Cursor c = db.query(AudioType.TABLE_NAME, AudioType.COLUMN, "name=?", new String[]{selectedType}, null, null, null)) {
            if (c != null && c.moveToNext()) {
                audioType = new AudioType(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return audioType;
    }

    public long addPurchaseResult(PurchaseResult contact) {
        return insertData(PurchaseResult.TABLE_NAME, contact.prepareContentValue());
    }

    public PurchaseResult getPurchaseResultByTour(String type) {
        PurchaseResult result = null;
        try (Cursor c = db.query(PurchaseResult.TABLE_NAME, PurchaseResult.COLUMN, "type=?", new String[]{type}, null, null, null)) {
            if (c != null && c.moveToNext()) {
                result = new PurchaseResult(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public long addLocalFile(LocalFile contact) {
        return insertData(LocalFile.TABLE_NAME, contact.prepareContentValue());
    }

    public boolean deleteLocalFile(LocalFile contact) {
        boolean ret = false;
        try {
            db.beginTransaction();
            db.delete(LocalFile.TABLE_NAME, "id=?", new String[]{String.valueOf(contact.id)});
            db.setTransactionSuccessful();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return ret;
    }

    public LocalFile getLocalFile(String link) {
        LocalFile localFile = null;
        try (Cursor c = db.query(LocalFile.TABLE_NAME, LocalFile.COLUMN, "cloud_link=?", new String[]{link}, null, null, null)) {
            if (c != null && c.moveToNext()) {
                localFile = new LocalFile(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return localFile;
    }

    public File getRealFile(Context context, String link) {
        String localFile = null;
        try (Cursor c = db.query(LocalFile.TABLE_NAME, LocalFile.COLUMN, "cloud_link=?", new String[]{link}, null, null, null)) {
            if (c != null && c.moveToNext()) {
                localFile = new LocalFile(c).local_file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (StringHelper.isEmpty(localFile)) {
            return null;
        }
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), localFile);
    }
}
