package com.example.guybi.myapplication;

/**
 * Created by guybi on 2/26/2018.
 */

enum BUTTON_TYPE {
    LEFT(0x110),
    RIGHT(0x111);

    private final short val;
    private BUTTON_TYPE(int i) {
        val = (short) i;
    }
    public short getVal(){
        return val;
    }
}
