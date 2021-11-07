package com.example.notesdemo.veiwmodel

import androidx.lifecycle.*
import com.example.notesdemo.DAO.NotesRepository
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.launch

class NotesViewModel(private val notesRep: NotesRepository): ViewModel() {

    val allNotes: LiveData<MutableList<Notes>> = notesRep.allNotes.asLiveData()

    fun insert(note: Notes) = viewModelScope.launch {
        notesRep.insertNote(note)
    }

    fun delete(note: Notes) = viewModelScope.launch {
        //TODO.Implement
        //notesRep.
    }
}
    class NotesViewModelFactory(private val notesRep: NotesRepository):ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return NotesViewModel(notesRep) as T
            }
            throw IllegalArgumentException("Unknown VieModel Class")
        }
    }
