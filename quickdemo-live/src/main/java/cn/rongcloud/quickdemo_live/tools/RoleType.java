package cn.rongcloud.quickdemo_live.tools;

public enum RoleType {

    UNKNOWN(-1),
    VIEWER(0),  //观众
    ANCHOR(1);  //主播

    private int mValue;
    RoleType(int value){
        mValue =value;
    }

    public int getValue() {
        return mValue;
    }

}
