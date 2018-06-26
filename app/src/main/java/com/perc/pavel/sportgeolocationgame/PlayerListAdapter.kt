package com.perc.pavel.sportgeolocationgame

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by pavel on 08.02.2018.
 */

class PlayerListAdapter(private val context: Context) : RecyclerView.Adapter<PlayerListAdapter.ViewHolder>() {
    
    val sortedListCallback = object : SortedList.Callback<Player>() {
        override fun compare(o1: Player, o2: Player): Int {
            return Integer.compare(o1.teamColor, o2.teamColor)
        }
        
        override fun onChanged(position: Int, count: Int) {
            this@PlayerListAdapter.notifyItemRangeChanged(position, count)
        }
        
        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.teamColor == newItem.teamColor && oldItem.login == newItem.login
        }
        
        override fun areItemsTheSame(item1: Player, item2: Player): Boolean {
            return item1 == item2
        }
        
        override fun onInserted(position: Int, count: Int) {
            this@PlayerListAdapter.notifyItemRangeInserted(position, count)
        }
        
        override fun onRemoved(position: Int, count: Int) {
            this@PlayerListAdapter.notifyItemRangeRemoved(position, count)
        }
        
        override fun onMoved(fromPosition: Int, toPosition: Int) {
            this@PlayerListAdapter.notifyItemMoved(fromPosition, toPosition)
        }
    }
    
    private inner class PlayerSortedList : SortedList<Player>(Player::class.java, sortedListCallback)
    
    val players: SortedList<Player> = PlayerSortedList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_player_name, parent, false)
        
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = players.get(position)
        
        holder.tvName.background.setColorFilter(p.teamColor, PorterDuff.Mode.MULTIPLY)
        holder.tvName.text = p.name
        holder.tvName.setTextColor(p.teamColor)
    }
    
    override fun getItemCount(): Int {
        return players.size()
    }
    
    
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById<View>(R.id.tvName) as TextView
    }
}