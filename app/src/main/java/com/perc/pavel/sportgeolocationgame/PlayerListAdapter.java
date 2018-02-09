package com.perc.pavel.sportgeolocationgame;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pavel on 08.02.2018.
 */

public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder> {
    
    
    private LayoutInflater inflater;
    private Context context;
    private final List<Player> players;
    
    PlayerListAdapter(Context context, List<Player> players) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.players = players;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_player_name, parent, false);
        
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Player p = players.get(position);
        
        holder.tvName.getBackground().setColorFilter(p.teamColor, PorterDuff.Mode.MULTIPLY);
        
        holder.tvName.setText(p.login);
        holder.tvName.setTextColor(p.teamColor);
    }
    
    @Override
    public int getItemCount() {
        return players.size();
    }
    
    
    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        ViewHolder(View view){
            super(view);
            tvName = (TextView) view.findViewById(R.id.tvName);
        }
    }
}
