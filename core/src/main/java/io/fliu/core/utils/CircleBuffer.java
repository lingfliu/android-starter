package io.fliu.core.utils;

/**
 * A ring buff supportting block bytes read / write
 * Created by liulingfeng on 2017/2/28.
 */

public class CircleBuffer<T> {
    private static final int DEFAULT_SIZE = 512;
    private Object[] buff;
    private int h;
    private int t;
    private int c; //next available pos to put byte
    private int rw; //next readable pos
    private boolean overflow;
    volatile int contentLen;

    public CircleBuffer(){
        buff = new Object[DEFAULT_SIZE];
        reset();
    }

    public CircleBuffer(int len){
        buff = new Object[len];
        reset();
    }

    public void push(T[] bs, int len){
        len = bs.length > len ? len : bs.length;
        for (int m = 0; m < len; m ++){
            buff[c] = bs[m];
            c++; //to the next empty position
            if (c > t && !overflow){
                c = h;
                overflow = true;
            }
            else if (c > t && overflow){
                c = h;
                if (rw == t){ //move p_rw to p_c
                    rw = c;
                }
            }

            //push the p_rw so p_c < = p_rw and is overflowed
            if (c > rw && overflow) {
                rw = c;
            }
        }

        contentLen = getAvailability();
    }

    public void read(T bs[], int len){
        if (bs == null) {
            return;
        }

        if (len > getAvailability() || len == 0){
            return;
        }

        for (int m = 0; m < len; m ++){
            if (rw + m <= t){
                bs[m] = (T) this.buff[rw + m];
            }
            else {
                bs[m] = (T) this.buff[h + m - (t - rw) - 1];
            }
        }
    }

    public void pop(T buff[], int len){
        if (buff == null) {
            return;
        }

        len = len > buff.length ? buff.length : len;

        int popLen = contentLen > len ? len : contentLen;

        for (int m = 0; m < popLen; m ++){
            buff[m] = (T) this.buff[rw];
            rw ++;
            if (rw > t){
                rw = h;
                overflow = false;
            }
        }

        contentLen = contentLen - len;
    }

    /*clear the whole buffer and reset its state*/
    public void flush(){
        buff = new Object[buff.length];
        reset();
    }

    /*reset the buffer*/
    public void reset(){
        h = 0;
        t = buff.length - 1;
        c = 0;
        rw = 0;
        overflow = false;
        contentLen = 0;
    }

    public int getAvailability(){
        if (overflow){
            return t - rw + c - h +1;
        }
        else {
            return c - rw;
        }
    }
}
