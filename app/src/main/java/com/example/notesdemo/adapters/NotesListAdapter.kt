package com.example.notesdemo.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notesdemo.R
import com.example.notesdemo.extensions.humanizeDiff
import com.example.notesdemo.model.Notes
import com.example.notesdemo.utils.showImagesThumb
import kotlinx.android.synthetic.main.activity_edit_note.*
import kotlinx.android.synthetic.main.notes_list_item.view.*
import java.util.*

class NotesListAdapter : RecyclerView.Adapter<NotesViewHolder>() {
    var notesList = mutableListOf<Notes>()

    var listener: ((Notes) -> Unit)? = null

    var longListener: ((Notes) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setNotes(notes: List<Notes>) {
        this.notesList = notes.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notes_list_item, parent, false)
        return NotesViewHolder(view)

    }


    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val note = notesList[position]
        holder.itemName.text = note.noteName
        holder.itemDate.text = if (note.modifiedDate != null)
            note.modifiedDate!!.humanizeDiff(Date())
        else
            note.createDate.humanizeDiff(Date())
        holder.itemText.text = note.noteText

        if (note.image != null) {
            val imageUri = showImagesThumb(holder.image.context, note.image!!.toUri())

            Glide.with(holder.image.context)
                .load(imageUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(holder.image)
        }

        holder.itemView.setBackgroundColor(if (note.selected) Color.GREEN else Color.TRANSPARENT)

        holder.itemView.setOnClickListener {
            listener?.invoke(notesList[position])
        }

        holder.itemView.setOnLongClickListener {
            longListener?.invoke(notesList[position])
            return@setOnLongClickListener true
        }

    }


    fun setOnNoteTapListener(listener: ((Notes) -> Unit)) {
        this.listener = listener
    }

    fun onNoteLongClickListener(listener: (Notes) -> Unit){
        this.longListener =listener
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    fun getSelectedList() = notesList.filter { note -> note.selected }

}


class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val itemDate = view.list_item_date
    val itemName = view.list_item_name
    val itemText = view.list_item_text
    val image = view.iv_has_image
}


