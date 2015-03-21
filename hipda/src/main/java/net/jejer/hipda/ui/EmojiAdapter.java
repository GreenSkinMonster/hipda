package net.jejer.hipda.ui;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import net.jejer.hipda.R;

public class EmojiAdapter extends BaseAdapter {

    //private Context mCtx;
    private LayoutInflater mInflater;

    private class EmojiBean {
        public int id;
        public String str;

        public EmojiBean(int id, String str) {
            this.id = id;
            this.str = str;
        }
    }

    private SparseArray<EmojiBean> mImgArray = new SparseArray<EmojiBean>();

    public EmojiAdapter(Context ctx, int tab) {
        //mCtx = ctx;
        mInflater = LayoutInflater.from(ctx);

        switch (tab) {
            case 1:
                mImgArray.append(0, new EmojiBean(R.drawable.default_smile, " :) "));
                mImgArray.append(1, new EmojiBean(R.drawable.default_sweat, " :sweat: "));
                mImgArray.append(2, new EmojiBean(R.drawable.default_huffy, " :huffy: "));
                mImgArray.append(3, new EmojiBean(R.drawable.default_cry, " :cry: "));
                mImgArray.append(4, new EmojiBean(R.drawable.default_titter, " :titter: "));
                mImgArray.append(5, new EmojiBean(R.drawable.default_handshake, " :handshake: "));
                mImgArray.append(6, new EmojiBean(R.drawable.default_victory, " :victory: "));
                mImgArray.append(7, new EmojiBean(R.drawable.default_curse, " :curse: "));
                mImgArray.append(8, new EmojiBean(R.drawable.default_dizzy, " :dizzy: "));
                mImgArray.append(9, new EmojiBean(R.drawable.default_shutup, " :shutup: "));
                mImgArray.append(10, new EmojiBean(R.drawable.default_funk, " :funk: "));
                mImgArray.append(11, new EmojiBean(R.drawable.default_loveliness, " :loveliness: "));
                mImgArray.append(12, new EmojiBean(R.drawable.default_sad, " :( "));
                mImgArray.append(13, new EmojiBean(R.drawable.default_biggrin, " :D "));
                mImgArray.append(14, new EmojiBean(R.drawable.default_cool, " :cool: "));
                mImgArray.append(15, new EmojiBean(R.drawable.default_mad, " :mad: "));
                mImgArray.append(16, new EmojiBean(R.drawable.default_shocked, " :o "));
                mImgArray.append(17, new EmojiBean(R.drawable.default_tongue, " :P "));
                mImgArray.append(18, new EmojiBean(R.drawable.default_lol, " :lol: "));
                mImgArray.append(19, new EmojiBean(R.drawable.default_shy, " :shy: "));
                mImgArray.append(20, new EmojiBean(R.drawable.default_sleepy, " :sleepy: "));
                break;
            case 2:
                mImgArray.append(0, new EmojiBean(R.drawable.coolmonkey_01, " {:2_41:} "));
                mImgArray.append(1, new EmojiBean(R.drawable.coolmonkey_02, " {:2_42:} "));
                mImgArray.append(2, new EmojiBean(R.drawable.coolmonkey_03, " {:2_43:} "));
                mImgArray.append(3, new EmojiBean(R.drawable.coolmonkey_04, " {:2_44:} "));
                mImgArray.append(4, new EmojiBean(R.drawable.coolmonkey_05, " {:2_45:} "));
                mImgArray.append(5, new EmojiBean(R.drawable.coolmonkey_06, " {:2_46:} "));
                mImgArray.append(6, new EmojiBean(R.drawable.coolmonkey_07, " {:2_47:} "));
                mImgArray.append(7, new EmojiBean(R.drawable.coolmonkey_08, " {:2_48:} "));
                mImgArray.append(8, new EmojiBean(R.drawable.coolmonkey_09, " {:2_49:} "));
                mImgArray.append(9, new EmojiBean(R.drawable.coolmonkey_10, " {:2_50:} "));
                mImgArray.append(10, new EmojiBean(R.drawable.coolmonkey_11, " {:2_51:} "));
                mImgArray.append(11, new EmojiBean(R.drawable.coolmonkey_12, " {:2_52:} "));
                mImgArray.append(12, new EmojiBean(R.drawable.coolmonkey_13, " {:2_53:} "));
                mImgArray.append(13, new EmojiBean(R.drawable.coolmonkey_14, " {:2_54:} "));
                mImgArray.append(14, new EmojiBean(R.drawable.coolmonkey_15, " {:2_55:} "));
                mImgArray.append(15, new EmojiBean(R.drawable.coolmonkey_16, " {:2_56:} "));
                break;
            case 3:
                mImgArray.append(0, new EmojiBean(R.drawable.grapeman_01, " {:3_57:} "));
                mImgArray.append(1, new EmojiBean(R.drawable.grapeman_02, " {:3_58:} "));
                mImgArray.append(2, new EmojiBean(R.drawable.grapeman_03, " {:3_59:} "));
                mImgArray.append(3, new EmojiBean(R.drawable.grapeman_04, " {:3_60:} "));
                mImgArray.append(4, new EmojiBean(R.drawable.grapeman_05, " {:3_61:} "));
                mImgArray.append(5, new EmojiBean(R.drawable.grapeman_06, " {:3_62:} "));
                mImgArray.append(6, new EmojiBean(R.drawable.grapeman_07, " {:3_63:} "));
                mImgArray.append(7, new EmojiBean(R.drawable.grapeman_08, " {:3_64:} "));
                mImgArray.append(8, new EmojiBean(R.drawable.grapeman_09, " {:3_65:} "));
                mImgArray.append(9, new EmojiBean(R.drawable.grapeman_10, " {:3_66:} "));
                mImgArray.append(10, new EmojiBean(R.drawable.grapeman_11, " {:3_67:} "));
                mImgArray.append(11, new EmojiBean(R.drawable.grapeman_12, " {:3_68:} "));
                mImgArray.append(12, new EmojiBean(R.drawable.grapeman_13, " {:3_69:} "));
                mImgArray.append(13, new EmojiBean(R.drawable.grapeman_14, " {:3_70:} "));
                mImgArray.append(14, new EmojiBean(R.drawable.grapeman_15, " {:3_71:} "));
                mImgArray.append(15, new EmojiBean(R.drawable.grapeman_16, " {:3_72:} "));
                mImgArray.append(16, new EmojiBean(R.drawable.grapeman_17, " {:3_73:} "));
                mImgArray.append(17, new EmojiBean(R.drawable.grapeman_18, " {:3_74:} "));
                mImgArray.append(18, new EmojiBean(R.drawable.grapeman_19, " {:3_75:} "));
                mImgArray.append(19, new EmojiBean(R.drawable.grapeman_20, " {:3_76:} "));
                mImgArray.append(20, new EmojiBean(R.drawable.grapeman_21, " {:3_77:} "));
                mImgArray.append(21, new EmojiBean(R.drawable.grapeman_22, " {:3_78:} "));
                mImgArray.append(22, new EmojiBean(R.drawable.grapeman_23, " {:3_79:} "));
                mImgArray.append(23, new EmojiBean(R.drawable.grapeman_24, " {:3_80:} "));
                break;
        }
    }

    @Override
    public int getCount() {
        return mImgArray.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mImgArray.get(arg0).str;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_emoji_grid, null);
            ((ImageView) convertView).setImageResource(mImgArray.get(position).id);
        } else {
            ((ImageView) convertView).setImageResource(mImgArray.get(position).id);
        }

        return convertView;
    }

}
