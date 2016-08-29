package com.cetsk.android.talkorigins;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

class ClaimAdapter extends ArrayAdapter<Claim> {
		private ArrayList<Claim> items;

		public ClaimAdapter(Context context, int textViewResourceId, ArrayList<Claim> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) { 
				LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item, null);
			}
			final Claim c = items.get(position);
			if (c != null) {
				TextView tt = (TextView) v.findViewById(R.id.list_item_text);
				tt.setText(c.getName());
				
				LinearLayout ll = (LinearLayout) v.findViewById(R.id.list_item);
				ll.setTag(c);
				File f = new File(getContext().getFilesDir().getAbsolutePath() + "/" + c.getUrl());
				if(f.exists() && c.getUrl().endsWith(".html")) ll.setBackgroundColor(Color.rgb(240,246,255));
				
				if(c.getUrl().length() > 0 && c.getChildren().size() > 0){
					ImageView iv = (ImageView) v.findViewById(R.id.list_item_image);
					//iv.setImageResource(R.drawable.claim_icon);
				}
			}
			return v;
		}
	}