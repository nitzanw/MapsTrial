package com.nitzandev.mapstrial;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nitzanwerber on 5/7/15.
 */
public class MeasureType implements Parcelable {
    private int id = 0;
    private String type = "";
    private String ccx = "";
    private int minimumDistance = 0;
    private int lower_threshold = 0;
    private int upper_threshold = 0;

    MeasureType() {

    }


    MeasureType(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public String toString() {
        return type;
    }

    private void readFromParcel(Parcel in) {
        this.id = in.readInt();
        this.type = in.readString();
        this.ccx = in.readString();
        this.minimumDistance = in.readInt();
        this.lower_threshold = in.readInt();
        this.upper_threshold = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(type);
        parcel.writeString(ccx);
        parcel.writeInt(minimumDistance);
        parcel.writeInt(lower_threshold);
        parcel.writeInt(upper_threshold);

    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MeasureType createFromParcel(Parcel in) {
            return new MeasureType(in);
        }

        public MeasureType[] newArray(int size) {
            return new MeasureType[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCcx() {
        return ccx;
    }

    public void setCcx(String ccx) {
        this.ccx = ccx;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMinimumDistance() {
        return minimumDistance;
    }

    public void setMinimumDistance(int minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    public int getLower_threshold() {
        return lower_threshold;
    }

    public void setLower_threshold(int lower_threshold) {
        this.lower_threshold = lower_threshold;
    }

    public int getUpper_threshold() {
        return upper_threshold;
    }

    public void setUpper_threshold(int upper_threshold) {
        this.upper_threshold = upper_threshold;
    }
}
