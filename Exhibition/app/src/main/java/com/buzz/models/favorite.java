package com.buzz.models;

/**
 * Created by NickChung on 6/10/15.
 */
public class favorite {
    private String extag;
    private String refImageId;
    private String refAudioId;

    public String getExtag() {
        return extag;
    }

    public void setExtag(String extag) {
        this.extag = extag;
    }

    public String getRefImageId() {
        return refImageId;
    }

    public void setRefImageId(String refImageId) {
        this.refImageId = refImageId;
    }

    public String getRefAudioId() {
        return refAudioId;
    }

    public void setRefAudioId(String refAudioId) {
        this.refAudioId = refAudioId;
    }

    @Override
    public boolean equals(Object o) {
        favorite obj = (favorite) o;
        boolean isEquals = false;
        if (this.extag.equals(obj.getExtag())
                && this.refImageId.equals(obj.refImageId)
                && (
                (this.refAudioId != null && this.refAudioId.equals(obj.refAudioId))
                        || (this.refAudioId == null)
        )
                ) {
            isEquals = true;
        }

        return isEquals;
    }
}
