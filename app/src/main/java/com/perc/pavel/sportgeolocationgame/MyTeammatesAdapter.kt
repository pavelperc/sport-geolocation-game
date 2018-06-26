package com.perc.pavel.sportgeolocationgame

import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.perc.pavel.sportgeolocationgame.activities.GoogleMapsActivity

/**
 * Created by pavel on 08.02.2018.
 */

class MyTeammatesAdapter (private val activity: GoogleMapsActivity, private val teammates: List<Player>) 
    : RecyclerView.Adapter<MyTeammatesAdapter.ViewHolder>() {
    
    private val inflater = LayoutInflater.from(activity)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_player_name, parent, false)
        
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = teammates[position]
        
        holder.tvName.background.setColorFilter(p.teamColor, PorterDuff.Mode.MULTIPLY)
        
        holder.tvName.text = p.name
        holder.tvName.setTextColor(p.teamColor)
    }
    
    override fun getItemCount(): Int {
        return teammates.size
    }
    
    
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val tvName: TextView
        
        init {
            // Растягиваем LinearLayout на всю длину, чтобы текст внутри равнялся по правому краю.
            val lp = view.layoutParams
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = lp
            tvName = view.findViewById<View>(R.id.tvName) as TextView
            tvName.setOnClickListener(this)
        }
        
        override fun onClick(v: View) {
            val position = this.adapterPosition
            val p = teammates[position]
            
            if (p.hasCoords() && p.hasMarker()) {
                activity.animateCameraToLocation(p.coords)
                
                activity.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                        .tilt(activity.googleMap.cameraPosition.tilt)// Наклон
                        .target(p.coords)
                        .zoom(18f)
                        .build()))
                
                p.marker!!.showInfoWindow()
            }
        }
    }
}