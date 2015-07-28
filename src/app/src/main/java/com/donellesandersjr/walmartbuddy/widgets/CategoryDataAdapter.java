package com.donellesandersjr.walmartbuddy.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.IconTextView;
import android.widget.TextView;

import com.donellesandersjr.walmartbuddy.R;
import com.donellesandersjr.walmartbuddy.api.WBStringUtils;
import com.donellesandersjr.walmartbuddy.models.CategoryModel;
import com.joanzapata.android.iconify.Iconify;

import java.util.List;

/**
 * This class is responsible for providing a listing of product categories
 */
public class CategoryDataAdapter extends ArrayAdapter<CategoryModel> {

    public CategoryDataAdapter(Context context, List<CategoryModel> categories) {
        super(context, R.layout.search_item_list_item, R.id.new_item_list_text, categories);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = super.getView(position, convertView, parent);
        TextView textView = (TextView) rootView.findViewById(R.id.new_item_list_text);
        textView.setTextColor(getContext().getResources().getColor(R.color.md_blue_500));
        //
        // We need to capture the icon used for this item
        //
        TextView iconTextView = (TextView) rootView.findViewById(R.id.new_item_list_icon);
        _setIcon(iconTextView, getItem(position));

        return rootView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View rootView =  super.getDropDownView(position, convertView, parent);
        //
        // We need to capture the icon used for this item
        //
        IconTextView iconTextView = (IconTextView) rootView.findViewById(R.id.new_item_list_icon);
        _setIcon (iconTextView, getItem(position));

        return rootView;
    }

    public int getDefaultCategoryPos () {
        int count = super.getCount();
        for (int i = 0; i < count; i++) {
            CategoryModel model = super.getItem(i);
            if (WBStringUtils.areEqual(model.getCategoryId(), "976759"))
                return i;
        }
        return -1;
    }

    private void _setIcon (TextView textView, CategoryModel model) {
        Iconify.IconValue iconValue;
        switch (model.getCategoryId()) {
            case "91083":
                //Auto & Tires
                iconValue = Iconify.IconValue.fa_automobile;
                break;
            case "3920":
                // Books
                iconValue = Iconify.IconValue.fa_book;
                break;

            case "1105910":
                //Cell Phones
                iconValue = Iconify.IconValue.fa_mobile_phone;
                break;

            case "3944":
                // Electronics
                iconValue = Iconify.IconValue.fa_laptop;
                break;

            case "976759":
                // Food
                iconValue = Iconify.IconValue.fa_shopping_cart;
                break;

            case "1094765":
                // Gifts & Registry
                iconValue = Iconify.IconValue.fa_gift;
                break;

            case "976760":
                // Health
                iconValue = Iconify.IconValue.fa_heartbeat;
                break;

            case "4044":
            case "1072864":
            case "1115193":
                // HOME
                iconValue = Iconify.IconValue.fa_home;
                break;

            case "3891":
                // Jewelry
                iconValue = Iconify.IconValue.fa_diamond;
                break;

            case "4096":
                // Movies & TV
                iconValue = Iconify.IconValue.fa_film;
                break;

            case "4104":
                // Music
                iconValue = Iconify.IconValue.fa_music;
                break;

            case "1229749":
                // Office
                iconValue = Iconify.IconValue.fa_fax;
                break;

            case "5440":
                // Pets
                iconValue = Iconify.IconValue.fa_paw;
                break;

            case "5426":
                // Photo Center
                iconValue = Iconify.IconValue.fa_camera;
                break;

            case "4125":
                // Sports & Outdoors
                iconValue = Iconify.IconValue.fa_soccer_ball_o;
                break;

            case "4171":
                // Toys
                iconValue = Iconify.IconValue.fa_rocket;
                break;

            default:
                iconValue = Iconify.IconValue.fa_tag;
        }

        Iconify.setIcon(textView, iconValue);
    }
}
