package com.perc.pavel.sportgeolocationgame;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pavel on 08.02.2018.
 */

public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder> {
    private class PlayerSortedList extends SortedList<Player> {
        
        public PlayerSortedList() {
            super(Player.class, new Callback<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    // сравниваем только по цветам
                    return Integer.compare(o1.teamColor, o2.teamColor);
                    
//                      // сначала сравниваем по цветам, потом по именам, потом по логинам
//                    int comp = Integer.compare(o1.teamColor, o2.teamColor);
//                    if (comp == 0) {
//                        comp = o1.name.compareTo(o2.name);
//                        if (comp == 0) {
//                            comp = o1.login.compareTo(o2.login);
//                        }
//                    }
//                    return comp;
                }
                
                @Override
                public void onChanged(int position, int count) {
//                    Log.d("my_tag", "in onChanged position = " + position + " count = " + count);
                    PlayerListAdapter.this.notifyItemRangeChanged(position, count);
                }
                
                @Override
                public boolean areContentsTheSame(Player oldItem, Player newItem) {
//                    Log.d("my_tag", "in areContentsTheSame old = " + oldItem + "   new = " + newItem);
//                    return false;
                    return oldItem.teamColor == newItem.teamColor
                            && oldItem.login.equals(newItem.login);// геолокация нас не интересует
                }
                
                @Override
                public boolean areItemsTheSame(Player item1, Player item2) {
//                    Log.d("my_tag", "in areItemsTheSame item1 = " + item1 + "   item2 = " + item2);
//                    return false;
//                    return item1.equals(item2);
                    return item1 == item2;
                }
                
                @Override
                public void onInserted(int position, int count) {
//                    Log.d("my_tag", "in onInserted position = " + position + " count = " + count);
                    PlayerListAdapter.this.notifyItemRangeInserted(position, count);
                }
                
                @Override
                public void onRemoved(int position, int count) {
//                    Log.d("my_tag", "in onRemoved position = " + position + " count = " + count);
                    PlayerListAdapter.this.notifyItemRangeRemoved(position, count);
                }
                
                @Override
                public void onMoved(int fromPosition, int toPosition) {
//                    Log.d("my_tag", "in onMoved from = " + fromPosition + " to = " + toPosition);
                    PlayerListAdapter.this.notifyItemMoved(fromPosition, toPosition);
                }
            });
        }
    }
    
    private LayoutInflater inflater;
    private Context context;
    private final SortedList<Player> players;
    
    PlayerListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.players = new PlayerSortedList();
    }
    
    SortedList<Player> getPlayers() {
        return players;
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
        
        holder.tvName.setText(p.name);
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