package com.perc.pavel.sportgeolocationgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;

import java.util.List;

/**
 * Created by pavel on 08.02.2018.
 */

public class MyTeammatesAdapter extends RecyclerView.Adapter<MyTeammatesAdapter.ViewHolder> {
    
    private LayoutInflater inflater;
    private GoogleMapsActivity activity;
    private final List<Player> teammates;
    
    MyTeammatesAdapter(GoogleMapsActivity activity, List<Player> teammates) {
        inflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.teammates = teammates;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_player_name, parent, false);
        
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Player p = teammates.get(position);
        
        holder.tvName.getBackground().setColorFilter(p.teamColor, PorterDuff.Mode.MULTIPLY);
        
        holder.tvName.setText(p.name);
        holder.tvName.setTextColor(p.teamColor);
    }
    
    @Override
    public int getItemCount() {
        return teammates.size();
    }
    
    
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tvName;
        ViewHolder(View view){
            super(view);
            // Растягиваем LinearLayout на всю длину, чтобы текст внутри равнялся по правому краю.
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(lp);
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvName.setOnClickListener(this);
        }
    
        @Override
        public void onClick(View v) {
            int position = this.getAdapterPosition();
            Player p = teammates.get(position);
            
            if (p.hasCoords() && p.hasMarker()) {
                activity.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .tilt(activity.googleMap.getCameraPosition().tilt)// Наклон
                        .target(p.getCoords())
                        .zoom(18)
                        .build()));
                
                p.getMarker().showInfoWindow();
            }
        }
    }
}