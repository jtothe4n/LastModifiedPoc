package eu.duong.lastmodified_poc;

import android.net.Uri;

public class MediaStoreImage {

    public MediaStoreImage(long id, Uri uri, String path, String name)
    {
        this.id = id;
        this.uri = uri;
        this.path = path;
        this.name = name;
    }

    public long id;
    public Uri uri;
    public String path;
    public String name;
}
