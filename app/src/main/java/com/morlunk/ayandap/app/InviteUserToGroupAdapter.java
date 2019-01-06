package com.morlunk.ayandap.app;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.morlunk.ayandap.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class InviteUserToGroupAdapter extends RecyclerView.Adapter<InviteUserToGroupAdapter.ViewHolder> {

  public static int position;
  ArrayList<HashMap<String,String>> choosen;
  static Context context;

  public InviteUserToGroupAdapter(Context context,  ArrayList<HashMap<String,String>> choosen) {
    super();
    RecyclerViewAdapter.context = context;
    this.choosen = choosen;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View v = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.create_group_list_item, viewGroup, false);
    ViewHolder viewHolder = new ViewHolder(v);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
    viewHolder.tvSpecies.setText(choosen.get(i).get("fullname"));
    Picasso.with(context)
      .load(choosen.get(i).get("image"))
      .networkPolicy(NetworkPolicy.OFFLINE)
      .transform(new CropCircleTransformation())
      .fit()
      .into(viewHolder.imgThumbnail, new Callback() {
        @Override
        public void onSuccess() {

        }
        @Override
        public void onError() {
          Picasso.with(context)
            .load(choosen.get(i).get("image"))
            .transform(new CropCircleTransformation())
            .fit()
            .into(viewHolder.imgThumbnail);
        }
      });

    viewHolder.imgThumbnail.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AddUserToGroup.listRemover(i);
      }
    });


  }

  @Override
  public int getItemCount() {
    return choosen.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public ImageView imgThumbnail;
    public TextView tvSpecies;

    public ViewHolder(View itemView) {
      super(itemView);
      imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
      tvSpecies = itemView.findViewById(R.id.tv_species);

      itemView.setOnClickListener(this);

      itemView.setOnLongClickListener(this);

//        imgThumbnail.setOnClickListener(new View.OnClickListener() {
//          @Override
//          public void onClick(View view) {
//            int pos = getPosition();
//            Toast.makeText(context, pos, Toast.LENGTH_LONG).show();
//          }
//        });
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View view) {

      return true;
    }
  }

}