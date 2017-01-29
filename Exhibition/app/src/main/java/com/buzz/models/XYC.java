package com.buzz.models;

import com.buzz.utils.GlobalConst;

/**
 * Created by NickChung on 5/12/15.
 * @deprecated
 */
public class XYC implements Comparable<XYC> {
    public float X;
    public float Y;
    public int C;
    public int S;
    public int L;
    public String T;

    public XYC(float x, float y, int c, int s) {
        this.X = x;
        this.Y = y;
        this.C = c;
        this.S = s;
        switch (c) {
            case GlobalConst.HEX_COLOR_LEVEL_01:
                this.L = 0;
                break;

            case GlobalConst.HEX_COLOR_LEVEL_02:
                this.L = 1;
                break;

            case GlobalConst.HEX_COLOR_LEVEL_03:
                this.L = 2;
                break;

            case GlobalConst.HEX_COLOR_LEVEL_04:
                this.L = 3;
                break;
        }
    }

    @Override
    public String toString() {
        return "XYC{" +
                "X=" + X +
                ", Y=" + Y +
                ", C=" + C +
                ", S=" + S +
                ", L=" + L +
                ", T='" + T + '\'' +
                '}';
    }

    @Override
    public int compareTo(XYC o) {
        return this.L - o.L;
    }
}
