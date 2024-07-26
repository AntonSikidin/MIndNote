package com.AgaKakje.mindnote;

public class Entity {

    public int     i_id;
    public String  s_name;
    public int i_parent;
    public int i_color;
    public int i_leftMargin;
    public int i_topMargin;
    public int i_rightMargin;
    public int i_bottomMargin;
    public float f_scalediff;
    public float f_rotation;

    public int i_order;

    public Entity(int i_id, String s_name, int i_parent, int i_color, int i_leftMargin, int i_topMargin, int i_rightMargin, int i_bottomMargin, float f_scalediff, float f_rotation, int i_order) {
        this.i_id = i_id;
        this.s_name = s_name;
        this.i_parent = i_parent;
        this.i_color = i_color;
        this.i_leftMargin = i_leftMargin;
        this.i_topMargin = i_topMargin;
        this.i_rightMargin = i_rightMargin;
        this.i_bottomMargin = i_bottomMargin;
        this.f_scalediff = f_scalediff;
        this.f_rotation = f_rotation;
        this.i_order = i_order;
    }


}
